package worldline.api;

public final class DomainApiTest {
    private DomainApiTest() {}

    public static void main(String[] arguments) {
        valueEqualityIsExact();
        invalidValuesFailClosed();
        snapshotsAreBoundedAndImmutable();
        System.out.println("DomainApiTest passed");
    }

    private static void valueEqualityIsExact() {
        equal(new BlockPosition(1, 2, 3), new BlockPosition(1, 2, 3), "block position");
        equal(new BlockState(20, 7), new BlockState(20, 7), "block state");
        equal(new GamePosition(1.25D, 2.5D, -3.75D),
                new GamePosition(1.25D, 2.5D, -3.75D), "game position");
    }

    private static void invalidValuesFailClosed() {
        failure(() -> new BlockState(-1, 0));
        failure(() -> new BlockState(1, 16));
        failure(() -> new GamePosition(Double.NaN, 0.0D, 0.0D));
        failure(() -> new GamePosition(0.0D, Double.POSITIVE_INFINITY, 0.0D));
    }

    private static void snapshotsAreBoundedAndImmutable() {
        byte[] source = new byte[] {1, 2, 3};
        RuntimeSnapshot snapshot = RuntimeSnapshot.of(source);
        source[0] = 9;
        byte[] copy = snapshot.bytes();
        copy[1] = 9;
        equal(RuntimeSnapshot.of(new byte[] {1, 2, 3}), snapshot, "runtime snapshot");
        if (snapshot.size() != 3 || snapshot.bytes()[0] != 1 || snapshot.bytes()[1] != 2
                || !snapshot.sha256().equals(
                        "039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81")) {
            throw new AssertionError("runtime snapshot immutability failed");
        }
        failure(() -> RuntimeSnapshot.of(new byte[0]));
        failure(() -> RuntimeSnapshot.of(new byte[RuntimeSnapshot.MAX_BYTES + 1]));
    }

    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected invalid value failure"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!expected.equals(actual) || expected.hashCode() != actual.hashCode()) {
            throw new AssertionError(label + " equality contract failed");
        }
    }
}
