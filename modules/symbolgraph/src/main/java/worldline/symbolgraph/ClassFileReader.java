package worldline.symbolgraph;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Minimal fail-closed class-file symbol reader; method bodies are never decoded. */
final class ClassFileReader {
    List<OfficialSymbolKey> read(InputStream input) throws IOException {
        DataInputStream data = new DataInputStream(input);
        if (data.readInt() != 0xCAFEBABE) throw new IllegalArgumentException("invalid class magic");
        data.readUnsignedShort();
        data.readUnsignedShort();
        int count = data.readUnsignedShort();
        String[] utf8 = new String[count];
        int[] classes = new int[count];
        for (int index = 1; index < count; index++) {
            int tag = data.readUnsignedByte();
            switch (tag) {
                case 1: utf8[index] = data.readUTF(); break;
                case 3: case 4: data.readInt(); break;
                case 5: case 6: data.readLong(); index++; break;
                case 7: classes[index] = data.readUnsignedShort(); break;
                case 8: case 16: case 19: case 20: data.readUnsignedShort(); break;
                case 9: case 10: case 11: case 12: case 17: case 18:
                    data.readUnsignedShort(); data.readUnsignedShort(); break;
                case 15: data.readUnsignedByte(); data.readUnsignedShort(); break;
                default: throw new IllegalArgumentException("unsupported constant-pool tag: " + tag);
            }
        }
        data.readUnsignedShort();
        int thisClass = data.readUnsignedShort();
        data.readUnsignedShort();
        String owner = utf8[classes[thisClass]];
        if (owner == null || owner.isEmpty()) throw new IllegalArgumentException("missing class name");
        int interfaces = data.readUnsignedShort();
        for (int index = 0; index < interfaces; index++) data.readUnsignedShort();
        List<OfficialSymbolKey> symbols = new ArrayList<OfficialSymbolKey>();
        symbols.add(new OfficialSymbolKey(SymbolKind.CLASS, "", owner, ""));
        readMembers(data, utf8, owner, SymbolKind.FIELD, symbols);
        readMembers(data, utf8, owner, SymbolKind.METHOD, symbols);
        skipAttributes(data);
        return symbols;
    }

    private static void readMembers(DataInputStream data, String[] utf8, String owner,
            SymbolKind kind, List<OfficialSymbolKey> symbols) throws IOException {
        int count = data.readUnsignedShort();
        for (int index = 0; index < count; index++) {
            data.readUnsignedShort();
            String name = utf8[data.readUnsignedShort()];
            String descriptor = utf8[data.readUnsignedShort()];
            if (name == null || descriptor == null) throw new IllegalArgumentException("invalid member name");
            symbols.add(new OfficialSymbolKey(kind, owner, name, descriptor));
            skipAttributes(data);
        }
    }

    private static void skipAttributes(DataInputStream data) throws IOException {
        int count = data.readUnsignedShort();
        for (int index = 0; index < count; index++) {
            data.readUnsignedShort();
            long length = Integer.toUnsignedLong(data.readInt());
            long skipped = data.skip(length);
            while (skipped < length) {
                long next = data.skip(length - skipped);
                if (next <= 0) throw new IllegalArgumentException("truncated class attribute");
                skipped += next;
            }
        }
    }
}
