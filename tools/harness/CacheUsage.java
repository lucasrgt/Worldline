import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

/** Publishes race-tolerant last-use markers beside immutable cache objects. */
final class CacheUsage {
    private CacheUsage() { }
    static void touch(Path object) throws IOException {
        Path marker = marker(object); Files.createDirectories(marker.getParent());
        Path temporary = marker.resolveSibling(marker.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + Thread.currentThread().threadId());
        try {
            Files.writeString(temporary, Long.toString(System.currentTimeMillis()), StandardCharsets.UTF_8);
            try { Files.move(temporary, marker, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporary, marker, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }
    static Path marker(Path object) {
        String name = object.getFileName().toString();
        String digest = name.matches("[0-9a-f]{64}") ? name
                : name.replaceFirst("[.](?:properties|log)$", "");
        return object.resolveSibling(digest + ".used");
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-cache-usage-");
        try {
            Path proof = root.resolve("objects/" + "a".repeat(64) + ".properties");
            Files.createDirectories(proof.getParent()); Files.writeString(proof, "proof");
            touch(proof); if (!Files.isRegularFile(marker(proof)))
                throw new IllegalStateException("cache usage marker was not published");
            Path obstruction = root.resolve("blocked"); Files.writeString(obstruction, "file");
            boolean rejected = false;
            try { touch(obstruction.resolve("proof.properties")); }
            catch (java.io.IOException expected) { rejected = true; }
            if (!rejected) throw new IllegalStateException("cache usage publication failed open");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  cache usage fail-closed self-test: passed");
    }
}
