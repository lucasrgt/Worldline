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
        try { initializesAndDiagnoses(root); diagnosesPackageManagerFixture(root);
            initializesGuiTemplate(root); migratesLegacySpecs(root); }
        finally { delete(root); }
        System.out.println("WorldlineProjectCommandTest passed");
    }
    private static void initializesGuiTemplate(Path root) throws Exception {
        Path target = root.resolve("gui/tests/worldline");
        int status = WorldlineCli.run(new String[] {"init", "--target=" + target,
                "--template=gui", "--no-wrapper"}, System.out, System.err);
        String sample = Files.readString(target.resolve("src/test/java/example/ExampleWorldlineTest.java"));
        require(status == 0 && sample.contains("context.ui()")
                && sample.contains("shouldHaveCount(45)") && !sample.contains(".todo()"),
                "GUI template is not executable");
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
    private static void diagnosesPackageManagerFixture(Path root) throws Exception {
        Path instance = root.resolve("wpm-instance"), target = instance.resolve("tests/worldline");
        int status = WorldlineCli.run(new String[] {"init", "--target=" + target,
                "--host-only", "--template=basic", "--no-wrapper"}, System.out, System.err);
        require(status == 0, "WPM fixture TestKit init");
        Path payload = instance.resolve("packages/worldline-testkit.fixture");
        Files.createDirectories(payload.getParent());
        Files.writeString(payload, WorldlinePackageInstance.packageBytes(), StandardCharsets.UTF_8);
        String lock = "schema=worldline.wpm-fixture-lock.v1\nruntime=b1.7.3\nmode=host-only\n"
                + "official.artifacts=external-only\npackage.count=1\n"
                + "package.0.id=worldline-testkit-fixture\npackage.0.version=0.0.0-fixture\n"
                + "package.0.path=packages/worldline-testkit.fixture\npackage.0.sha256="
                + PinnedDownload.digest(payload) + "\n";
        Path lockPath = instance.resolve("wpm.lock"); Files.writeString(lockPath, lock);
        ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
        status = WorldlineCli.run(new String[] {"doctor", target.toString()},
                new PrintStream(output), new PrintStream(error));
        require(status == 0 && output.toString(StandardCharsets.UTF_8)
                        .contains("WPM fixture instance verified"), "WPM fixture doctor");
        Files.writeString(lockPath, lock.replace("external-only", "embedded"));
        require(WorldlineCli.run(new String[] {"doctor", target.toString()}, System.out,
                new PrintStream(error)) == 1, "embedded official-artifact policy was accepted");
        Files.writeString(lockPath, lock); Files.writeString(payload, "corrupt\n");
        require(WorldlineCli.run(new String[] {"doctor", target.toString()}, System.out,
                new PrintStream(error)) == 1, "corrupt WPM fixture package was accepted");
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
