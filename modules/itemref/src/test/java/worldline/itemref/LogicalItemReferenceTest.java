package worldline.itemref;

public final class LogicalItemReferenceTest {
    private static final String VALUE = "minecraft:0123456789abcdef0123456789abcdef|"
            + "fedcba9876543210fedcba9876543210|betaenergistics.storage-cell/2";

    public static void main(String[] arguments) {
        LogicalItemReference reference = LogicalItemReference.parse(VALUE);
        require(VALUE.equals(reference.canonical()), "canonical value");
        require(reference.equals(LogicalItemReference.parse(VALUE)), "value equality");
        require(reference.hashCode() == LogicalItemReference.parse(VALUE).hashCode(), "value hash");
        reject(null); reject(" " + VALUE); reject(VALUE.toUpperCase());
        reject(VALUE.replace("minecraft:", "other:"));
        reject(VALUE.replace("storage-cell/2", "storage_cell/2"));
        reject(VALUE.replace("/2", "/0"));
        System.out.println("LogicalItemReferenceTest passed");
    }

    private static void reject(String value) {
        try {
            LogicalItemReference.parse(value);
            throw new AssertionError("accepted malformed reference: " + value);
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
