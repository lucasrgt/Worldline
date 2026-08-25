package worldline.testkit;

import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

final class ChestAccessFixtureTest {
    private static final BlockPosition CONTROL = new BlockPosition(4, 72, 4);
    private static final BlockPosition BLOCKED = new BlockPosition(7, 72, 4);
    private static final BlockPosition LID = new BlockPosition(7, 73, 4);
    private static final BlockPosition THIRD = new BlockPosition(9, 72, 4);
    private static final BlockPosition LEFT = new BlockPosition(10, 72, 4);
    private static final BlockPosition RIGHT = new BlockPosition(11, 72, 4);
    private ChestAccessFixtureTest() { }

    static void execute() {
        ChestAccessFixture.Window single = new ChestAccessFixture.Window("Chest", 27, 63);
        ChestAccessFixture.Window large = new ChestAccessFixture.Window("Large chest", 54, 90);
        ChestAccessFixture.Evidence first = verify(world(false), single, large, true, true);
        ChestAccessFixture.Evidence second = verify(world(false), single, large, true, true);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && "Chest:27:63".equals(first.singleWindow())
                && "Large chest:54:90".equals(first.largeWindow())
                && first.blocked() && first.thirdRejected(),
                "chest access evidence was not equatable");
        reject(() -> verify(world(false), single, large, false, true));
        reject(() -> verify(world(false), single, large, true, false));
        reject(() -> verify(world(true), single, large, true, true));
        reject(() -> verify(world(false), single,
                new ChestAccessFixture.Window("Chest", 27, 63), true, true));
    }

    private static ChestAccessFixture.Evidence verify(RemoteWorldView world,
            ChestAccessFixture.Window single, ChestAccessFixture.Window large,
            boolean blocked, boolean thirdRejected) {
        return ChestAccessFixture.verify(world,
                new ChestAccessFixture.Sites(CONTROL, BLOCKED, LID, LEFT, RIGHT, THIRD),
                single, blocked, large, thirdRejected);
    }
    private static RemoteWorldView world(boolean thirdPresent) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot chunk = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384])
                .withBlock(4, 72, 4, new BlockState(54, 0))
                .withBlock(7, 72, 4, new BlockState(54, 0))
                .withBlock(7, 73, 4, new BlockState(1, 0))
                .withBlock(10, 72, 4, new BlockState(54, 0))
                .withBlock(11, 72, 4, new BlockState(54, 0));
        if (thirdPresent) chunk = chunk.withBlock(9, 72, 4, new BlockState(54, 0));
        return new RemoteWorldView(List.of(chunk));
    }
    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid chest access evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
