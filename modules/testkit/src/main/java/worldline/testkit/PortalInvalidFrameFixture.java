package worldline.testkit;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteWorldView;

/** Reusable incomplete-portal topology and official rejection evidence boundary. */
public final class PortalInvalidFrameFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState OBSIDIAN = new BlockState(49, 0);
    private PortalInvalidFrameFixture() { }

    public static Evidence reject(RemoteWorldView live, RemoteWorldView persisted,
            List<BlockPosition> frame, BlockPosition missing, List<BlockPosition> interior,
            boolean fireObserved) {
        if (live == null || persisted == null || frame == null || missing == null
                || interior == null) throw new IllegalArgumentException("null portal evidence");
        require(frame.size() == 13 && new HashSet<>(frame).size() == 13,
                "incomplete portal frame must contain thirteen unique cells");
        require(interior.size() == 6 && new HashSet<>(interior).size() == 6,
                "portal interior must contain six unique cells");
        Set<BlockPosition> disjoint = new HashSet<>(frame);
        require(disjoint.add(missing), "missing frame cell overlaps placed frame");
        for (BlockPosition cell : interior)
            require(disjoint.add(cell), "portal topology cells overlap");
        require(fireObserved, "flint ignition did not reach the incomplete frame");
        requireCell(live, missing, AIR, "live missing frame cell");
        requireCell(persisted, missing, AIR, "persisted missing frame cell");
        for (BlockPosition cell : frame) {
            requireCell(live, cell, OBSIDIAN, "live obsidian frame");
            requireCell(persisted, cell, OBSIDIAN, "persisted obsidian frame");
        }
        int livePortal = portals(live, interior), persistedPortal = portals(persisted, interior);
        require(livePortal == 0, "incomplete frame created live portal cells");
        require(persistedPortal == 0, "incomplete frame persisted portal cells");
        return new Evidence(frame.size(), true, fireObserved, livePortal, persistedPortal);
    }

    private static int portals(RemoteWorldView world, List<BlockPosition> cells) {
        int count = 0;
        for (BlockPosition cell : cells)
            if (world.blockAt(cell.x(), cell.y(), cell.z()).legacyId() == 90) count++;
        return count;
    }
    private static void requireCell(RemoteWorldView world, BlockPosition cell,
            BlockState expected, String description) {
        require(expected.equals(world.blockAt(cell.x(), cell.y(), cell.z())),
                description + " drifted at " + cell);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    /** Equatable evidence normalized to frame and portal-cell counts. */
    public static final class Evidence {
        private final int obsidianCells, livePortalCells, persistedPortalCells;
        private final boolean missingAir, fireObserved;
        Evidence(int obsidianCells, boolean missingAir, boolean fireObserved,
                int livePortalCells, int persistedPortalCells) {
            this.obsidianCells = obsidianCells; this.missingAir = missingAir;
            this.fireObserved = fireObserved; this.livePortalCells = livePortalCells;
            this.persistedPortalCells = persistedPortalCells;
        }
        public int obsidianCells() { return obsidianCells; }
        public boolean missingAir() { return missingAir; }
        public boolean fireObserved() { return fireObserved; }
        public int livePortalCells() { return livePortalCells; }
        public int persistedPortalCells() { return persistedPortalCells; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return obsidianCells == value.obsidianCells && missingAir == value.missingAir
                    && fireObserved == value.fireObserved
                    && livePortalCells == value.livePortalCells
                    && persistedPortalCells == value.persistedPortalCells;
        }
        @Override public int hashCode() {
            return Objects.hash(obsidianCells, missingAir, fireObserved,
                    livePortalCells, persistedPortalCells);
        }
    }
}
