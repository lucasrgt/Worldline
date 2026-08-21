package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WorldlineProjectCommandTest {
    private WorldlineProjectCommandTest() {}
    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-project-command-");
        try { initializesAndDiagnoses(root); migratesLegacySpecs(root); }
        finally { delete(root); }
        System.out.println("WorldlineProjectCommandTest passed");
    }
    private static void initializesAndDiagnoses(Path root) throws Exception {
        Path target = root.resolve("mod/tests/worldline"); ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int status = WorldlineCli.run(new String[] {"init", "--target=" + target,
                "--host-only", "--template=storage", "--no-wrapper"},
                new PrintStream(bytes), System.err);
        require(status == 0 && Files.isRegularFile(target.resolve("build.gradle.kts")), "init build");
        require(Files.isRegularFile(target.resolve(".local/oracles/b1.7.3/.gitignore")), "oracle guard");
        require(Files.readString(target.resolve("src/test/java/example/ExampleWorldlineTest.java"))
                .contains(".todo()"), "template contract");
        bytes.reset(); status = WorldlineCli.run(new String[] {"doctor", target.toString()},
                new PrintStream(bytes), System.err);
        require(status == 0 && bytes.toString(StandardCharsets.UTF_8).contains("WORLDLINE_DOCTOR=PASS"),
                "host-only doctor");
    }
    private static void migratesLegacySpecs(Path root) throws Exception {
        Path repository = root.resolve("legacy"); Path source = repository.resolve(
                "worldline-tests/src/test/java/example/LegacySpec.java");
        Files.createDirectories(source.getParent()); Files.writeString(source, "class LegacySpec {}\n");
        Files.writeString(repository.resolve("worldline-test.properties"), "format=1\nsource=old\n");
        int status = WorldlineCli.run(new String[] {"migrate", "--root=" + repository, "--no-wrapper"},
                System.out, System.err);
        require(status == 0 && Files.isRegularFile(repository.resolve(
                "tests/worldline/src/test/java/example/LegacySpec.java")), "migrated source");
        require(Files.isRegularFile(source), "legacy source retained");
        require(Files.isRegularFile(repository.resolve(
                "tests/worldline/legacy-worldline-test.properties")), "preserved legacy config");
    }
    private static void delete(Path root) throws Exception {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
