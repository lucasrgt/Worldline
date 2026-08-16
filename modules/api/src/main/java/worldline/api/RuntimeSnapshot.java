package worldline.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/** Immutable bounded byte representation of a durable runtime snapshot. */
public final class RuntimeSnapshot {
    public static final int MAX_BYTES = 1_048_576;
    private final byte[] bytes;

    private RuntimeSnapshot(byte[] bytes) { this.bytes = bytes; }

    public static RuntimeSnapshot of(byte[] bytes) {
        if (bytes == null) throw new NullPointerException("bytes");
        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("snapshot size must be 1.." + MAX_BYTES + " bytes");
        }
        return new RuntimeSnapshot(bytes.clone());
    }

    public byte[] bytes() { return bytes.clone(); }

    public int size() { return bytes.length; }

    public String sha256() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format("%02x", value & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    @Override public boolean equals(Object other) {
        return other instanceof RuntimeSnapshot
                && Arrays.equals(bytes, ((RuntimeSnapshot) other).bytes);
    }

    @Override public int hashCode() { return Arrays.hashCode(bytes); }

    @Override public String toString() {
        return "RuntimeSnapshot[size=" + size() + ",sha256=" + sha256() + "]";
    }
}
