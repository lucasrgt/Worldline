package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Reusable evidence boundary for a wall torch losing its supporting block. */
public final class TorchSupportBreakFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private TorchSupportBreakFixture() { }

    public static Evidence observe(BlockState wallTorch, BlockState supportBefore,
            BlockState torchAfter, RemoteItemStack drop, BlockState torchPersisted) {
        if (wallTorch == null || supportBefore == null || torchAfter == null
                || drop == null || torchPersisted == null)
            throw new IllegalArgumentException("null torch support-break evidence");
        require(wallTorch.legacyId() == 50 && wallTorch.metadata() >= 1
                && wallTorch.metadata() <= 4, "placed torch was not wall-mounted");
        require(supportBefore.legacyId() != 0, "support cell was already air");
        require(torchAfter.equals(AIR), "wall torch did not pop to air");
        require(drop.legacyId() == 50 && drop.count() == 1,
                "support break did not drop exactly one torch item");
        require(torchPersisted.equals(AIR), "popped wall-torch cell did not persist as air");
        return new Evidence(wallTorch.metadata(), supportBefore.legacyId(),
                drop.damage(), torchPersisted);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final int torchMetadata, supportId, dropDamage;
        private final BlockState persisted;
        Evidence(int torchMetadata, int supportId, int dropDamage, BlockState persisted) {
            this.torchMetadata = torchMetadata; this.supportId = supportId;
            this.dropDamage = dropDamage; this.persisted = persisted;
        }
        public int torchMetadata() { return torchMetadata; }
        public int supportId() { return supportId; }
        public int dropDamage() { return dropDamage; }
        public BlockState persisted() { return persisted; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return torchMetadata == value.torchMetadata && supportId == value.supportId
                    && dropDamage == value.dropDamage && persisted.equals(value.persisted);
        }
        @Override public int hashCode() {
            return Objects.hash(torchMetadata, supportId, dropDamage, persisted);
        }
    }
}
