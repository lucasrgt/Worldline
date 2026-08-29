import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Compiles the exact legacy profiler installer source closure before execution. */
public final class LegacyProfilerInstallerLauncher {
    private LegacyProfilerInstallerLauncher() { }

    public static void main(String[] arguments) {
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Path output = root.resolve(".worldline/build/legacy-profiler-installer");
            Files.createDirectories(output);
            List<Path> sources = List.of(
                    root.resolve("tools/integration/LegacyProfilerSourceTransform.java"),
                    root.resolve("tools/integration/LegacyProfilerInstallerSelfTest.java"),
                    root.resolve("tools/integration/LegacyProfilerInstaller.java"),
                    root.resolve("tools/harness/SafeTreeDelete.java"));
            List<String> compile = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                    "--release", "21", "-Xlint:all,-options", "-Werror", "-d", output.toString()));
            for (Path source : sources) {
                require(Files.isRegularFile(source), "missing installer source " + source);
                compile.add(source.toString());
            }
            require(run(root, compile, 120) == 0, "legacy profiler installer did not compile");
            List<String> command = new ArrayList<>(List.of(javaTool("java"), "-cp",
                    output.toString(), "LegacyProfilerInstaller"));
            command.addAll(List.of(arguments));
            System.exit(run(root, command, 180));
        } catch (Exception error) {
            System.err.println("legacy profiler installer launcher failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static int run(Path root, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.destroyForcibly(); throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }
    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : ""))
                .toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
