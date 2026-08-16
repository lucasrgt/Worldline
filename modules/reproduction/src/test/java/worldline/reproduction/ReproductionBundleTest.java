package worldline.reproduction;

import java.nio.charset.StandardCharsets;
import worldline.api.RuntimeSnapshot;

public final class ReproductionBundleTest {
    private static final String CLIENT = repeat('a', 64), TOOLCHAIN = repeat('b', 40);
    private ReproductionBundleTest() {}

    public static void main(String[] arguments) {
        roundTripIsCanonicalAndImmutable();
        invalidInputFailsClosed();
        System.out.println("ReproductionBundleTest passed");
    }

    private static void roundTripIsCanonicalAndImmutable() {
        ReproductionBundle bundle = ReproductionBundle.create("test-runtime", "1.2.3", CLIENT,
                TOOLCHAIN, RuntimeSnapshot.of("snapshot".getBytes(StandardCharsets.UTF_8)));
        byte[] bytes = bundle.bytes(); ReproductionBundle parsed = ReproductionBundle.parse(bytes);
        bytes[0] = 0;
        require(bundle.equals(parsed) && bundle.hashCode() == parsed.hashCode(), "bundle equality failed");
        require(parsed.runtimeId().equals("test-runtime") && parsed.worldlineVersion().equals("1.2.3")
                && parsed.clientSha256().equals(CLIENT) && parsed.toolchainRevision().equals(TOOLCHAIN)
                && parsed.sha256().length() == 64 && parsed.size() == parsed.bytes().length,
                "bundle fields failed");
    }

    private static void invalidInputFailsClosed() {
        failure(() -> ReproductionBundle.parse(new byte[0]));
        failure(() -> ReproductionBundle.create("Bad Runtime", "1.2.3", CLIENT, TOOLCHAIN,
                RuntimeSnapshot.of(new byte[] {1})));
        ReproductionBundle bundle = ReproductionBundle.create("test-runtime", "1.2.3", CLIENT,
                TOOLCHAIN, RuntimeSnapshot.of(new byte[] {1, 2, 3}));
        byte[] corrupt = bundle.bytes(); corrupt[corrupt.length - 10] ^= 1;
        failure(() -> ReproductionBundle.parse(corrupt));
    }

    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected bundle failure"); }
        catch (IllegalArgumentException expected) { }
    }
    private static String repeat(char value, int count) {
        StringBuilder result = new StringBuilder(); while (result.length() < count) result.append(value);
        return result.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
