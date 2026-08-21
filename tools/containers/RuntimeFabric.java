import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Cross-platform coordinator for bounded, fail-closed official-runtime pools. */
public final class RuntimeFabric {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final List<String> BACKENDS = List.of(
            "auto", "windows-job", "windows-appcontainer", "linux-cgroup", "linux-sandbox", "docker");

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            if (List.of(arguments).equals(List.of("doctor"))) { doctor(); return; }
            execute(Options.parse(arguments));
        } catch (Exception error) {
            System.err.println("runtime fabric failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void execute(Options options) throws Exception {
        Capabilities capabilities = Capabilities.detect();
        String backend = select(options.backend, options.isolation, capabilities);
        require(available(backend, capabilities), unavailable(backend, capabilities));
        System.out.println("Worldline Runtime Fabric: backend=" + backend + " isolation=" + options.isolation
                + " jobs=" + options.jobs);
        if (backend.equals("docker")) {
            require(options.action.equals("run"), "Docker simulation is reported by 'doctor'; use its explicit capacity gate on run");
            List<String> delegated = new ArrayList<>(List.of(java(), "tools/containers/ContainerSmokePool.java", "run",
                    options.manifest.toString(), "--jobs", options.jobs));
            if (options.skipVerify) delegated.add("--skip-verify");
            List<String> command = new ArrayList<>(List.of(java(), "tools/containers/OfficialRuntimeLease.java"));
            if (options.lock != null) { command.add("--lock"); command.add(options.lock); }
            command.add("--"); command.addAll(delegated); run(command);
            return;
        }
        List<String> command = new ArrayList<>(List.of(java(), "tools/containers/HostSmokePool.java",
                options.action, options.manifest.toString(), "--jobs", options.jobs, "--backend", backend));
        if (options.config != null) { command.add("--config"); command.add(options.config.toString()); }
        if (options.lock != null) { command.add("--lock"); command.add(options.lock); }
        if (options.skipVerify) command.add("--skip-verify"); run(command);
    }

    private static void doctor() throws Exception {
        Capabilities value = Capabilities.detect();
        System.out.println("Worldline Runtime Fabric doctor");
        System.out.println("host.os=" + value.os); System.out.println("host.filesystem=" + value.fileSystem);
        row("windows-job", value.windowsJob, value.windowsJob ? "ready" : "requires Windows and the .NET Framework C# compiler");
        row("windows-appcontainer", false, "experimental API is never selected automatically; compatibility probe pending");
        row("linux-cgroup", value.linuxCgroup, value.linuxCgroup ? "ready" : "requires Linux, cgroups v2, and a delegated systemd user manager");
        row("linux-sandbox", value.linuxSandbox, value.linuxSandbox ? "ready" : "requires linux-cgroup and bubblewrap");
        row("docker", value.docker, value.docker ? "ready (fallback)" : "Docker daemon unavailable");
        if (value.wsl) {
            Result java = capture(List.of("wsl.exe", "sh", "-lc", "command -v java"), 20);
            Result kernel = capture(List.of("wsl.exe", "sh", "-lc", "test -r /sys/fs/cgroup/cgroup.controllers && command -v systemd-run && command -v bwrap"), 20);
            System.out.println("wsl2=installed; java=" + (java.passed ? "ready" : "missing")
                    + "; cgroup+sandbox=" + (kernel.passed ? "ready" : "missing"));
            System.out.println("wsl2.note=" + (java.passed ? "run Worldline from a repository inside the distribution's ext4 filesystem"
                    : "install a Linux JDK 21, then run Worldline from a repository inside the distribution's ext4 filesystem"));
        }
        if (value.onWslWindowsMount) System.out.println("warning=repository is under /mnt; dense Linux execution is rejected because cross-filesystem I/O is slow");
    }

    private static void row(String backend, boolean available, String detail) {
        System.out.printf(Locale.ROOT, "%-24s %-12s %s%n", backend, available ? "AVAILABLE" : "UNAVAILABLE", detail);
    }
    private static String select(String requested, String isolation, Capabilities value) {
        if (!requested.equals("auto")) return requested;
        if (value.windows) {
            if (isolation.equals("sealed")) return "windows-appcontainer";
            return "windows-job";
        }
        if (isolation.equals("sealed")) return "linux-sandbox";
        return "linux-cgroup";
    }
    private static boolean available(String backend, Capabilities value) {
        return switch (backend) {
            case "windows-job" -> value.windowsJob; case "windows-appcontainer" -> false;
            case "linux-cgroup" -> value.linuxCgroup && !value.onWslWindowsMount;
            case "linux-sandbox" -> value.linuxSandbox && !value.onWslWindowsMount;
            case "docker" -> value.docker; default -> false;
        };
    }
    private static String unavailable(String backend, Capabilities value) {
        if (backend.equals("windows-appcontainer")) return "windows-appcontainer is experimental and has not passed Java/loopback compatibility; choose balanced or an explicit proven backend";
        if (backend.startsWith("linux-") && value.onWslWindowsMount) return "Linux dense mode requires the repository on WSL ext4, not /mnt";
        return "backend is unavailable on this host: " + backend + "; run doctor";
    }

    private static void run(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(ROOT.toFile()).inheritIO().start();
        require(process.waitFor(24, TimeUnit.HOURS), "child coordinator timed out");
        require(process.exitValue() == 0, "child coordinator exited " + process.exitValue());
    }
    private static Result capture(List<String> command, long seconds) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Process process = new ProcessBuilder(command).directory(ROOT.toFile()).redirectErrorStream(true).start();
            boolean finished = process.waitFor(seconds, TimeUnit.SECONDS);
            if (!finished) { process.destroyForcibly(); return new Result(false, "timeout"); }
            process.getInputStream().transferTo(bytes);
            return new Result(process.exitValue() == 0, bytes.toString(StandardCharsets.UTF_8));
        } catch (Exception error) { return new Result(false, error.getMessage()); }
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java").toString(); }
    private static boolean command(String name) { return capture(isWindows() ? List.of("where.exe", name)
            : List.of("sh", "-lc", "command -v " + name), 5).passed; }
    private static boolean isWindows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows"); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }

    private static void selfTest() {
        Capabilities windows = new Capabilities(true, "windows", "ntfs", true, false, false, false, false, true, false);
        require(select("auto", "balanced", windows).equals("windows-job"), "Windows auto-selection drift");
        require(select("auto", "sealed", windows).equals("windows-appcontainer"), "Windows sealed fail-closed drift");
        Capabilities linux = new Capabilities(false, "linux", "ext4", false, true, true, false, false, false, false);
        require(select("auto", "fast", linux).equals("linux-cgroup"), "Linux auto-selection drift");
        require(select("auto", "sealed", linux).equals("linux-sandbox"), "Linux sealed selection drift");
        require(!available("linux-cgroup", new Capabilities(false, "linux", "drvfs", false, true, true, false, true, false, true)),
                "WSL Windows-mount rejection drift");
        if (isWindows()) require(capture(List.of(java(), "tools/containers/WindowsJobBootstrap.java", "--self-test"), 90).passed,
                "Windows Job launcher self-test failed");
        require(capture(List.of(java(), "tools/containers/OfficialRuntimeLease.java", "--self-test"), 30).passed,
                "official runtime lease self-test failed");
        System.out.println("runtime fabric self-test passed");
    }

    private record Result(boolean passed, String output) { }
    private record Capabilities(boolean windows, String os, String fileSystem, boolean windowsJob,
                                boolean linuxCgroup, boolean linuxSandbox, boolean docker,
                                boolean wsl, boolean appContainer, boolean onWslWindowsMount) {
        static Capabilities detect() {
            boolean windows = isWindows(), wsl = windows && capture(List.of("wsl.exe", "--status"), 5).passed;
            String os = windows ? "windows" : System.getProperty("os.name").toLowerCase(Locale.ROOT);
            boolean job = windows && Files.isRegularFile(Path.of(System.getenv().getOrDefault("WINDIR", "C:/Windows"),
                    "Microsoft.NET/Framework64/v4.0.30319/csc.exe"));
            boolean cgroup = !windows && Files.isRegularFile(Path.of("/sys/fs/cgroup/cgroup.controllers"))
                    && command("systemd-run") && capture(List.of("systemctl", "--user", "show-environment"), 5).passed;
            boolean sandbox = cgroup && command("bwrap");
            boolean docker = command("docker") && capture(List.of("docker", "version", "--format", "{{.Server.Version}}"), 5).passed;
            String cwd = ROOT.toString().replace('\\', '/'); boolean mounted = !windows && cwd.startsWith("/mnt/");
            String fileSystem = windows ? "windows-native" : capture(List.of("findmnt", "-n", "-o", "FSTYPE", ROOT.toString()), 5).output.trim();
            return new Capabilities(windows, os, fileSystem, job, cgroup, sandbox, docker, wsl, false, mounted);
        }
    }
    private record Options(String action, Path manifest, String backend, String isolation, String jobs,
                           Path config, String lock, boolean skipVerify) {
        static Options parse(String[] arguments) {
            require(arguments.length >= 2 && List.of("simulate", "run").contains(arguments[0]),
                    "usage: java tools/containers/RuntimeFabric.java doctor|simulate|run MANIFEST"
                            + " [--backend auto|windows-job|windows-appcontainer|linux-cgroup|linux-sandbox|docker]"
                            + " [--isolation fast|balanced|sealed] [--jobs auto|1..25] [--config FILE] [--lock FILE] [--skip-verify]");
            String backend = "auto", isolation = "balanced", jobs = "auto", lock = null; Path config = null; boolean skip = false;
            for (int index = 2; index < arguments.length; index++) switch (arguments[index]) {
                case "--backend" -> backend = next(arguments, ++index); case "--isolation" -> isolation = next(arguments, ++index);
                case "--jobs" -> jobs = next(arguments, ++index); case "--config" -> config = Path.of(next(arguments, ++index)).toAbsolutePath();
                case "--lock" -> lock = next(arguments, ++index); case "--skip-verify" -> skip = true;
                default -> throw new IllegalArgumentException("unknown option: " + arguments[index]); }
            require(BACKENDS.contains(backend), "unknown backend: " + backend);
            require(List.of("fast", "balanced", "sealed").contains(isolation), "unknown isolation: " + isolation);
            require(jobs.equals("auto") || jobs.matches("[1-9]|1[0-9]|2[0-5]"), "jobs must be auto or 1..25");
            return new Options(arguments[0], Path.of(arguments[1]).toAbsolutePath().normalize(), backend, isolation, jobs, config, lock, skip); }
        private static String next(String[] values, int index) { require(index < values.length && !values[index].startsWith("--"), "missing option value"); return values[index]; }
    }
}
