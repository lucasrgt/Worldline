package worldline.testkit;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import worldline.api.RemoteMobSpawn;

/** Bounded contract for a distinct, spatially coherent natural Beta wolf pack. */
public final class NaturalWolfPackFixture {
    private static final int WOLF = 95;
    private static final double MAXIMUM_PAIR_DISTANCE_SQUARED = 48D * 48D;
    private NaturalWolfPackFixture() { }

    public static Evidence await(int maximumAttempts, Attempt attempt) {
        if (maximumAttempts < 1 || attempt == null)
            throw new IllegalArgumentException("invalid natural wolf attempt boundary");
        for (int index = 1; index <= maximumAttempts; index++) {
            List<RemoteMobSpawn> pack = attempt.observe(index);
            if (qualifies(pack)) return new Evidence(maximumAttempts, 2, 8);
        }
        throw new IllegalStateException("natural wolf pack absent after bounded attempts");
    }

    private static boolean qualifies(List<RemoteMobSpawn> pack) {
        if (pack == null || pack.size() < 2 || pack.size() > 8) return false;
        Set<Integer> ids = new HashSet<>();
        for (RemoteMobSpawn wolf : pack) {
            if (wolf == null || wolf.legacyType() != WOLF || !ids.add(wolf.entityId())) return false;
        }
        for (RemoteMobSpawn left : pack) for (RemoteMobSpawn right : pack) {
            double dx = left.x() - right.x(), dz = left.z() - right.z();
            if (dx * dx + dz * dz > MAXIMUM_PAIR_DISTANCE_SQUARED) return false;
        }
        return true;
    }

    @FunctionalInterface public interface Attempt {
        List<RemoteMobSpawn> observe(int attempt);
    }

    public static final class Evidence {
        private final int maximumAttempts, minimumPackSize, maximumPackSize;
        Evidence(int maximumAttempts, int minimumPackSize, int maximumPackSize) {
            this.maximumAttempts = maximumAttempts; this.minimumPackSize = minimumPackSize;
            this.maximumPackSize = maximumPackSize;
        }
        public int maximumAttempts() { return maximumAttempts; }
        public int minimumPackSize() { return minimumPackSize; }
        public int maximumPackSize() { return maximumPackSize; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return maximumAttempts == value.maximumAttempts
                    && minimumPackSize == value.minimumPackSize
                    && maximumPackSize == value.maximumPackSize;
        }
        @Override public int hashCode() {
            return Objects.hash(maximumAttempts, minimumPackSize, maximumPackSize);
        }
    }
}
