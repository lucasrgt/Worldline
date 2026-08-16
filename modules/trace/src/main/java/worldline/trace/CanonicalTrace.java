package worldline.trace;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** Versioned canonical observation protocol shared by subjects and oracles. */
public final class CanonicalTrace {
    public static final String TRACE_PREFIX = "WORLDLINE_SMOKE_TRACE=";
    public static final String SIGNATURE_PREFIX = "WORLDLINE_SMOKE_SIGNATURE=";

    private final StringBuilder value;

    public CanonicalTrace(long seed) {
        value = new StringBuilder("v1|seed=").append(seed);
    }

    public void record(String label, long worldTime, int entityCount, int... column) {
        validateLabel(label);
        if (entityCount < 0) {
            throw new IllegalArgumentException("entity count must not be negative");
        }
        Objects.requireNonNull(column, "column");
        if (column.length == 0) {
            throw new IllegalArgumentException("column must contain at least one value");
        }
        value.append('|').append(label)
                .append(":time=").append(worldTime)
                .append(",entities=").append(entityCount)
                .append(",column=");
        for (int index = 0; index < column.length; index++) {
            if (index > 0) {
                value.append('.');
            }
            value.append(column[index]);
        }
    }

    public String value() {
        return value.toString();
    }

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

    public void emitTo(PrintStream output) {
        Objects.requireNonNull(output, "output");
        output.println(TRACE_PREFIX + value());
        output.println(SIGNATURE_PREFIX + signature());
    }

    private void validateLabel(String label) {
        Objects.requireNonNull(label, "label");
        if (label.isEmpty()) {
            throw new IllegalArgumentException("label must not be empty");
        }
        for (int index = 0; index < label.length(); index++) {
            char item = label.charAt(index);
            if (!Character.isLetterOrDigit(item) && item != '-' && item != '_') {
                throw new IllegalArgumentException("label contains a protocol delimiter: " + label);
            }
        }
    }
}
