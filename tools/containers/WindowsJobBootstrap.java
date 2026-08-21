import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Builds and probes the source-distributed Windows Job Object launcher. */
public final class WindowsJobBootstrap {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path SOURCE = ROOT.resolve("tools/containers/WindowsJobRunner.cs");
    private static final Path OUTPUT = ROOT.resolve(".worldline/tools/WindowsJobRunner.exe");

    public static void main(String[] arguments) {
        try {
            require(isWindows(), "Windows Job Objects require Windows");
            require(arguments.length == 1 && List.of("ensure", "--self-test").contains(arguments[0]),
                    "usage: java tools/containers/WindowsJobBootstrap.java ensure|--self-test");
            Path executable = ensure();
            if (arguments[0].equals("ensure")) System.out.println(executable);
            else selfTest(executable);
        } catch (Exception error) {
            System.err.println("windows job bootstrap failed: " + error.getMessage()); System.exit(1);
        }
    }

    static Path ensure() throws Exception {
        require(Files.isRegularFile(SOURCE), "missing launcher source: " + SOURCE);
        if (Files.isRegularFile(OUTPUT) && Files.getLastModifiedTime(OUTPUT).compareTo(Files.getLastModifiedTime(SOURCE)) >= 0)
            return OUTPUT;
        Files.createDirectories(OUTPUT.getParent()); Path compiler = compiler();
        Process process = new ProcessBuilder(compiler.toString(), "/nologo", "/optimize+", "/target:exe",
                "/out:" + OUTPUT, SOURCE.toString()).directory(ROOT.toFile()).inheritIO().start();
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0, "C# launcher compilation failed");
        require(Files.isRegularFile(OUTPUT), "compiler produced no launcher"); return OUTPUT;
    }

    private static void selfTest(Path executable) throws Exception {
        Path test = ROOT.resolve(".worldline/job-runner-self-test"); Files.createDirectories(test);
        Path log = test.resolve("console.log"), metrics = test.resolve("metrics.properties");
        Process process = new ProcessBuilder(executable.toString(), "--memory", Long.toString(96L << 20),
                "--cpu-rate", "10000", "--active-processes", "4", "--timeout-seconds", "10",
                "--cwd", ROOT.toString(), "--log", log.toString(), "--metrics", metrics.toString(),
                "--", "cmd.exe", "/d", "/c", "echo job-self-test").directory(ROOT.toFile()).inheritIO().start();
        require(process.waitFor(20, TimeUnit.SECONDS) && process.exitValue() == 0, "launcher probe failed");
        require(Files.readString(log, StandardCharsets.UTF_8).contains("job-self-test"), "launcher did not capture output");
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) { values.load(reader); }
        require("windows-job".equals(values.getProperty("backend")) && "0".equals(values.getProperty("exit.code")),
                "launcher metrics drift");
        Path timeoutLog = test.resolve("timeout.log"), timeoutMetrics = test.resolve("timeout.properties");
        String marker = "worldline-job-tree-probe";
        Process timeout = new ProcessBuilder(executable.toString(), "--memory", Long.toString(256L << 20),
                "--cpu-rate", "10000", "--active-processes", "8", "--timeout-seconds", "1",
                "--cwd", ROOT.toString(), "--log", timeoutLog.toString(), "--metrics", timeoutMetrics.toString(),
                "--", "powershell.exe", "-NoProfile", "-Command", "$null=Start-Process powershell.exe -ArgumentList"
                        + " '-NoProfile','-Command','Start-Sleep -Seconds 30 # " + marker + "'; Start-Sleep -Seconds 30")
                .directory(ROOT.toFile()).inheritIO().start();
        require(timeout.waitFor(15, TimeUnit.SECONDS) && timeout.exitValue() == 124, "timeout probe did not terminate the job");
        Properties timed = new Properties(); try (var reader = Files.newBufferedReader(timeoutMetrics)) { timed.load(reader); }
        require("true".equals(timed.getProperty("timed.out")), "timeout metrics drift");
        require(ProcessHandle.allProcesses().noneMatch(candidate -> candidate.info().commandLine().orElse("").contains(marker)),
                "a timed-out descendant escaped the Job Object");
        System.out.println("windows job launcher self-test passed");
    }

    private static Path compiler() {
        String windows = System.getenv("WINDIR"); require(windows != null, "WINDIR is unavailable");
        Path csc = Path.of(windows, "Microsoft.NET", "Framework64", "v4.0.30319", "csc.exe");
        require(Files.isRegularFile(csc), "C# compiler is unavailable: " + csc); return csc;
    }
    static boolean isWindows() { return System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("windows"); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
}
