import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/** Retains a bounded local diagnostic set before a failed server cell is removed. */
final class ExecutorServerFailureCapture {
    private ExecutorServerFailureCapture() { }

    static void capture(Path source, Path cell, String label, String id, Exception failure) {
        Path destination = source.resolve(".worldline/executor-server-failures")
                .resolve(label + "-" + id + "-" + UUID.randomUUID());
        List<Path> paths = List.of(
                Path.of(".worldline/smoke-logs", id + ".log"),
                Path.of(".worldline/executor-server-cycle", label, id, "stdout.log"),
                Path.of(".worldline/executor-server-cycle", label, id, "stderr.log"),
                Path.of(".worldline/executor-server-cycle", label, id, "metrics.json"),
                Path.of(".worldline/executor-server-cycle", label, id, "manifest.json"),
                Path.of(".worldline/executor-server-cycle", label, id, "receipt.json"),
                Path.of(".worldline/executor-server-cycle", label, id + "-plan.json"),
                Path.of("reports/verify.json"));
        try {
            Files.createDirectories(destination);
            for (Path relative : paths) copy(cell, destination, relative);
            Files.writeString(destination.resolve("failure.txt"), label + " " + id + "\n"
                    + failure.getClass().getName() + ": " + failure.getMessage() + "\n");
            System.err.println("server failure diagnostics preserved: " + destination);
        } catch (Exception capture) { failure.addSuppressed(capture); }
    }

    static void selfTest() throws Exception {
        Path repository = Path.of("").toAbsolutePath().normalize();
        Path scratch = Files.createTempDirectory(repository.resolve(".worldline"),
                "server-failure-capture-");
        Path source = Files.createDirectories(scratch.resolve("source"));
        Path cell = Files.createDirectories(scratch.resolve("cell"));
        try {
            Path log = cell.resolve(".worldline/smoke-logs/sample.log");
            Files.createDirectories(log.getParent()); Files.writeString(log, "diagnostic\n");
            Files.writeString(cell.resolve("ignored.txt"), "must-not-copy\n");
            capture(source, cell, "rust", "sample", new IllegalStateException("expected"));
            Path root = source.resolve(".worldline/executor-server-failures");
            List<Path> captures; try (var paths = Files.list(root)) { captures = paths.toList(); }
            require(captures.size() == 1
                    && Files.readString(captures.get(0).resolve("sample.log")).equals("diagnostic\n")
                    && Files.readString(captures.get(0).resolve("failure.txt")).contains("expected")
                    && !Files.exists(captures.get(0).resolve("ignored.txt")),
                    "server failure diagnostic boundary drifted");
        } finally { ProcessTreeTermination.delete(repository, scratch, "server failure self-test"); }
    }

    private static void copy(Path cell, Path destination, Path relative) throws Exception {
        Path input = cell.resolve(relative).normalize();
        if (!input.startsWith(cell) || !Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(input)) return;
        Files.copy(input, destination.resolve(relative.getFileName().toString()),
                StandardCopyOption.COPY_ATTRIBUTES);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
