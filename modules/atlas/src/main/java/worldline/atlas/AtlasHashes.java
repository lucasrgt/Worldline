package worldline.atlas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** SHA-256 helpers for canonical Atlas documents. */
public final class AtlasHashes {
    private AtlasHashes() {}

    public static String sha256(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] value) {
        if (value == null) throw new NullPointerException("digest");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) hex.append(String.format("%02x", item & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
}
