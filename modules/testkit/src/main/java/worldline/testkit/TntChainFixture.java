package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteObjectSpawn;

/** Reusable evidence boundary for one primed TNT igniting a neighboring charge. */
public final class TntChainFixture {
    private TntChainFixture() { }

    public static Evidence observe(BlockPosition first, BlockPosition second,
            RemoteObjectSpawn direct, RemoteExplosion directExplosion,
            RemoteObjectSpawn chained, RemoteExplosion chainedExplosion,
            BlockState firstAfter, BlockState secondAfter) {
        if (first == null || second == null || direct == null || directExplosion == null
                || chained == null || chainedExplosion == null || firstAfter == null
                || secondAfter == null) throw new IllegalArgumentException("null TNT chain evidence");
        require(distance(first, second) == 1, "TNT charges were not adjacent");
        require(direct.type() == 50 && chained.type() == 50
                && direct.entityId() != chained.entityId(), "distinct primed TNT objects required");
        require(direct.throwerId() == 0 && chained.throwerId() == 0,
                "primed TNT thrower drifted");
        require(directExplosion.strength() == 4F && chainedExplosion.strength() == 4F,
                "TNT chain strength drifted");
        require(near(directExplosion, first) && near(chained, second),
                "TNT chain centers drifted");
        require(firstAfter.equals(new BlockState(0, 0))
                && secondAfter.equals(new BlockState(0, 0)), "TNT chain did not clear both charges");
        return new Evidence(2, 50, 4, true, true);
    }

    private static int distance(BlockPosition a, BlockPosition b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z());
    }
    private static boolean near(RemoteExplosion explosion, BlockPosition charge) {
        return Math.abs(explosion.x() - (charge.x() + 0.5D)) < 2D
                && Math.abs(explosion.y() - (charge.y() + 0.5D)) < 4D
                && Math.abs(explosion.z() - (charge.z() + 0.5D)) < 2D;
    }
    private static boolean near(RemoteObjectSpawn object, BlockPosition charge) {
        return Math.abs(object.x() - (charge.x() + 0.5D)) < 2D
                && Math.abs(object.y() - (charge.y() + 0.5D)) < 4D
                && Math.abs(object.z() - (charge.z() + 0.5D)) < 2D;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final int primedObjects, objectType, strength;
        private final boolean adjacent, bothAir;
        Evidence(int primedObjects, int objectType, int strength, boolean adjacent, boolean bothAir) {
            this.primedObjects = primedObjects; this.objectType = objectType;
            this.strength = strength; this.adjacent = adjacent; this.bothAir = bothAir;
        }
        public int primedObjects() { return primedObjects; }
        public int objectType() { return objectType; }
        public int strength() { return strength; }
        public boolean adjacent() { return adjacent; }
        public boolean bothAir() { return bothAir; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false; Evidence value = (Evidence) other;
            return primedObjects == value.primedObjects && objectType == value.objectType
                    && strength == value.strength && adjacent == value.adjacent
                    && bothAir == value.bothAir;
        }
        @Override public int hashCode() {
            return Objects.hash(primedObjects, objectType, strength, adjacent, bothAir);
        }
    }
}
