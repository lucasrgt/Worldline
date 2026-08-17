package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteChunkObservation;

/** Bounded payload skipper for qualified protocol-14 server packets. */
final class B173InboundPacket {
    private B173InboundPacket() {}

    static void skip(DataInputStream input, int packet) throws IOException {
        switch (packet) {
            case 0: break;
            case 4: bytes(input, 8); break;
            case 6: bytes(input, 12); break;
            case 8: bytes(input, 2); break;
            case 9: bytes(input, 1); break;
            case 10: bytes(input, 1); break;
            case 11: bytes(input, 33); break;
            case 12: bytes(input, 9); break;
            case 13: bytes(input, 41); break;
            case 17: bytes(input, 14); break;
            case 18: bytes(input, 5); break;
            case 21: bytes(input, 24); break;
            case 22: bytes(input, 8); break;
            case 23: bytes(input, 17); break;
            case 24: mob(input); break;
            case 25: painting(input); break;
            case 28: bytes(input, 10); break;
            case 29: bytes(input, 4); break;
            case 30: bytes(input, 4); break;
            case 31: bytes(input, 7); break;
            case 32: bytes(input, 6); break;
            case 33: bytes(input, 9); break;
            case 34: bytes(input, 18); break;
            case 38: bytes(input, 5); break;
            case 39: bytes(input, 8); break;
            case 40: bytes(input, 4); metadata(input); break;
            case 50: bytes(input, 9); break;
            case 51: chunk(input); break;
            case 52: multiBlock(input); break;
            case 53: bytes(input, 11); break;
            case 54: bytes(input, 12); break;
            case 60: explosion(input); break;
            case 61: bytes(input, 17); break;
            case 70: bytes(input, 1); break;
            case 71: bytes(input, 1); break;
            case 100: openWindow(input); break;
            case 101: bytes(input, 1); break;
            case 105: bytes(input, 5); break;
            case 106: bytes(input, 4); break;
            case 130: sign(input); break;
            case 131: mapData(input); break;
            case 200: bytes(input, 5); break;
            default: throw new IOException("unexpected inbound packet " + packet);
        }
    }

    static String string(DataInputStream input, int maximum) throws IOException {
        int length = input.readShort();
        if (length < 0 || length > maximum) throw new IOException("invalid string length " + length);
        StringBuilder value = new StringBuilder(length);
        for (int index = 0; index < length; index++) value.append(input.readChar());
        return value.toString();
    }

    static void string(DataOutputStream output, String value) throws IOException {
        output.writeShort(value.length());
        for (int index = 0; index < value.length(); index++) output.writeChar(value.charAt(index));
    }

    private static void mob(DataInputStream input) throws IOException {
        bytes(input, 19); metadata(input);
    }
    private static void painting(DataInputStream input) throws IOException {
        bytes(input, 4); string(input, 13); bytes(input, 13);
    }
    static RemoteChunkObservation chunk(DataInputStream input) throws IOException {
        return B173ChunkCodec.read(input).observation();
    }
    private static void multiBlock(DataInputStream input) throws IOException {
        bytes(input, 8); int count = input.readShort();
        if (count < 0 || count > 65535) throw new IOException("invalid multi-block count " + count);
        bytes(input, Math.multiplyExact(count, 4));
    }
    private static void explosion(DataInputStream input) throws IOException {
        bytes(input, 28); int count = input.readInt();
        boundedBytes(input, Math.multiplyExact(count, 3), 3_000_000);
    }
    private static void openWindow(DataInputStream input) throws IOException {
        bytes(input, 2); input.readUTF(); bytes(input, 1);
    }
    private static void item(DataInputStream input) throws IOException {
        short id = input.readShort(); if (id >= 0) bytes(input, 3);
    }
    private static void sign(DataInputStream input) throws IOException {
        bytes(input, 10); for (int index = 0; index < 4; index++) string(input, 15);
    }
    private static void mapData(DataInputStream input) throws IOException {
        bytes(input, 4); boundedBytes(input, input.readUnsignedByte(), 255);
    }
    private static void metadata(DataInputStream input) throws IOException {
        while (true) {
            int header = input.readUnsignedByte(); if (header == 127) return;
            switch (header >> 5) {
                case 0: bytes(input, 1); break;
                case 1: bytes(input, 2); break;
                case 2: bytes(input, 4); break;
                case 3: bytes(input, 4); break;
                case 4: string(input, 64); break;
                case 5: item(input); break;
                case 6: bytes(input, 12); break;
                default: throw new IOException("invalid metadata type " + (header >> 5));
            }
        }
    }
    private static void boundedBytes(DataInputStream input, int count, int maximum) throws IOException {
        if (count < 0 || count > maximum) throw new IOException("invalid payload length " + count);
        bytes(input, count);
    }
    private static void bytes(DataInputStream input, int count) throws IOException {
        for (int remaining = count; remaining > 0; ) {
            int skipped = input.skipBytes(remaining);
            if (skipped <= 0) throw new IOException("truncated inbound packet"); remaining -= skipped;
        }
    }
}
