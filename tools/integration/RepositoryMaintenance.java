import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Idempotent per-clone Git setup and fail-closed Windows capability doctor. */
public final class RepositoryMaintenance {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("setup"))) new RepositoryMaintenance().setup();
            else if (List.of(arguments).equals(List.of("doctor"))) new RepositoryMaintenance().doctor();
            else throw new IllegalArgumentException(
                    "usage: java tools/integration/RepositoryMaintenance.java setup|doctor");
        } catch (Exception error) {
            System.err.println("repository maintenance failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void setup() throws Exception {
        require(run(root, 30, "git", "rev-parse", "--show-toplevel").passed, "not a Git repository");
        git("config", "core.untrackedCache", "true"); git("config", "core.longpaths", "true");
        Result fsmonitor = run(root, 30, "git", "fsmonitor--daemon", "start");
        if (!fsmonitor.passed) fsmonitor = run(root, 15, "git", "fsmonitor--daemon", "status");
        if (fsmonitor.passed) git("config", "core.fsmonitor", "true");
        else System.out.println("fsmonitor.setup=unavailable; " + oneLine(fsmonitor.output));
        git("config", "merge.worldline-smoke-lock.name", "Worldline qualification lock ordered union");
        git("config", "merge.worldline-smoke-lock.driver",
                "java tools/integration/QualificationLockMerge.java %O %A %B");
        git("maintenance", "start");
        git("commit-graph", "write", "--reachable", "--changed-paths");
        doctor();
    }

    private void doctor() throws Exception {
        require("true".equals(config("core.untrackedCache")), "core.untrackedCache is not true; run setup");
        require("true".equals(config("core.longpaths")), "core.longpaths is not true; run setup");
        require(config("merge.worldline-smoke-lock.driver").contains("qualificationlockmerge.java"),
                "qualification lock merge driver is absent; run setup");
        Result cache = run(root, 30, "git", "update-index", "--test-untracked-cache");
        require(cache.passed, "untracked cache probe failed: " + oneLine(cache.output));
        Result graph = run(root, 60, "git", "commit-graph", "verify");
        require(graph.passed, "commit graph verification failed: " + oneLine(graph.output));
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows");
        if (windows) windowsDoctor();
        Result fsmonitor = run(root, 15, "git", "fsmonitor--daemon", "status");
        System.out.println("git.fsmonitor=" + (fsmonitor.passed ? "ready" : "unavailable"));
        System.out.println("git.untracked-cache=ready"); System.out.println("git.commit-graph=ready");
        System.out.println("git.longpaths=ready");
        System.out.println("WORLDLINE_REPOSITORY_DOCTOR=PASS");
    }

    private void windowsDoctor() throws Exception {
        Result paths = run(root, 15, "reg.exe", "query",
                "HKLM\\SYSTEM\\CurrentControlSet\\Control\\FileSystem", "/v", "LongPathsEnabled");
        require(paths.passed && paths.output.matches("(?s).*LongPathsEnabled\\s+REG_DWORD\\s+0x1.*"),
                "Windows long paths are disabled; administrator action: Set-ItemProperty "
                        + "'HKLM:\\SYSTEM\\CurrentControlSet\\Control\\FileSystem' "
                        + "-Name LongPathsEnabled -Type DWord -Value 1");
        long started = System.nanoTime(); Path probe = Files.createTempDirectory(
                root.resolve(".worldline"), "defender-probe-");
        try {
            byte[] content = new byte[16_384];
            for (int index = 0; index < 128; index++) Files.write(probe.resolve(index + ".bin"), content);
        } finally { SafeTreeDelete.delete(probe); }
        long millis = Math.max(1L, (System.nanoTime() - started) / 1_000_000L);
        System.out.println("defender.current-path-probe.ms=" + millis + ";files=128;bytes=2097152");
        String escaped = root.toString().replace("'", "''");
        System.out.println("defender.optional-administrator-action=Add-MpPreference -ExclusionPath '"
                + escaped + "'");
        System.out.println("defender.note=measure before and after explicitly; setup never changes exclusions");
    }

    private String config(String name) throws Exception {
        Result result = run(root, 15, "git", "config", "--get", name);
        return result.passed ? result.output.trim().toLowerCase(Locale.ROOT) : "";
    }

    private void git(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Result result = run(root, 120, command.toArray(String[]::new));
        require(result.passed, "git " + String.join(" ", arguments) + " failed: " + oneLine(result.output));
    }

    private static Result run(Path directory, int timeout, String... command) throws Exception {
        Path log = Files.createTempFile("worldline-maintenance-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            if (!finished) { process.destroyForcibly(); return new Result(false, "timeout"); }
            return new Result(process.exitValue() == 0,
                    Files.readString(log, StandardCharsets.UTF_8));
        } finally { Files.deleteIfExists(log); }
    }

    private static String oneLine(String value) { return value.replaceAll("\\s+", " ").trim(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Result(boolean passed, String output) {}
}
