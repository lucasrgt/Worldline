package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ordered gzip-NBT tree so level.dat edits preserve unrelated tags value-semantically. */
final class B173Nbt {
    private B173Nbt() {}

    static final class Compound {
        final Map<String, Object> entries = new LinkedHashMap<>();
    }

    static final class ListValue {
        final int type;
        final List<Object> items;
        ListValue(int type, List<Object> items) { this.type = type; this.items = items; }
    }

    static Compound read(DataInputStream input) throws IOException {
        if (input.readUnsignedByte() != 10) throw new IOException("level.dat root is not a compound");
        input.readUTF();
        return compound(input);
    }

    static void write(DataOutputStream output, Compound root) throws IOException {
        output.writeByte(10);
        output.writeUTF("");
        writeCompound(output, root);
    }

    private static Compound compound(DataInputStream input) throws IOException {
        Compound result = new Compound();
        while (true) {
            int type = input.readUnsignedByte();
            if (type == 0) return result;
            result.entries.put(input.readUTF(), payload(input, type));
        }
    }

    private static Object payload(DataInputStream input, int type) throws IOException {
        switch (type) {
            case 1: return input.readByte();
            case 2: return input.readShort();
            case 3: return input.readInt();
            case 4: return input.readLong();
            case 5: return input.readFloat();
            case 6: return input.readDouble();
            case 7: byte[] bytes = new byte[input.readInt()]; input.readFully(bytes); return bytes;
            case 8: return input.readUTF();
            case 9: return list(input);
            case 10: return compound(input);
            case 11: int[] ints = new int[input.readInt()];
                for (int index = 0; index < ints.length; index++) ints[index] = input.readInt();
                return ints;
            default: throw new IOException("unknown NBT tag " + type);
        }
    }

    private static ListValue list(DataInputStream input) throws IOException {
        int type = input.readUnsignedByte();
        int count = input.readInt();
        if (count < 0) throw new IOException("negative NBT list length");
        List<Object> items = new ArrayList<>(count);
        for (int index = 0; index < count; index++) items.add(payload(input, type));
        return new ListValue(type, items);
    }

    private static void writeCompound(DataOutputStream output, Compound compound) throws IOException {
        for (Map.Entry<String, Object> entry : compound.entries.entrySet()) {
            output.writeByte(typeOf(entry.getValue()));
            output.writeUTF(entry.getKey());
            writePayload(output, entry.getValue());
        }
        output.writeByte(0);
    }

    private static int typeOf(Object value) {
        if (value instanceof Byte) return 1;
        if (value instanceof Short) return 2;
        if (value instanceof Integer) return 3;
        if (value instanceof Long) return 4;
        if (value instanceof Float) return 5;
        if (value instanceof Double) return 6;
        if (value instanceof byte[]) return 7;
        if (value instanceof String) return 8;
        if (value instanceof ListValue) return 9;
        if (value instanceof Compound) return 10;
        if (value instanceof int[]) return 11;
        throw new IllegalArgumentException("unsupported NBT value " + value.getClass());
    }

    private static void writePayload(DataOutputStream output, Object value) throws IOException {
        if (value instanceof Byte) { output.writeByte((Byte) value); return; }
        if (value instanceof Short) { output.writeShort((Short) value); return; }
        if (value instanceof Integer) { output.writeInt((Integer) value); return; }
        if (value instanceof Long) { output.writeLong((Long) value); return; }
        if (value instanceof Float) { output.writeFloat((Float) value); return; }
        if (value instanceof Double) { output.writeDouble((Double) value); return; }
        if (value instanceof byte[]) { byte[] v = (byte[]) value; output.writeInt(v.length); output.write(v); return; }
        if (value instanceof String) { output.writeUTF((String) value); return; }
        if (value instanceof ListValue) { ListValue v = (ListValue) value;
            output.writeByte(v.type); output.writeInt(v.items.size());
            for (Object item : v.items) writePayload(output, item); return; }
        if (value instanceof Compound) { writeCompound(output, (Compound) value); return; }
        if (value instanceof int[]) { int[] v = (int[]) value; output.writeInt(v.length);
            for (int item : v) output.writeInt(item); return; }
        throw new IllegalArgumentException("unsupported NBT value " + value.getClass());
    }
}
