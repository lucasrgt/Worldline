package worldline.symbolgraph;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Properties;

/** Exact byte-length and SHA-256 gate for a locally supplied mapping input. */
public final class MappingPin {
    private final String id;
    private final long bytes;
    private final String sha256;

    private MappingPin(String id, long bytes, String sha256) {
        this.id = id;
        this.bytes = bytes;
        this.sha256 = sha256;
    }

    public static MappingPin load(Path descriptor) throws Exception {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(descriptor, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        String id = required(properties, "id");
        long bytes = Long.parseLong(required(properties, "expected.bytes"));
        String sha256 = required(properties, "expected.sha256");
        if (bytes <= 0 || !sha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("invalid mapping pin: " + id);
        }
        return new MappingPin(id, bytes, sha256);
    }

    public void verify(Path artifact) throws Exception {
        if (!Files.isRegularFile(artifact) || Files.size(artifact) != bytes) {
            throw new IllegalArgumentException("mapping byte-length mismatch: " + id);
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(artifact)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        if (!sha256.equals(hex(digest.digest()))) {
            throw new IllegalArgumentException("mapping SHA-256 mismatch: " + id);
        }
    }

    public String id() { return id; }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("missing " + key);
        return value.trim();
    }

    private static String hex(byte[] bytes) {
        StringBuilder text = new StringBuilder();
        for (byte value : bytes) text.append(String.format("%02x", Integer.valueOf(value & 255)));
        return text.toString();
    }
}
