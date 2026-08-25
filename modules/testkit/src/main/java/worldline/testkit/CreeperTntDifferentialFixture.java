package worldline.testkit;

import java.util.Objects;

/** Reusable normalized evidence for official creeper and TNT explosion strengths. */
public final class CreeperTntDifferentialFixture {
    private CreeperTntDifferentialFixture() { }

    public static Evidence observe(float creeperStrength, float tntStrength) {
        require(creeperStrength == 3F, "creeper explosion strength drifted");
        require(tntStrength == 4F, "TNT explosion strength drifted");
        require(tntStrength > creeperStrength, "explosion strength ordering drifted");
        return new Evidence(3, 4, 1, true);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final int creeperStrength, tntStrength, delta;
        private final boolean tntStronger;
        Evidence(int creeperStrength, int tntStrength, int delta, boolean tntStronger) {
            this.creeperStrength = creeperStrength; this.tntStrength = tntStrength;
            this.delta = delta; this.tntStronger = tntStronger;
        }
        public int creeperStrength() { return creeperStrength; }
        public int tntStrength() { return tntStrength; }
        public int delta() { return delta; }
        public boolean tntStronger() { return tntStronger; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return creeperStrength == value.creeperStrength && tntStrength == value.tntStrength
                    && delta == value.delta && tntStronger == value.tntStronger;
        }
        @Override public int hashCode() {
            return Objects.hash(creeperStrength, tntStrength, delta, tntStronger);
        }
    }
}
