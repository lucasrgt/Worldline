package worldline.api.scenario;

import java.util.Objects;

/** Stable causal evidence for a lightning-powered creeper identity. */
public final class PoweredCreeperEvidence {
    private final int cellX;
    private final int cellY;
    private final int cellZ;
    private final boolean initiallyUnpowered;
    private final boolean strikeObserved;
    private final boolean strikeAtCreeper;
    private final boolean identityPreserved;
    private final boolean powered;
    private final boolean heldPowered;

    private PoweredCreeperEvidence(int cellX, int cellY, int cellZ,
            boolean initiallyUnpowered, boolean strikeObserved,
            boolean strikeAtCreeper, boolean identityPreserved,
            boolean powered, boolean heldPowered) {
        this.cellX = cellX;
        this.cellY = cellY;
        this.cellZ = cellZ;
        this.initiallyUnpowered = initiallyUnpowered;
        this.strikeObserved = strikeObserved;
        this.strikeAtCreeper = strikeAtCreeper;
        this.identityPreserved = identityPreserved;
        this.powered = powered;
        this.heldPowered = heldPowered;
    }

    public static PoweredCreeperEvidence capture(Trial trial, CreeperState held) {
        if (trial == null || held == null) {
            throw new IllegalArgumentException("missing powered-creeper observation");
        }
        CreeperState before = trial.before();
        CreeperState prerequisite = trial.prerequisite();
        CreeperState after = trial.after();
        LightningStrike strike = trial.strike();
        require(!before.powered() && !prerequisite.powered(),
                "creeper was powered before the observed strike");
        require(before.sameIdentityAndCell(prerequisite),
                "creeper prerequisite identity or cell drifted");
        require(strike.joined() && strike.alive()
                        && strike.entityId() != before.entityId()
                        && strike.cellX() == before.cellX()
                        && strike.cellY() == before.cellY()
                        && strike.cellZ() == before.cellZ(),
                "lightning strike was not observed at the creeper cell");
        require(after.powered() && before.sameIdentityAndCell(after),
                "same creeper did not become powered");
        require(held.powered() && after.sameIdentityAndCell(held),
                "powered creeper state did not survive the observation tick");
        return new PoweredCreeperEvidence(before.cellX(), before.cellY(), before.cellZ(),
                true, true, true, true, true, true);
    }

    public int cellX() { return cellX; }
    public int cellY() { return cellY; }
    public int cellZ() { return cellZ; }
    public boolean initiallyUnpowered() { return initiallyUnpowered; }
    public boolean strikeObserved() { return strikeObserved; }
    public boolean strikeAtCreeper() { return strikeAtCreeper; }
    public boolean identityPreserved() { return identityPreserved; }
    public boolean powered() { return powered; }
    public boolean heldPowered() { return heldPowered; }

    public int[] flattened() {
        return new int[] {
            initiallyUnpowered ? 1 : 0,
            strikeObserved ? 1 : 0,
            strikeAtCreeper ? 1 : 0,
            identityPreserved ? 1 : 0,
            powered ? 1 : 0,
            heldPowered ? 1 : 0
        };
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof PoweredCreeperEvidence)) {
            return false;
        }
        PoweredCreeperEvidence value = (PoweredCreeperEvidence) other;
        return cellX == value.cellX && cellY == value.cellY && cellZ == value.cellZ
                && initiallyUnpowered == value.initiallyUnpowered
                && strikeObserved == value.strikeObserved
                && strikeAtCreeper == value.strikeAtCreeper
                && identityPreserved == value.identityPreserved
                && powered == value.powered && heldPowered == value.heldPowered;
    }

    @Override public int hashCode() {
        return Objects.hash(cellX, cellY, cellZ, initiallyUnpowered, strikeObserved,
                strikeAtCreeper, identityPreserved, powered, heldPowered);
    }

    public static final class CreeperState {
        private final int entityId;
        private final int cellX;
        private final int cellY;
        private final int cellZ;
        private final boolean powered;

        public CreeperState(int entityId, int cellX, int cellY, int cellZ, boolean powered) {
            if (entityId < 0 || cellY < 0 || cellY >= 128) {
                throw new IllegalArgumentException("invalid creeper state");
            }
            this.entityId = entityId;
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellZ = cellZ;
            this.powered = powered;
        }

        public int entityId() { return entityId; }
        public int cellX() { return cellX; }
        public int cellY() { return cellY; }
        public int cellZ() { return cellZ; }
        public boolean powered() { return powered; }

        boolean sameIdentityAndCell(CreeperState other) {
            return other != null && entityId == other.entityId
                    && cellX == other.cellX && cellY == other.cellY && cellZ == other.cellZ;
        }
    }

    public static final class LightningStrike {
        private final int entityId;
        private final int cellX;
        private final int cellY;
        private final int cellZ;
        private final boolean joined;
        private final boolean alive;

        public LightningStrike(int entityId, int cellX, int cellY, int cellZ,
                boolean joined, boolean alive) {
            if (entityId < 0 || cellY < 0 || cellY >= 128) {
                throw new IllegalArgumentException("invalid lightning strike");
            }
            this.entityId = entityId;
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellZ = cellZ;
            this.joined = joined;
            this.alive = alive;
        }

        public int entityId() { return entityId; }
        public int cellX() { return cellX; }
        public int cellY() { return cellY; }
        public int cellZ() { return cellZ; }
        public boolean joined() { return joined; }
        public boolean alive() { return alive; }
    }

    public static final class Trial {
        private final CreeperState before;
        private final CreeperState prerequisite;
        private final LightningStrike strike;
        private final CreeperState after;

        public Trial(CreeperState before, CreeperState prerequisite,
                LightningStrike strike, CreeperState after) {
            this.before = Objects.requireNonNull(before, "before");
            this.prerequisite = Objects.requireNonNull(prerequisite, "prerequisite");
            this.strike = Objects.requireNonNull(strike, "strike");
            this.after = Objects.requireNonNull(after, "after");
        }

        public CreeperState before() { return before; }
        public CreeperState prerequisite() { return prerequisite; }
        public LightningStrike strike() { return strike; }
        public CreeperState after() { return after; }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
