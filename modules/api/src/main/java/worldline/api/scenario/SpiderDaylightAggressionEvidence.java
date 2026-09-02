package worldline.api.scenario;

import java.util.Objects;

/** Equatable evidence for causal spider target selection across a light transition. */
public final class SpiderDaylightAggressionEvidence {
    private final int spiderCellX;
    private final int spiderCellY;
    private final int spiderCellZ;
    private final int playerCellX;
    private final int playerCellY;
    private final int playerCellZ;
    private final boolean daylightTargetAbsent;
    private final boolean nightTargetPlayer;
    private final boolean spiderIdentityPreserved;
    private final boolean playerIdentityPreserved;
    private final boolean geometryPreserved;
    private final int maximumAttempts;

    private SpiderDaylightAggressionEvidence(
            int spiderCellX,
            int spiderCellY,
            int spiderCellZ,
            int playerCellX,
            int playerCellY,
            int playerCellZ,
            boolean daylightTargetAbsent,
            boolean nightTargetPlayer,
            boolean spiderIdentityPreserved,
            boolean playerIdentityPreserved,
            boolean geometryPreserved,
            int maximumAttempts) {
        require(spiderCellY >= 0 && spiderCellY < 128, "invalid spider cell");
        require(playerCellY >= 0 && playerCellY < 128, "invalid player cell");
        require(daylightTargetAbsent, "spider selected a daylight target");
        require(nightTargetPlayer, "spider did not select the same player at night");
        require(spiderIdentityPreserved && playerIdentityPreserved,
                "spider or player identity changed across the light transition");
        require(geometryPreserved, "fixture geometry changed across the light transition");
        require(maximumAttempts > 0, "invalid target-selection attempt bound");
        this.spiderCellX = spiderCellX;
        this.spiderCellY = spiderCellY;
        this.spiderCellZ = spiderCellZ;
        this.playerCellX = playerCellX;
        this.playerCellY = playerCellY;
        this.playerCellZ = playerCellZ;
        this.daylightTargetAbsent = daylightTargetAbsent;
        this.nightTargetPlayer = nightTargetPlayer;
        this.spiderIdentityPreserved = spiderIdentityPreserved;
        this.playerIdentityPreserved = playerIdentityPreserved;
        this.geometryPreserved = geometryPreserved;
        this.maximumAttempts = maximumAttempts;
    }

    public static SpiderDaylightAggressionEvidence capture(Trial trial, int expectedMaximum) {
        Objects.requireNonNull(trial, "trial");
        require(trial.maximumAttempts() == expectedMaximum,
                "target-selection attempt bound drifted");
        ActorState spider = trial.spiderBefore();
        ActorState player = trial.playerBefore();
        boolean sameSpider = spider.same(trial.spiderAfter());
        boolean samePlayer = player.same(trial.playerAfter());
        boolean sameGeometry = spider.sameCell(trial.spiderAfter())
                && player.sameCell(trial.playerAfter());
        require(trial.daylightBright(), "daylight brightness prerequisite absent");
        require(trial.nightDark(), "night darkness positive-control prerequisite absent");
        require(trial.daylightTargetId() < 0, "spider selected a daylight target");
        require(trial.nightTargetId() == player.entityId(),
                "night target was not the same player");
        return new SpiderDaylightAggressionEvidence(
                spider.cellX(), spider.cellY(), spider.cellZ(),
                player.cellX(), player.cellY(), player.cellZ(),
                true, true, sameSpider, samePlayer, sameGeometry, expectedMaximum);
    }

    public int spiderCellX() {
        return spiderCellX;
    }

    public int spiderCellY() {
        return spiderCellY;
    }

    public int spiderCellZ() {
        return spiderCellZ;
    }

    public int playerCellX() {
        return playerCellX;
    }

    public int playerCellY() {
        return playerCellY;
    }

    public int playerCellZ() {
        return playerCellZ;
    }

    public boolean daylightTargetAbsent() {
        return daylightTargetAbsent;
    }

    public boolean nightTargetPlayer() {
        return nightTargetPlayer;
    }

