package worldline.cli;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

/** HTTPS download with a frozen SHA-256 and atomic publication. */
final class PinnedDownload {
    private PinnedDownload() {}
    static void fetch(String url, String sha256, Path target) throws Exception {
        require(url.startsWith("https://"), "download must use HTTPS");
        Files.createDirectories(target.getParent()); Path temporary = Files.createTempFile(
                target.getParent(), "worldline-download-", ".part");
        try {
            try (InputStream input = URI.create(url).toURL().openStream()) {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            require(digest(temporary).equals(sha256), "download checksum mismatch: " + url);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } finally { Files.deleteIfExists(temporary); }
    }
    static String digest(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) digest.update(buffer, 0, read);
        }
        StringBuilder value = new StringBuilder();
        for (byte item : digest.digest()) value.append(String.format("%02x", item & 255));
        return value.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
