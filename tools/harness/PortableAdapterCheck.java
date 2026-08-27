import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
        Path testkit = root.resolve("adapters/b173-server/src/testkit/java");
        compile(javaFiles(testkit), adapters.resolve("b173-server-testkit"),
                List.of(classes.resolve("api"), classes.resolve("testmodel"),
                        classes.resolve("testapi"), adapters.resolve("b173-server")));
        copyResources(root.resolve("adapters/b173-server/src/testkit/resources"),
                adapters.resolve("b173-server-testkit"));
        Path testkitTest = adapters.resolve("b173-server-testkit-test");
        List<Path> testkitClasspath = List.of(classes.resolve("api"), classes.resolve("testmodel"),
                classes.resolve("testapi"), adapters.resolve("b173-server"),
                adapters.resolve("b173-server-testkit"));
        compile(javaFiles(root.resolve("adapters/b173-server/src/testkitTest/java")),
                testkitTest, testkitClasspath);
        run("worldline.b173server.B173ServerLifecycleProviderTest", testkitTest,
                testkitClasspath);
        run("worldline.b173server.B173ServerStateDomainProviderTest", testkitTest,
                testkitClasspath);
        run("worldline.b173server.B173ServerCollisionProviderTest", testkitTest,
                testkitClasspath);
        run("worldline.b173server.B173ServerLightProviderTest", testkitTest,
                testkitClasspath);
        List<Path> atlas = new ArrayList<>(javaFiles(server));
        atlas.addAll(javaFiles(root.resolve("adapters/b173-server/src/atlas/java")));
        compile(atlas, adapters.resolve("b173-server-analysis"),
                List.of(classes.resolve("api"), classes.resolve("analysis")));
        compile(javaFiles(root.resolve("adapters/aero-model-lib/src/main/java")),
                adapters.resolve("aero-model-lib"),
                List.of(classes.resolve("analysis"), classes.resolve("trace")));
        System.out.println("  portable adapters: compiled");
    }

    private static void copyResources(Path source, Path output) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted().collect(Collectors.toList())) {
                if (!Files.isRegularFile(path)) continue;
                Path target = output.resolve(source.relativize(path).toString());
                Files.createDirectories(target.getParent());
                Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void compile(List<Path> sources, Path output, List<Path> classpath) throws Exception {
        if (sources.isEmpty()) throw new IllegalStateException("no adapter sources for " + output);
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList(javaTool("javac"), "-encoding", "UTF-8",
                "--release", release, "-Xlint:all,-options", "-Werror", "-d", output.toString(),
                "-classpath", classpath.stream().map(Path::toString)
                        .collect(Collectors.joining(System.getProperty("path.separator")))));
        sources.forEach(path -> command.add(path.toString()));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String outputText = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException("adapter compile failed\n" + outputText);
    }

    private void run(String main, Path output, List<Path> classpath) throws Exception {
        List<Path> paths = new ArrayList<Path>(); paths.add(output); paths.addAll(classpath);
        Process process = new ProcessBuilder(javaTool("java"), "-classpath",
                paths.stream().map(Path::toString).collect(Collectors.joining(
                        System.getProperty("path.separator"))), main).directory(root.toFile())
                .redirectErrorStream(true).start();
        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException("adapter test failed\n" + text);
        System.out.print(text);
    }

    private static List<Path> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }
}
