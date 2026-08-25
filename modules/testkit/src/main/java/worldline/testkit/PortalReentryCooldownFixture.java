package worldline.testkit;

import java.util.Objects;

/** Reusable dimension and timing evidence for arrival-side portal re-entry cooldown. */
public final class PortalReentryCooldownFixture {
    private PortalReentryCooldownFixture() { }

    public static Evidence verify(Trial contact, Trial released, int sourcePortalCells,
            int destinationPortalCells, int returnPortalCells, int persistedDimension) {
        if (contact == null || released == null)
            throw new IllegalArgumentException("null portal cooldown trial");
        require(contact.actorKey.equals(released.actorKey),
                "portal cooldown trials used different players");
        require(contact.matches(-1, -1, 0, 120, true, false, false),
                "continuous arrival-portal contact did not suppress return");
        require(released.matches(-1, 0, 220, 120, true, true, true),
                "released portal re-entry did not return to the Overworld");
        require(sourcePortalCells == 6 && destinationPortalCells == 6
                && returnPortalCells == 6, "portal geometry drifted");
        require(persistedDimension == 0, "returned dimension was not persisted");
        return new Evidence(contact.actorKey, contact.residenceTicks, released.outsideTicks,
                released.exitedCollision, released.reenteredCollision, sourcePortalCells,
                destinationPortalCells, returnPortalCells, persistedDimension);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    /** One bounded contact trial, excluding dynamic portal coordinates. */
    public static final class Trial {
        private final String actorKey;
        private final int before, after, outsideTicks, residenceTicks;
        private final boolean insideBefore, exitedCollision, reenteredCollision;
        public Trial(String actorKey, int before, int after, int outsideTicks,
                int residenceTicks, boolean insideBefore, boolean exitedCollision,
                boolean reenteredCollision) {
            if (actorKey == null || actorKey.isEmpty() || (before != 0 && before != -1)
                    || (after != 0 && after != -1)
                    || outsideTicks < 0 || residenceTicks < 1)
                throw new IllegalArgumentException("invalid portal cooldown trial");
            this.actorKey = actorKey;
            this.before = before;
            this.after = after;
            this.outsideTicks = outsideTicks;
            this.residenceTicks = residenceTicks;
            this.insideBefore = insideBefore;
            this.exitedCollision = exitedCollision;
            this.reenteredCollision = reenteredCollision;
        }
        boolean matches(int expectedBefore, int expectedAfter, int expectedOutside,
                int expectedResidence, boolean expectedInside, boolean expectedExited,
                boolean expectedReentered) {
            return before == expectedBefore && after == expectedAfter
                    && outsideTicks == expectedOutside && residenceTicks == expectedResidence
                    && insideBefore == expectedInside && exitedCollision == expectedExited
                    && reenteredCollision == expectedReentered;
        }
    }

    /** Equatable evidence normalized to bounded timing and portal counts. */
    public static final class Evidence {
        private final String actorKey;
        private final int blockedTicks, releaseTicks, sourcePortalCells;
        private final int destinationPortalCells, returnPortalCells, persistedDimension;
        private final boolean exitedCollision, reenteredCollision;
        Evidence(String actorKey, int blockedTicks, int releaseTicks, boolean exitedCollision,
                boolean reenteredCollision, int sourcePortalCells, int destinationPortalCells,
                int returnPortalCells, int persistedDimension) {
            this.actorKey = actorKey;
            this.blockedTicks = blockedTicks;
            this.releaseTicks = releaseTicks;
            this.exitedCollision = exitedCollision;
            this.reenteredCollision = reenteredCollision;
            this.sourcePortalCells = sourcePortalCells;
            this.destinationPortalCells = destinationPortalCells;
            this.returnPortalCells = returnPortalCells;
            this.persistedDimension = persistedDimension;
        }
        public int blockedTicks() { return blockedTicks; }
        public int releaseTicks() { return releaseTicks; }
        public String actorKey() { return actorKey; }
        public boolean exitedCollision() { return exitedCollision; }
        public boolean reenteredCollision() { return reenteredCollision; }
        public int sourcePortalCells() { return sourcePortalCells; }
        public int destinationPortalCells() { return destinationPortalCells; }
        public int returnPortalCells() { return returnPortalCells; }
        public int persistedDimension() { return persistedDimension; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return actorKey.equals(value.actorKey) && blockedTicks == value.blockedTicks
                    && releaseTicks == value.releaseTicks
                    && exitedCollision == value.exitedCollision
                    && reenteredCollision == value.reenteredCollision
                    && sourcePortalCells == value.sourcePortalCells
                    && destinationPortalCells == value.destinationPortalCells
                    && returnPortalCells == value.returnPortalCells
                    && persistedDimension == value.persistedDimension;
        }
        @Override public int hashCode() {
            return Objects.hash(actorKey, blockedTicks, releaseTicks, exitedCollision,
                    reenteredCollision, sourcePortalCells, destinationPortalCells,
                    returnPortalCells, persistedDimension);
        }
    }
}
