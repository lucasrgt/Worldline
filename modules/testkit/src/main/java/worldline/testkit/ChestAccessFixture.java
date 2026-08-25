package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteWorldView;

/** Reusable official chest-access topology and Packet100 evidence boundary. */
public final class ChestAccessFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STONE = new BlockState(1, 0);
    private static final BlockState CHEST = new BlockState(54, 0);
    private ChestAccessFixture() { }

    public static Evidence verify(RemoteWorldView world, Sites sites, Window single,
            boolean blockedWindowAbsent, Window large, boolean thirdPlacementRejected) {
        if (world == null || sites == null || single == null || large == null)
            throw new IllegalArgumentException("null chest access evidence");
        requireCell(world, sites.control, CHEST, "control chest");
        requireCell(world, sites.blocked, CHEST, "blocked chest");
        requireCell(world, sites.lid, STONE, "solid lid");
        requireCell(world, sites.left, CHEST, "left double chest");
        requireCell(world, sites.right, CHEST, "right double chest");
        requireCell(world, sites.thirdTarget, AIR, "rejected third chest");
        require(single.matches("Chest", 27, 63), "single chest window drifted");
        require(blockedWindowAbsent, "solid lid did not suppress Packet100");
        require(large.matches("Large chest", 54, 90), "double chest window drifted");
        require(thirdPlacementRejected, "third adjacent chest placement was accepted");
        return new Evidence(single.shape(), large.shape(), true, true);
    }

    private static void requireCell(RemoteWorldView world, BlockPosition cell,
            BlockState expected, String description) {
        require(expected.equals(world.blockAt(cell.x(), cell.y(), cell.z())),
                description + " topology drifted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    /** Absolute cells needed to compare a chest-access fixture. */
    public static final class Sites {
        private final BlockPosition control, blocked, lid, left, right, thirdTarget;
        public Sites(BlockPosition control, BlockPosition blocked, BlockPosition lid,
                BlockPosition left, BlockPosition right, BlockPosition thirdTarget) {
            if (control == null || blocked == null || lid == null || left == null
                    || right == null || thirdTarget == null)
                throw new IllegalArgumentException("incomplete chest access sites");
            this.control = control; this.blocked = blocked; this.lid = lid;
            this.left = left; this.right = right; this.thirdTarget = thirdTarget;
        }
    }

    /** Window shape with unstable protocol window IDs deliberately excluded. */
    public static final class Window {
        private final String title;
        private final int ownedSlots, totalSlots;
        public Window(String title, int ownedSlots, int totalSlots) {
            if (title == null || title.isEmpty() || ownedSlots < 1 || totalSlots < ownedSlots)
                throw new IllegalArgumentException("invalid chest window shape");
            this.title = title; this.ownedSlots = ownedSlots; this.totalSlots = totalSlots;
        }
        public boolean matches(String expectedTitle, int expectedOwned, int expectedTotal) {
            return title.equals(expectedTitle) && ownedSlots == expectedOwned
                    && totalSlots == expectedTotal;
        }
        public String shape() { return title + ":" + ownedSlots + ":" + totalSlots; }
    }

    /** Equatable evidence normalized to window shapes and access outcomes. */
    public static final class Evidence {
        private final String singleWindow, largeWindow;
        private final boolean blocked, thirdRejected;
        Evidence(String singleWindow, String largeWindow, boolean blocked,
                boolean thirdRejected) {
            this.singleWindow = singleWindow; this.largeWindow = largeWindow;
            this.blocked = blocked; this.thirdRejected = thirdRejected;
        }
        public String singleWindow() { return singleWindow; }
        public String largeWindow() { return largeWindow; }
        public boolean blocked() { return blocked; }
        public boolean thirdRejected() { return thirdRejected; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return blocked == value.blocked && thirdRejected == value.thirdRejected
                    && singleWindow.equals(value.singleWindow)
                    && largeWindow.equals(value.largeWindow);
        }
        @Override public int hashCode() {
            return Objects.hash(singleWindow, largeWindow, blocked, thirdRejected);
        }
    }
}
