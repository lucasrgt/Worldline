import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Compiles portable adapters without loading mapped Minecraft classes. */
final class PortableAdapterCheck {
    private final Path root, build;
    private final String release;

    PortableAdapterCheck(Path root, Path build, String release) {
        this.root = root; this.build = build; this.release = release;
    }

    void execute() throws Exception {
        Path classes = build.resolve("classes"), adapters = build.resolve("adapter-classes");
        Path server = root.resolve("adapters/b173-server/src/main/java");
        compile(javaFiles(server), adapters.resolve("b173-server"),
                List.of(classes.resolve("api")));
        List<Path> atlas = new ArrayList<>(javaFiles(server));
        atlas.addAll(javaFiles(root.resolve("adapters/b173-server/src/atlas/java")));
        compile(atlas, adapters.resolve("b173-server-analysis"),
                List.of(classes.resolve("api"), classes.resolve("analysis")));
        compile(javaFiles(root.resolve("adapters/aero-model-lib/src/main/java")),
                adapters.resolve("aero-model-lib"),
                List.of(classes.resolve("analysis"), classes.resolve("trace")));
        compile(legacySources(), adapters.resolve("modloader-forge-java8"), List.of(), "8");
        System.out.println("  portable adapters: compiled");
    }

    private void compile(List<Path> sources, Path output, List<Path> classpath) throws Exception {
        compile(sources, output, classpath, release);
    }

    private void compile(List<Path> sources, Path output, List<Path> classpath,
            String targetRelease) throws Exception {
        if (sources.isEmpty()) throw new IllegalStateException("no adapter sources for " + output);
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList(javaTool("javac"), "-encoding", "UTF-8",
                "--release", targetRelease, "-Xlint:all,-options", "-Werror", "-d", output.toString(),
                "-classpath", classpath.stream().map(Path::toString)
                        .collect(Collectors.joining(System.getProperty("path.separator")))));
        sources.forEach(path -> command.add(path.toString()));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String outputText = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException("adapter compile failed\n" + outputText);
    }

    private static List<Path> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private List<Path> legacySources() throws Exception {
        Path manifest = root.resolve("adapters/modloader-forge/runtime-sources.properties");
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(manifest, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("worldline.legacy-profiler-sources.v1".equals(values.getProperty("schema")),
                "legacy profiler source manifest schema drifted");
        int count = Integer.parseInt(values.getProperty("count", "0"));
        List<Path> sources = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            Path source = root.resolve(values.getProperty("source." + index, "")).normalize();
            require(source.startsWith(root) && Files.isRegularFile(source),
                    "missing legacy profiler source " + source);
            sources.add(source);
        }
        require(count == 15 && sources.stream().distinct().count() == count,
                "legacy profiler source manifest census drifted");
        return sources;
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
