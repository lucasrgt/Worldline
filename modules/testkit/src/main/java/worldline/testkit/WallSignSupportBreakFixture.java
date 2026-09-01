package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Reusable evidence boundary for a wall sign popped after its support block is removed. */
public final class WallSignSupportBreakFixture {
    private static final BlockState STONE = new BlockState(1, 0);
    private static final BlockState WALL_SIGN = new BlockState(68, 5);
    private static final BlockState AIR = new BlockState(0, 0);
    private static final RemoteItemStack SIGN_DROP = new RemoteItemStack(323, 1, 0);

    private WallSignSupportBreakFixture() { }

    public static Evidence observe(BlockState supportBefore, BlockState signBefore,
            BlockState supportAfter, BlockState signAfter, RemoteItemStack drop,
            BlockState persistedSign) {
        require(STONE.equals(supportBefore), "initial support was not stone 1:0");
        require(WALL_SIGN.equals(signBefore), "initial attachment was not east wall sign 68:5");
        require(AIR.equals(supportAfter), "support did not become air after Packet14");
        require(AIR.equals(signAfter), "unsupported wall sign did not pop to air");
        require(SIGN_DROP.equals(drop), "unsupported wall sign did not drop sign item 323");
        require(AIR.equals(persistedSign), "popped wall sign did not remain air after login");
        return new Evidence(supportBefore, signBefore, supportAfter, signAfter, drop, persistedSign);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final BlockState supportBefore, signBefore, supportAfter, signAfter, persistedSign;
        private final RemoteItemStack drop;

        Evidence(BlockState supportBefore, BlockState signBefore, BlockState supportAfter,
                BlockState signAfter, RemoteItemStack drop, BlockState persistedSign) {
            this.supportBefore = supportBefore;
            this.signBefore = signBefore;
            this.supportAfter = supportAfter;
            this.signAfter = signAfter;
            this.drop = drop;
            this.persistedSign = persistedSign;
        }

        public BlockState supportBefore() { return supportBefore; }
        public BlockState signBefore() { return signBefore; }
        public BlockState supportAfter() { return supportAfter; }
        public BlockState signAfter() { return signAfter; }
        public RemoteItemStack drop() { return drop; }
        public BlockState persistedSign() { return persistedSign; }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return supportBefore.equals(value.supportBefore) && signBefore.equals(value.signBefore)
                    && supportAfter.equals(value.supportAfter) && signAfter.equals(value.signAfter)
                    && drop.equals(value.drop) && persistedSign.equals(value.persistedSign);
        }

        @Override public int hashCode() {
            return Objects.hash(supportBefore, signBefore, supportAfter, signAfter, drop, persistedSign);
        }
    }
}
