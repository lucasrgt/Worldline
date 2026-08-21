package worldline.testkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import worldline.test.WorldlineAssertionError;

/** Explicit-update external snapshot store; never writes inline source or evidence. */
final class SnapshotStore {
    private final Path root;
    private final boolean update;

    SnapshotStore(Path root, boolean update) {
        this.root = OutputGuard.safe(root, "snapshot root"); this.update = update;
    }

    void match(String testId, String name, Object actual) {
        String rendered = SnapshotValue.render(actual);
        String file = ArtifactStore.safe(testId + "--" + requiredName(name)) + ".wlsnap";
        Path target = root.resolve(file).normalize();
        if (!target.getParent().equals(root)) throw new IllegalStateException("snapshot path escaped root");
        try {
            if (update) {
                Files.createDirectories(root);
                Files.write(target, rendered.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                return;
            }
            if (!Files.isRegularFile(target)) {
                throw new WorldlineAssertionError("snapshot is missing; rerun with --update-snapshots",
                        target.getFileName(), rendered);
            }
            String expected = new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
            if (!expected.equals(rendered)) {
                throw new WorldlineAssertionError("snapshot does not match", expected, rendered);
            }
        } catch (IOException error) {
            throw new IllegalStateException("snapshot I/O failed: " + error.getMessage(), error);
        }
    }

    private static String requiredName(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("snapshot name is blank");
        return value.trim();
    }
}
