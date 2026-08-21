package worldline.analysis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Canonical checksum-protected registry census section. Rows are sorted by
 * key so the document is a pure function of the captured registry state.
 */
public final class CensusDocument {
    private static final String HEADER = "WORLDLINE-CENSUS/1";
    private static final int MAX_ROWS = 65_536, MAX_ROW_BYTES = 512;

    private CensusDocument() {}

    /** Renders one canonical section; keys must be unique and bounded. */
    public static String section(String name, TreeMap<String, String> rows) {
        if (name == null || !name.matches("[a-z]{3,24}")) {
            throw new IllegalArgumentException("invalid census section name");
        }
        if (rows == null || rows.isEmpty() || rows.size() > MAX_ROWS) {
            throw new IllegalArgumentException("invalid census row count");
        }
        StringBuilder body = new StringBuilder();
        line(body, HEADER);
        line(body, "section=" + name);
        line(body, "rows=" + rows.size());
        for (Map.Entry<String, String> entry : rows.entrySet()) {
            String key = entry.getKey(), value = entry.getValue();
            require(!key.isEmpty() && key.length() <= 64
                    && value.length() * 4 <= MAX_ROW_BYTES, "invalid census row");
            line(body, key + "=" + value);
        }
        String digest = sha256(utf8(body.toString()));
        return body.append("sha256=").append(digest).append('\n').toString();
    }

    private static void line(StringBuilder target, String value) {
        target.append(value).append('\n');
    }
    private static byte[] utf8(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    static String sha256(byte[] value) { try {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 255));
        return result.toString();
    } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
}
