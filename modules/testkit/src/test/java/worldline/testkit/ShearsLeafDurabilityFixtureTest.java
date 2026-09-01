package worldline.testkit;

import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

final class ShearsLeafDurabilityFixtureTest {
    private ShearsLeafDurabilityFixtureTest() { }

    static void execute() {
        ShearsLeafDurabilityFixture.Evidence first = evidence(), second = evidence();
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.leaf().equals(new BlockState(18, 8))
                && first.drop().equals(new RemoteItemStack(18, 1, 0))
                && first.beforeDamage() == 0 && first.afterDamage() == 1,
                "shears leaf durability evidence drifted");
        fail(() -> ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 4),
                new BlockState(18, 8), new RemoteItemStack(18, 1, 0), new BlockState(0, 0),
                new RemoteItemStack(359, 1, 5)));
        fail(() -> ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 0),
                new BlockState(17, 0), new RemoteItemStack(18, 1, 0), new BlockState(0, 0),
                new RemoteItemStack(359, 1, 1)));
        fail(() -> ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 0),
                new BlockState(18, 8), new RemoteItemStack(18, 2, 0), new BlockState(0, 0),
                new RemoteItemStack(359, 1, 1)));
        fail(() -> ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 0),
                new BlockState(18, 8), new RemoteItemStack(18, 1, 0), new BlockState(18, 8),
                new RemoteItemStack(359, 1, 1)));
        fail(() -> ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 0),
                new BlockState(18, 8), new RemoteItemStack(18, 1, 0), new BlockState(0, 0),
                new RemoteItemStack(359, 1, 2)));
        fail(() -> ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 0),
                new BlockState(18, 8), new RemoteItemStack(18, 1, 0), new BlockState(0, 0),
                new RemoteItemStack(359, 1, 0)));
    }

    private static ShearsLeafDurabilityFixture.Evidence evidence() {
        return ShearsLeafDurabilityFixture.harvest(new RemoteItemStack(359, 1, 0),
                new BlockState(18, 8), new RemoteItemStack(18, 1, 0), new BlockState(0, 0),
                new RemoteItemStack(359, 1, 1));
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid shears leaf evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
