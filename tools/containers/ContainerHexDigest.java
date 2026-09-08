import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Module-local SHA-256 factory for the isolated runtime executor toolset. */
final class ContainerHexDigest {
    private ContainerHexDigest() { }

    static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
