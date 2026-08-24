package worldline.testkit;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteWorldEvent;

final class DoorSoundFixtureTest {
    private DoorSoundFixtureTest() { }

    static void execute() {
        BlockPosition lower = new BlockPosition(4, 72, 5);
        RemoteWorldEvent event = new RemoteWorldEvent(lower, 1003, 0);
        DoorSoundFixture.Evidence first = DoorSoundFixture.observe(lower,
                new BlockState(64, 0), new BlockState(64, 8),
                new BlockState(64, 4), new BlockState(64, 12), event);
        DoorSoundFixture.Evidence second = DoorSoundFixture.observe(lower,
                new BlockState(64, 0), new BlockState(64, 8),
                new BlockState(64, 4), new BlockState(64, 12), event);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.effectId() == 1003 && first.data() == 0 && first.lowerHalf(),
                "door sound evidence drifted");
        fail(() -> DoorSoundFixture.observe(lower,
                new BlockState(64, 0), new BlockState(64, 8),
                new BlockState(64, 4), new BlockState(64, 12),
                new RemoteWorldEvent(lower, 1004, 0)));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid door sound evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
