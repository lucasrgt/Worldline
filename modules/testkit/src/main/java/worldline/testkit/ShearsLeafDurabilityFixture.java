package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Reusable evidence boundary for Beta shears harvesting a single oak-leaf block. */
public final class ShearsLeafDurabilityFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private ShearsLeafDurabilityFixture() { }

    public static Evidence harvest(RemoteItemStack shearsBefore, BlockState leafBefore,
            RemoteItemStack drop, BlockState leafAfter, RemoteItemStack shearsAfter) {
        if (shearsBefore == null || leafBefore == null || drop == null || leafAfter == null
                || shearsAfter == null)
            throw new IllegalArgumentException("null shears leaf durability evidence");
        require(shearsBefore.legacyId() == 359 && shearsBefore.count() == 1
                && shearsBefore.damage() == 0, "held shears did not start pristine");
        require(leafBefore.legacyId() == 18, "harvested cell was not an oak-leaf block");
        require(drop.legacyId() == 18 && drop.count() == 1 && drop.damage() == 0,
                "harvest did not emit one undamaged oak-leaf stack");
        require(AIR.equals(leafAfter), "harvested leaf cell did not become air");
        require(shearsAfter.legacyId() == 359 && shearsAfter.count() == 1,
                "harvest changed the held shear stack identity");
        require(shearsAfter.damage() == shearsBefore.damage() + 1,
                "one leaf harvest did not consume exactly one durability point");
        return new Evidence(leafBefore, drop, shearsBefore.damage(), shearsAfter.damage());
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final BlockState leaf;
        private final RemoteItemStack drop;
        private final int beforeDamage, afterDamage;
        Evidence(BlockState leaf, RemoteItemStack drop, int beforeDamage, int afterDamage) {
            this.leaf = leaf; this.drop = drop;
            this.beforeDamage = beforeDamage; this.afterDamage = afterDamage;
        }
        public BlockState leaf() { return leaf; }
        public RemoteItemStack drop() { return drop; }
        public int beforeDamage() { return beforeDamage; }
        public int afterDamage() { return afterDamage; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return leaf.equals(value.leaf) && drop.equals(value.drop)
                    && beforeDamage == value.beforeDamage && afterDamage == value.afterDamage;
        }
        @Override public int hashCode() {
            return Objects.hash(leaf, drop, beforeDamage, afterDamage);
        }
    }
}
