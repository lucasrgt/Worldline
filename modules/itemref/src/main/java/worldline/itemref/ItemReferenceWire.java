package worldline.itemref;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Strict opt-in wire suffix for a nullable logical item reference. */
public final class ItemReferenceWire {
    private ItemReferenceWire() {}

    public static void write(DataOutputStream output, LogicalItemReference reference)
            throws IOException {
        if (output == null) throw new IllegalArgumentException("output");
        output.writeBoolean(reference != null);
        if (reference != null) output.writeUTF(reference.canonical());
    }

    public static LogicalItemReference read(DataInputStream input) throws IOException {
        if (input == null) throw new IllegalArgumentException("input");
        if (!input.readBoolean()) return null;
        String value = input.readUTF();
        try {
            return LogicalItemReference.parse(value);
        } catch (IllegalArgumentException error) {
            IOException failure = new IOException("invalid logical item reference on wire");
            failure.initCause(error);
            throw failure;
        }
    }
}
