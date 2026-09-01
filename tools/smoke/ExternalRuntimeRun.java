import java.net.URLClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Compiles and executes a smoke-owned coordinator for a pinned external runtime. */
public final class ExternalRuntimeRun {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final String id;

    private ExternalRuntimeRun(String id) {
        SmokeSupport.require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"),
            "invalid external-runtime smoke id");
        this.id = id;
    }

    public static void main(String[] arguments) {
        if (arguments.length != 1) {
            System.err.println("usage: ExternalRuntimeRun ID");
            System.exit(2);
        }
        try {
            new ExternalRuntimeRun(arguments[0]).run();
        } catch (Exception error) {
            System.err.println(arguments[0] + " external-runtime smoke failed: "
                + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void run() throws Exception {
        Path smoke = root.resolve("smokes").resolve(id).normalize();
        Properties config = new Properties();
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        String main = SmokeSupport.value(config, "runner.main");
        SmokeSupport.require(main.matches("[A-Za-z_$][A-Za-z0-9_$.]*"),
            "invalid external-runtime coordinator class");
        Path source = smoke.resolve(SmokeSupport.value(config, "runner.sources")).normalize();
        SmokeSupport.require(source.startsWith(smoke) && Files.isDirectory(source),
            "external-runtime coordinator sources escape their smoke");
        Path output = root.resolve(".worldline/build/external-runtime-runners")
            .resolve(id).normalize();
        SmokeSupport.recreate(root, output);
        compile(source, output);
        try (URLClassLoader loader = new URLClassLoader(
                new java.net.URL[] {output.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> type = Class.forName(main, true, loader);
            Object value = type.getDeclaredConstructor().newInstance();
            try {
                type.getMethod("execute", String.class).invoke(value, id);
            } catch (InvocationTargetException error) {
                if (error.getCause() instanceof Exception cause) throw cause;
                throw error;
            }
        }
    }

    private void compile(Path source, Path output) throws Exception {
        List<String> command = new ArrayList<>(List.of("javac", "-encoding", "UTF-8",
            "--release", "21", "-Xlint:all,-options", "-Werror", "-classpath",
            System.getProperty("java.class.path"), "-d", output.toString()));
        List<Path> sources = new ArrayList<>();
        javaFiles(source, sources);
        sources.stream().sorted().forEach(path -> command.add(path.toString()));
        SmokeSupport.require(command.size() > 11, "external-runtime coordinator has no sources");
        SmokeSupport.capture(root, command, 300);
    }

    private void javaFiles(Path directory, List<Path> result) throws Exception {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            for (Path path : entries) {
                SmokeSupport.require(!Files.isSymbolicLink(path),
                    "external-runtime coordinator source link is forbidden: " + path);
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    javaFiles(path, result);
                } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                        && path.toString().endsWith(".java")) {
                    result.add(path);
                }
            }
        }
    }
}
