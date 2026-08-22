import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Isolated positive and fail-closed checks for OptimizationCatalogCheck. */
public final class OptimizationCatalogCheckTest {
    private static final String VALID = "schema=worldline.optimization.v1\n"
            + "id=sample.fast-path\nsummary=Test fast path.\nsubsystem=test\n"
            + "status=candidate\ndefault.enabled=false\nbehavior.delta=Test-only delta.\n"
            + "risks=Test-only risk.\nrollback=Disable test path.\ntracking=annotation\n"
            + "source.symbols=sample.Subject#work\nevidence=test-fixture\n";
    private final Path checker = Paths.get("tools/harness/OptimizationCatalogCheck.java")
            .toAbsolutePath().normalize();

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new IllegalArgumentException("no arguments expected");
        new OptimizationCatalogCheckTest().execute();
        System.out.println("  optimization catalog self-test: passed");
    }

    private void execute() throws Exception {
        Path root = Files.createTempDirectory("worldline-optimization-check-").toAbsolutePath();
        try {
            Path catalog = root.resolve("worldline/optimizations/catalog/sample.fast-path.properties");
            Path source = root.resolve("modules/sample/src/main/java/sample/Subject.java");
            Files.createDirectories(catalog.getParent()); Files.createDirectories(source.getParent());
            Files.write(catalog, VALID.getBytes(StandardCharsets.UTF_8));
            writeSource(source, "sample.fast-path");
            Outcome valid = run(root); require(valid.code == 0, "valid fixture failed: " + valid.text);
            writeSource(source, "sample.missing");
            Outcome missing = run(root); require(missing.code != 0
                    && missing.text.contains("unknown optimization reference sample.missing"),
                    "unknown reference did not fail closed: " + missing.text);
            writeSource(source, "sample.fast-path");
            Files.write(catalog, VALID.replace("default.enabled=false", "default.enabled=true")
                    .getBytes(StandardCharsets.UTF_8));
            Outcome unsafe = run(root); require(unsafe.code != 0
                    && unsafe.text.contains("non-active optimization defaults on sample.fast-path"),
                    "unsafe default did not fail closed: " + unsafe.text);
        } finally { delete(root); }
    }

    private void writeSource(Path path, String id) throws Exception {
        String source = "package sample;\nimport worldline.optimization.OptimizationRef;\n"
                + "final class Subject {\n    @OptimizationRef(\"" + id
                + "\")\n    void work() {}\n}\n";
        Files.write(path, source.getBytes(StandardCharsets.UTF_8));
    }

    private Outcome run(Path root) throws Exception {
        Process process = new ProcessBuilder("java", checker.toString()).directory(root.toFile())
                .redirectErrorStream(true).start();
        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Outcome(process.waitFor(), text);
    }

    private void delete(Path root) throws Exception {
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path path : ordered) Files.delete(path);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Outcome {
        final int code; final String text;
        Outcome(int code, String text) { this.code = code; this.text = text; }
    }
}
