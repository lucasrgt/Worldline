package worldline.itemref;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ItemReferenceWireTest {
    private static final LogicalItemReference REFERENCE = LogicalItemReference.parse(
            "minecraft:0123456789abcdef0123456789abcdef|"
                    + "fedcba9876543210fedcba9876543210|betaenergistics.storage-cell/2");

    public static void main(String[] arguments) throws Exception {
        require(REFERENCE.equals(roundTrip(REFERENCE)), "reference round trip");
        require(roundTrip(null) == null, "absent round trip");
        rejectMalformed();
        System.out.println("ItemReferenceWireTest passed");
    }

    private static LogicalItemReference roundTrip(LogicalItemReference reference) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ItemReferenceWire.write(new DataOutputStream(bytes), reference);
        return ItemReferenceWire.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
    }

    private static void rejectMalformed() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeBoolean(true); output.writeUTF("not-a-reference");
        try {
            ItemReferenceWire.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
            throw new AssertionError("accepted malformed wire reference");
        } catch (IOException expected) {
            require(expected.getCause() instanceof IllegalArgumentException, "wire cause");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
