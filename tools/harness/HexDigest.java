import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Single SHA-256 helper for harness code. New copies are rejected by DuplicatePatternCheck. */
public final class HexDigest {
    private HexDigest() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length == 2 && "--sha256-text".equals(arguments[0])) {
            System.out.println(sha256Hex(arguments[1].getBytes(StandardCharsets.UTF_8)));
            return;
        }
        throw new IllegalArgumentException("usage: HexDigest --sha256-text VALUE");
    }

    static MessageDigest sha256() throws Exception {
        return MessageDigest.getInstance("SHA-256");
    }

    static String hex(byte[] digest) {
        return HexFormat.of().formatHex(digest);
    }

    static String sha256Hex(byte[] payload) throws Exception {
        if (payload == null) throw new IllegalArgumentException("null digest payload");
        return hex(sha256().digest(payload));
    }

    static void selfTest() throws Exception {
        String empty = sha256Hex(new byte[0]);
        String abc = sha256Hex("abc".getBytes(StandardCharsets.UTF_8));
        require(empty.equals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                "SHA-256 empty-vector drift");
        require(abc.equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),
                "SHA-256 abc-vector drift");
        System.out.println("  hex digest self-test: passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
