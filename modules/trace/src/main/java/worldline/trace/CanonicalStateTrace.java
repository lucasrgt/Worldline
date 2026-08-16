package worldline.trace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/** Schema-bearing canonical trace for expanded deterministic state vectors. */
public final class CanonicalStateTrace {
    private final StringBuilder value;
    private final int width;

    public CanonicalStateTrace(long seed, String... fields) {
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("trace schema must contain fields");
        }
        Set<String> unique = new HashSet<>();
        value = new StringBuilder("v2|seed=").append(seed).append("|schema=");
        for (int index = 0; index < fields.length; index++) {
            validateName(fields[index], "field");
            if (!unique.add(fields[index])) {
                throw new IllegalArgumentException("duplicate trace field: " + fields[index]);
            }
            if (index > 0) value.append(',');
            value.append(fields[index]);
        }
        width = fields.length;
    }

    public void record(String label, long... fields) {
        validateName(label, "label");
        if (fields == null || fields.length != width) {
            throw new IllegalArgumentException("state width must be " + width);
        }
        value.append('|').append(label).append('=');
        for (int index = 0; index < fields.length; index++) {
            if (index > 0) value.append(',');
            value.append(fields[index]);
        }
    }

    public String value() { return value.toString(); }

    public String signature() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                int unsigned = item & 0xff;
                hex.append(Character.forDigit(unsigned >>> 4, 16));
                hex.append(Character.forDigit(unsigned & 0xf, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    private static void validateName(String value, String role) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(role + " must not be empty");
        }
        for (int index = 0; index < value.length(); index++) {
            char item = value.charAt(index);
            if (!Character.isLetterOrDigit(item) && item != '_') {
                throw new IllegalArgumentException(role + " contains a protocol delimiter: " + value);
            }
        }
    }
}
