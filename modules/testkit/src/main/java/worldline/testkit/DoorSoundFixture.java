package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteWorldEvent;

/** Reusable evidence boundary for a wooden-door toggle and its server world event. */
public final class DoorSoundFixture {
    private static final BlockState CLOSED_LOWER = new BlockState(64, 0);
    private static final BlockState CLOSED_UPPER = new BlockState(64, 8);
    private static final BlockState OPEN_LOWER = new BlockState(64, 4);
    private static final BlockState OPEN_UPPER = new BlockState(64, 12);
    private DoorSoundFixture() { }

    public static Evidence observe(BlockPosition lower, BlockState beforeLower,
            BlockState beforeUpper, BlockState afterLower, BlockState afterUpper,
            RemoteWorldEvent event) {
        if (lower == null || beforeLower == null || beforeUpper == null || afterLower == null
                || afterUpper == null || event == null)
            throw new IllegalArgumentException("null door sound evidence");
        require(beforeLower.equals(CLOSED_LOWER) && beforeUpper.equals(CLOSED_UPPER),
                "wooden door did not begin closed");
        require(afterLower.equals(OPEN_LOWER) && afterUpper.equals(OPEN_UPPER),
                "wooden door did not finish open");
        require(event.effectId() == 1003 && event.data() == 0 && event.position().equals(lower),
                "wooden door Packet61 effect drifted");
        return new Evidence(beforeLower, beforeUpper, afterLower, afterUpper,
                event.effectId(), event.data(), event.position().equals(lower));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final BlockState beforeLower, beforeUpper, afterLower, afterUpper;
        private final int effectId, data;
        private final boolean lowerHalf;
        Evidence(BlockState beforeLower, BlockState beforeUpper, BlockState afterLower,
                BlockState afterUpper, int effectId, int data, boolean lowerHalf) {
            this.beforeLower = beforeLower; this.beforeUpper = beforeUpper;
            this.afterLower = afterLower; this.afterUpper = afterUpper;
            this.effectId = effectId; this.data = data; this.lowerHalf = lowerHalf;
        }
        public BlockState beforeLower() { return beforeLower; }
        public BlockState beforeUpper() { return beforeUpper; }
        public BlockState afterLower() { return afterLower; }
        public BlockState afterUpper() { return afterUpper; }
        public int effectId() { return effectId; }
        public int data() { return data; }
        public boolean lowerHalf() { return lowerHalf; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return effectId == value.effectId && data == value.data && lowerHalf == value.lowerHalf
                    && beforeLower.equals(value.beforeLower) && beforeUpper.equals(value.beforeUpper)
                    && afterLower.equals(value.afterLower) && afterUpper.equals(value.afterUpper);
        }
        @Override public int hashCode() {
            return Objects.hash(beforeLower, beforeUpper, afterLower, afterUpper,
                    effectId, data, lowerHalf);
        }
    }
}
