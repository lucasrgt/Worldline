import java.security.MessageDigest;

/** Runner-local hashing avoids package-private harness methods across class loaders. */
final class M793Digest {
    private M793Digest() {}
    static String sha256Hex(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder();
        for (byte value : digest) result.append(String.format("%02x", value & 255));
        return result.toString();
    }
}
