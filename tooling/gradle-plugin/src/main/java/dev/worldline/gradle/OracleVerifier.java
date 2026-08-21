package dev.worldline.gradle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Frozen identity checks; configured paths never define oracle identity. */
final class OracleVerifier {
    private OracleVerifier() {}
    static void verify(Path path, long bytes, String sha256) {
        try {
            require(Files.isRegularFile(path), "oracle is absent: " + path);
            require(Files.size(path) == bytes, "oracle size mismatch: " + path);
            require(digest(path).equals(sha256), "oracle SHA-256 mismatch: " + path);
        } catch (IOException error) { throw new IllegalStateException("cannot verify oracle " + path, error); }
    }
    static String digest(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                for (int read; (read = input.read(buffer)) >= 0;) digest.update(buffer, 0, read);
            }
            StringBuilder value = new StringBuilder();
            for (byte item : digest.digest()) value.append(String.format("%02x", item & 255));
            return value.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
