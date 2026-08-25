package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Reusable evidence boundary for Beta bonemeal maturing planted wheat. */
public final class BonemealWheatFixture {
    private static final BlockState PLANTED = new BlockState(59, 0);
    private static final BlockState MATURE = new BlockState(59, 7);
    private BonemealWheatFixture() { }

    public static Evidence observe(BlockState before, RemoteItemStack catalyst,
            BlockState after, BlockState persisted) {
        if (before == null || catalyst == null || after == null || persisted == null)
            throw new IllegalArgumentException("null bonemeal wheat evidence");
        require(before.equals(PLANTED), "wheat did not begin at age zero");
        require(catalyst.legacyId() == 351 && catalyst.damage() == 15,
                "wheat catalyst was not Beta bonemeal");
        require(after.equals(MATURE), "bonemeal did not mature wheat");
        require(persisted.equals(MATURE), "mature wheat did not persist");
        return new Evidence(before, catalyst.legacyId(), catalyst.damage(), after, persisted);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final BlockState before, after, persisted;
        private final int itemId, damage;
        Evidence(BlockState before, int itemId, int damage, BlockState after,
                BlockState persisted) {
            this.before = before; this.itemId = itemId; this.damage = damage;
            this.after = after; this.persisted = persisted;
        }
        public BlockState before() { return before; }
        public int itemId() { return itemId; }
        public int damage() { return damage; }
        public BlockState after() { return after; }
        public BlockState persisted() { return persisted; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return itemId == value.itemId && damage == value.damage
                    && before.equals(value.before) && after.equals(value.after)
                    && persisted.equals(value.persisted);
        }
        @Override public int hashCode() {
            return Objects.hash(before, itemId, damage, after, persisted);
        }
    }
}
