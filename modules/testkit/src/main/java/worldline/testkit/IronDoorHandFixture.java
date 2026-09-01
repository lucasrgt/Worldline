package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Reusable evidence boundary for rejected hand activation of a closed iron door. */
public final class IronDoorHandFixture {
    private static final BlockState CLOSED_LOWER = new BlockState(71, 0);
    private static final BlockState CLOSED_UPPER = new BlockState(71, 8);
    private IronDoorHandFixture() { }

    public static Evidence observe(BlockPosition lower, BlockState placedLower,
            BlockState placedUpper, BlockState afterLowerHandLower, BlockState afterLowerHandUpper,
            BlockState afterUpperHandLower, BlockState afterUpperHandUpper) {
        if (lower == null || placedLower == null || placedUpper == null
                || afterLowerHandLower == null || afterLowerHandUpper == null
                || afterUpperHandLower == null || afterUpperHandUpper == null)
            throw new IllegalArgumentException("null iron door hand evidence");
        require(placedLower.equals(CLOSED_LOWER) && placedUpper.equals(CLOSED_UPPER),
                "iron door did not begin closed");
        require(afterLowerHandLower.equals(CLOSED_LOWER)
                && afterLowerHandUpper.equals(CLOSED_UPPER),
                "lower-half hand activation changed the iron door");
        require(afterUpperHandLower.equals(CLOSED_LOWER)
                && afterUpperHandUpper.equals(CLOSED_UPPER),
                "upper-half hand activation changed the iron door");
        return new Evidence(placedLower, placedUpper, afterLowerHandLower, afterLowerHandUpper,
                afterUpperHandLower, afterUpperHandUpper);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final BlockState placedLower, placedUpper;
        private final BlockState afterLowerHandLower, afterLowerHandUpper;
        private final BlockState afterUpperHandLower, afterUpperHandUpper;
        Evidence(BlockState placedLower, BlockState placedUpper, BlockState afterLowerHandLower,
                BlockState afterLowerHandUpper, BlockState afterUpperHandLower,
                BlockState afterUpperHandUpper) {
            this.placedLower = placedLower; this.placedUpper = placedUpper;
            this.afterLowerHandLower = afterLowerHandLower;
            this.afterLowerHandUpper = afterLowerHandUpper;
            this.afterUpperHandLower = afterUpperHandLower;
            this.afterUpperHandUpper = afterUpperHandUpper;
        }
        public boolean handsRejected() {
            return afterLowerHandLower.equals(CLOSED_LOWER)
                    && afterLowerHandUpper.equals(CLOSED_UPPER)
                    && afterUpperHandLower.equals(CLOSED_LOWER)
                    && afterUpperHandUpper.equals(CLOSED_UPPER);
        }
        public boolean preservedHalves() {
            return placedLower.equals(CLOSED_LOWER) && placedUpper.equals(CLOSED_UPPER);
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return placedLower.equals(value.placedLower) && placedUpper.equals(value.placedUpper)
                    && afterLowerHandLower.equals(value.afterLowerHandLower)
                    && afterLowerHandUpper.equals(value.afterLowerHandUpper)
                    && afterUpperHandLower.equals(value.afterUpperHandLower)
                    && afterUpperHandUpper.equals(value.afterUpperHandUpper);
        }
        @Override public int hashCode() {
            return Objects.hash(placedLower, placedUpper, afterLowerHandLower,
                    afterLowerHandUpper, afterUpperHandLower, afterUpperHandUpper);
        }
    }
}
