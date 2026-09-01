import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Compiles the exact smoke-owned coordinator closure before runtime qualification. */
final class ExternalRuntimeCoordinatorBuild {
    private ExternalRuntimeCoordinatorBuild() {
    }

    static void compile(Path root, String id, Properties descriptor, Path runnerClasses,
            Path output) throws Exception {
        Path smoke = root.resolve("smokes").resolve(id).normalize();
        String main = required(descriptor, "runner.main");
        require(main.matches("[A-Za-z_$][A-Za-z0-9_$.]*"),
            "invalid external-runtime coordinator class");
        Path source = smoke.resolve(required(descriptor, "runner.sources")).normalize();
        require(source.startsWith(smoke) && Files.isDirectory(source),
            "external-runtime coordinator sources escape their smoke");
        List<Path> sources = SafeTreeDelete.paths(source).stream()
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".java"))
            .sorted().toList();
        require(!sources.isEmpty(), "external-runtime coordinator has no sources");
        Files.createDirectories(output);
        String classpath = runnerClasses + System.getProperty("path.separator")
            + System.getenv("WORLDLINE_HARNESS_CP");
        List<String> command = new ArrayList<>(List.of(javaTool(), "-encoding", "UTF-8",
            "--release", "21", "-Xlint:all,-options", "-Werror", "-classpath", classpath,
            "-d", output.toString()));
        for (Path path : sources) command.add(path.toString());
        ProcessCapture.Result result = ProcessCapture.run(root, command, 300);
        require(!result.timedOut(), "external-runtime coordinator compilation timed out");
        require(result.exit() == 0, "external-runtime coordinator compilation failed:\n"
            + result.output());
        Path mainClass = output.resolve(main.replace('.', '/') + ".class");
        require(Files.isRegularFile(mainClass),
            "external-runtime coordinator main class was not compiled: " + main);
        try (URLClassLoader loader = new URLClassLoader(
                new java.net.URL[] {output.toUri().toURL()},
                ExternalRuntimeCoordinatorBuild.class.getClassLoader())) {
            Class<?> type = Class.forName(main, false, loader);
            require(type.getConstructor() != null
                    && type.getMethod("execute", String.class).getReturnType() == void.class,
                "external-runtime coordinator contract drift: " + main);
        }
        System.out.println("  compiled external-runtime coordinator: " + main);
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }

    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
            "javac" + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