    public boolean spiderIdentityPreserved() {
        return spiderIdentityPreserved;
    }

    public boolean playerIdentityPreserved() {
        return playerIdentityPreserved;
    }

    public boolean geometryPreserved() {
        return geometryPreserved;
    }

    public int maximumAttempts() {
        return maximumAttempts;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SpiderDaylightAggressionEvidence)) {
            return false;
        }
        SpiderDaylightAggressionEvidence value = (SpiderDaylightAggressionEvidence) other;
        return spiderCellX == value.spiderCellX
                && spiderCellY == value.spiderCellY
                && spiderCellZ == value.spiderCellZ
                && playerCellX == value.playerCellX
                && playerCellY == value.playerCellY
                && playerCellZ == value.playerCellZ
                && daylightTargetAbsent == value.daylightTargetAbsent
                && nightTargetPlayer == value.nightTargetPlayer
                && spiderIdentityPreserved == value.spiderIdentityPreserved
                && playerIdentityPreserved == value.playerIdentityPreserved
                && geometryPreserved == value.geometryPreserved
                && maximumAttempts == value.maximumAttempts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spiderCellX, spiderCellY, spiderCellZ,
                playerCellX, playerCellY, playerCellZ,
                daylightTargetAbsent, nightTargetPlayer,
                spiderIdentityPreserved, playerIdentityPreserved,
                geometryPreserved, maximumAttempts);
    }

    public static final class ActorState {
        private final int entityId;
        private final int cellX;
        private final int cellY;
        private final int cellZ;

        public ActorState(int entityId, int cellX, int cellY, int cellZ) {
            require(entityId >= 0, "invalid actor identity");
            require(cellY >= 0 && cellY < 128, "invalid actor cell");
            this.entityId = entityId;
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellZ = cellZ;
        }

        public int entityId() {
            return entityId;
        }

        public int cellX() {
            return cellX;
        }

        public int cellY() {
            return cellY;
        }

        public int cellZ() {
            return cellZ;
        }

        boolean same(ActorState other) {
            return other != null && entityId == other.entityId && sameCell(other);
        }

        boolean sameCell(ActorState other) {
            return other != null && cellX == other.cellX
                    && cellY == other.cellY && cellZ == other.cellZ;
        }
    }

    public static final class Trial {
        private final ActorState spiderBefore;
        private final ActorState playerBefore;
        private final int daylightTargetId;
        private final boolean daylightBright;
        private final ActorState spiderAfter;
        private final ActorState playerAfter;
        private final int nightTargetId;
        private final boolean nightDark;
        private final int maximumAttempts;

        public Trial(
                ActorState spiderBefore,
                ActorState playerBefore,
                int daylightTargetId,
                boolean daylightBright,
                ActorState spiderAfter,
                ActorState playerAfter,
                int nightTargetId,
                boolean nightDark,
                int maximumAttempts) {
            this.spiderBefore = Objects.requireNonNull(spiderBefore, "spiderBefore");
            this.playerBefore = Objects.requireNonNull(playerBefore, "playerBefore");
            this.daylightTargetId = daylightTargetId;
            this.daylightBright = daylightBright;
            this.spiderAfter = Objects.requireNonNull(spiderAfter, "spiderAfter");
            this.playerAfter = Objects.requireNonNull(playerAfter, "playerAfter");
            this.nightTargetId = nightTargetId;
            this.nightDark = nightDark;
            this.maximumAttempts = maximumAttempts;
        }

        public ActorState spiderBefore() {
            return spiderBefore;
        }

        public ActorState playerBefore() {
            return playerBefore;
        }

        public int daylightTargetId() {
            return daylightTargetId;
        }

        public boolean daylightBright() {
            return daylightBright;
        }

        public ActorState spiderAfter() {
            return spiderAfter;
        }

        public ActorState playerAfter() {
            return playerAfter;
        }

        public int nightTargetId() {
            return nightTargetId;
        }

        public boolean nightDark() {
            return nightDark;
        }

        public int maximumAttempts() {
            return maximumAttempts;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
