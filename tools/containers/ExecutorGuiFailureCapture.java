import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Retains a bounded local diagnostic set before a failed GUI cell is removed. */
final class ExecutorGuiFailureCapture {
    private static final Set<String> PROFILES = Set.of("gui", "aero");
    private ExecutorGuiFailureCapture() { }

    static void capture(Path source, Path cell, String profile, String label, String id,
            Exception failure) {
        try {
            require(PROFILES.contains(profile) && label.matches("legacy|rust")
                    && id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid GUI failure identity");
            Path cycle = Path.of(".worldline/executor-" + profile + "-cycle");
            Path destination = source.resolve(".worldline/executor-" + profile + "-failures")
                    .resolve(label + "-" + id + "-" + UUID.randomUUID());
            List<Path> paths = List.of(
                    Path.of(".worldline/smoke-logs", id + ".log"),
                    cycle.resolve(label).resolve("console.log"),
                    cycle.resolve(label).resolve("metrics.properties"),
                    cycle.resolve(label).resolve("stdout.log"),
                    cycle.resolve(label).resolve("stderr.log"),
                    cycle.resolve(label).resolve("metrics.json"),
                    cycle.resolve(label).resolve("manifest.json"),
                    cycle.resolve(label).resolve("receipt.json"),
                    cycle.resolve(label + "-plan.json"), Path.of("reports/verify.json"));
            Files.createDirectories(destination);
            for (Path relative : paths) copy(cell, destination, relative);
            Files.writeString(destination.resolve("failure.txt"), label + " " + id + "\n"
                    + failure.getClass().getName() + ": " + failure.getMessage() + "\n");
            System.err.println(profile + " failure diagnostics preserved: " + destination);
        } catch (Exception capture) { failure.addSuppressed(capture); }
    }

    static void selfTest() throws Exception {
        Path repository = Path.of("").toAbsolutePath().normalize();
        Path scratch = Files.createTempDirectory(repository.resolve(".worldline"),
                "gui-failure-capture-");
        Path source = Files.createDirectories(scratch.resolve("source"));
        Path cell = Files.createDirectories(scratch.resolve("cell"));
        try {
            Path stderr = cell.resolve(".worldline/executor-gui-cycle/rust/stderr.log");
            Files.createDirectories(stderr.getParent()); Files.writeString(stderr, "diagnostic\n");
            Path plan = cell.resolve(".worldline/executor-gui-cycle/rust-plan.json");
            Files.writeString(plan, "{}\n"); Files.writeString(cell.resolve("ignored.txt"), "private\n");
            capture(source, cell, "gui", "rust", "sample", new IllegalStateException("expected"));
            Path root = source.resolve(".worldline/executor-gui-failures");
            List<Path> captures; try (var paths = Files.list(root)) { captures = paths.toList(); }
            require(captures.size() == 1
                    && Files.readString(captures.get(0).resolve("stderr.log")).equals("diagnostic\n")
                    && Files.isRegularFile(captures.get(0).resolve("rust-plan.json"))
                    && !Files.exists(captures.get(0).resolve("ignored.txt")),
                    "GUI failure diagnostic boundary drifted");
        } finally { ProcessTreeTermination.delete(repository, scratch, "GUI failure self-test"); }
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
