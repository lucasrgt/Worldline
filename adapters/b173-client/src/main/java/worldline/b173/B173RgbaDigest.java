package worldline.b173;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

/** Shared byte-exact digest for native RGBA frames and their canonical family documents. */
public final class B173RgbaDigest {
    private B173RgbaDigest() { }

    public static String hash(ByteBuffer bytes) throws Exception {
        ByteBuffer copy = bytes.duplicate(); copy.clear();
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); digest.update(copy);
        StringBuilder value = new StringBuilder();
        for (byte item : digest.digest()) value.append(String.format("%02x", item & 255));
        return value.toString();
    }
}
