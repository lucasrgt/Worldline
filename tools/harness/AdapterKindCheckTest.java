import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Isolated positive and fail-closed checks for AdapterKindCheck. */
public final class AdapterKindCheckTest {
    private final Path checker = Paths.get("tools/harness/AdapterKindCheck.java")
            .toAbsolutePath().normalize();

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new IllegalArgumentException("no arguments expected");
        new AdapterKindCheckTest().execute();
        System.out.println("  adapter kind self-test: passed");
    }

    private void execute() throws Exception {
        Path root = Files.createTempDirectory("worldline-adapter-kind-").toAbsolutePath();
        try {
            write(root, "sample-mod", "extension");
            Files.write(root.resolve("harness.properties"),
                    "adapter.extensions=sample-mod\n".getBytes(StandardCharsets.UTF_8));
            Outcome valid = run(root);
            require(valid.code == 0, "valid extension failed: " + valid.text);
            write(root, "sample-mod", "driver");
            Outcome kind = run(root);
            require(kind.code != 0 && kind.text.contains("adapter kind sample-mod"),
                    "unknown driver did not fail closed: " + kind.text);
        } finally { delete(root); }
    }

    private void write(Path root, String adapter, String kind) throws Exception {
        Path manifest = root.resolve("worldline").resolve("extensions").resolve(adapter)
                .resolve("manifest.properties");
        Files.createDirectories(manifest.getParent());
        String text = "schema=worldline.adapter.semantics.v1\nadapter=" + adapter
                + "\nkind=" + kind + "\nowner.prefix=worldline/sample/\n";
        Files.write(manifest, text.getBytes(StandardCharsets.UTF_8));
    }

    private Outcome run(Path root) throws Exception {
        Process process = new ProcessBuilder("java", checker.toString(), root.toString())
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
