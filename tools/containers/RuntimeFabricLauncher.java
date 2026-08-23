import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/** Compiles the cohesive Runtime Fabric sources before invoking one backend. */
public final class RuntimeFabricLauncher {
    private RuntimeFabricLauncher() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length > 0 && arguments[0].matches("[A-Za-z][A-Za-z0-9]+"),
                    "usage: RuntimeFabricLauncher MAIN [ARG...]");
            Path root = Path.of("").toAbsolutePath().normalize();
            Path output = root.resolve(".worldline/tools/runtime-fabric-classes");
            Files.createDirectories(output);
            List<String> compile = new ArrayList<>(List.of(tool("javac"), "-encoding", "UTF-8",
                    "--release", "21", "-Xlint:all,-options", "-Werror", "-d", output.toString()));
            try (Stream<Path> sources = Files.list(root.resolve("tools/containers"))) {
                compile.addAll(sources.filter(path -> path.toString().endsWith(".java"))
                        .sorted(Comparator.naturalOrder()).map(Path::toString).toList());
            }
            run(root, compile, 180);
            List<String> command = new ArrayList<>(List.of(tool("java"), "-cp", output.toString(), arguments[0]));
            command.addAll(List.of(arguments).subList(1, arguments.length)); run(root, command, 86_400);
        } catch (Exception error) {
            System.err.println("Runtime Fabric launcher failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void run(Path root, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
        require(process.waitFor(seconds, TimeUnit.SECONDS), command.get(0) + " timed out");
        require(process.exitValue() == 0, command.get(0) + " exited " + process.exitValue());
    }
    private static String tool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
