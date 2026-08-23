import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Publishes race-tolerant last-use markers beside immutable cache objects. */
final class CacheUsage {
    private CacheUsage() { }
    static void touch(Path object) {
        try {
            Path marker = marker(object); Files.createDirectories(marker.getParent());
            Path temporary = marker.resolveSibling(marker.getFileName() + ".tmp-"
                    + ProcessHandle.current().pid() + "-" + Thread.currentThread().threadId());
            Files.writeString(temporary, Long.toString(System.currentTimeMillis()), StandardCharsets.UTF_8);
            try { Files.move(temporary, marker, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporary, marker, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) { }
    }
    static Path marker(Path object) {
        String name = object.getFileName().toString();
        String digest = name.matches("[0-9a-f]{64}") ? name
                : name.replaceFirst("[.](?:properties|log)$", "");
        return object.resolveSibling(digest + ".used");
    }
}
