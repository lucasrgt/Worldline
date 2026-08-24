package worldline.b173server;

import java.util.Arrays;
import worldline.api.RemoteMapContent;
import worldline.api.RemoteMapData;

/** Public Packet131 color-grid boundary for one stationary held-map session. */
public final class B173MapDataAccess {
    private static final int WIDTH = 128;
    private static final int QUIET_TICKS = 160;
    private B173MapDataAccess() { }

    public static void begin(B173WireClient client) {
        if (client == null) throw new IllegalArgumentException("null map client");
        client.channel().protocolTracker().reset();
    }

    public static RemoteMapContent observe(B173WireClient client, int maximumTicks) {
        if (client == null || maximumTicks < 320 || maximumTicks > 1200)
            throw new IllegalArgumentException("invalid map observation window");
        B173Protocol14Tracker tracker = client.channel().protocolTracker();
        Accumulator content = new Accumulator(); int quiet = 0;
        for (int tick = 0; tick < maximumTicks; tick++) {
            client.sustainTicks(1); boolean colors = false;
            for (RemoteMapData packet : tracker.drainMaps()) colors |= content.accept(packet);
            quiet = colors ? 0 : quiet + 1;
            if (content.complete() && quiet >= QUIET_TICKS) return content.snapshot();
        }
        throw new IllegalStateException("Packet131 color grid did not converge: "
                + content.columns + " columns, quiet=" + quiet);
    }

    private static final class Accumulator {
        private final byte[] colors = new byte[WIDTH * WIDTH];
        private final boolean[] seen = new boolean[WIDTH];
        private int columns, colorPackets, markerPackets;
        private boolean accept(RemoteMapData packet) {
            if (packet.itemId() != 358 || packet.mapId() != 0)
                throw new IllegalStateException("unexpected Packet131 map identity");
            byte[] payload = packet.payload();
            if (payload.length < 1) throw new IllegalStateException("empty Packet131 payload");
            int kind = payload[0] & 255;
            if (kind == 1) {
                if ((payload.length - 1) % 3 != 0)
                    throw new IllegalStateException("invalid Packet131 marker payload");
                markerPackets++; return false;
            }
            if (kind != 0 || payload.length < 4)
                throw new IllegalStateException("unsupported Packet131 payload kind " + kind);
            int column = payload[1] & 255, start = payload[2] & 255;
            int count = payload.length - 3;
            if (column >= WIDTH || start >= WIDTH || start + count > WIDTH)
                throw new IllegalStateException("invalid Packet131 color span");
            for (int index = 0; index < count; index++)
                colors[(start + index) * WIDTH + column] = payload[index + 3];
            if (!seen[column]) { seen[column] = true; columns++; }
            colorPackets++; return true;
        }
        private boolean complete() { return columns == WIDTH; }
        private RemoteMapContent snapshot() {
            return new RemoteMapContent(358, 0, columns, colorPackets,
                    markerPackets, Arrays.copyOf(colors, colors.length));
        }
    }
}
