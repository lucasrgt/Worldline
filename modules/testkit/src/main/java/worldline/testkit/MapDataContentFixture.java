package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.RemoteMapContent;

/** Reusable evidence boundary for deterministic protocol-14 held-map colors. */
public final class MapDataContentFixture {
    private MapDataContentFixture() { }

    public static Evidence observe(long seed, BlockPosition position, RemoteMapContent content) {
        if (position == null || content == null) throw new IllegalArgumentException("null map evidence");
        require(content.itemId() == 358 && content.mapId() == 0
                && content.observedColumns() == RemoteMapContent.WIDTH
                && content.colorPackets() >= RemoteMapContent.WIDTH,
                "Packet131 coverage drifted");
        require(content.nonZeroColors() > 0 && content.distinctColors() >= 3,
                "Packet131 color content drifted");
        String digest = content.colorsSha256();
        require(digest.matches("[0-9a-f]{64}"), "Packet131 color digest drifted");
        return new Evidence(seed, position, content.observedColumns(),
                content.nonZeroColors(), content.distinctColors(), digest);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final long seed;
        private final BlockPosition position;
        private final int columns, nonZero, palette;
        private final String colorsSha256;
        Evidence(long seed, BlockPosition position, int columns, int nonZero,
                int palette, String colorsSha256) {
            this.seed = seed; this.position = position; this.columns = columns;
            this.nonZero = nonZero; this.palette = palette; this.colorsSha256 = colorsSha256;
        }
        public long seed() { return seed; }
        public BlockPosition position() { return position; }
        public int columns() { return columns; }
        public int nonZero() { return nonZero; }
        public int palette() { return palette; }
        public String colorsSha256() { return colorsSha256; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return seed == value.seed && columns == value.columns && nonZero == value.nonZero
                    && palette == value.palette && position.equals(value.position)
                    && colorsSha256.equals(value.colorsSha256);
        }
        @Override public int hashCode() {
            return Objects.hash(seed, position, columns, nonZero, palette, colorsSha256);
        }
    }
}
