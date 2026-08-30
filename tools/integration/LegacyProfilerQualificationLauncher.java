import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Compiles the exact legacy runtime qualification closure before execution. */
public final class LegacyProfilerQualificationLauncher {
    private LegacyProfilerQualificationLauncher() {}

    public static void main(String[] arguments) {
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Path output = root.resolve(".worldline/build/legacy-profiler-qualification");
            Files.createDirectories(output);
            List<String> runtimeCompile = new ArrayList<>(List.of(javaTool("javac"), "-encoding",
                    "UTF-8", "--release", "8", "-Xlint:all,-options", "-Werror", "-d",
                    output.toString()));
            for (Path source : runtimeSources(root)) runtimeCompile.add(source.toString());
            require(run(root, runtimeCompile, 120) == 0,
                    "legacy profiler qualification runtime did not compile");
            List<Path> sources = new ArrayList<>(List.of(
                    root.resolve("tools/integration/LegacyProfilerSourceTransform.java"),
                    root.resolve("tools/integration/LegacyProfilerInstallerSelfTest.java"),
                    root.resolve("tools/integration/LegacyProfilerInstaller.java"),
                    root.resolve("tools/integration/LegacyProfilerQualificationConfig.java"),
                    root.resolve("tools/integration/LegacyLoaderWorkspace.java"),
                    root.resolve("tools/integration/LegacyProfilerQualificationProcess.java"),
                    root.resolve("tools/integration/LegacyProfilerQualificationSelfTest.java"),
                    root.resolve("tools/integration/LegacyProfilerQualification.java"),
                    root.resolve("tools/harness/SafeTreeDelete.java")));
            List<String> compile = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                    "--release", "21", "-Xlint:all,-options", "-Werror", "-classpath",
                    output.toString(), "-d", output.toString()));
            for (Path source : sources) {
                require(Files.isRegularFile(source), "missing qualification source " + source);
                compile.add(source.toString());
            }
            require(run(root, compile, 120) == 0, "legacy profiler qualification did not compile");
            List<String> command = new ArrayList<>(List.of(javaTool("java"), "-cp",
                    output.toString(), "LegacyProfilerQualification"));
            command.addAll(List.of(arguments)); System.exit(run(root, command, 900));
        } catch (Exception error) {
            System.err.println("legacy profiler qualification launcher failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static List<Path> runtimeSources(Path root) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve(
                "adapters/modloader-forge/runtime-sources.properties"), StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        int count = Integer.parseInt(values.getProperty("count", "0"));
        require(count == 15, "legacy profiler runtime source census drifted");
        List<Path> result = new ArrayList<>();
        for (int index = 1; index <= count; index++)
            result.add(root.resolve(values.getProperty("source." + index)).normalize());
        return result;
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
