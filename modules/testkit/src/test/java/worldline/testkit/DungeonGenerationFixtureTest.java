package worldline.testkit;

import java.util.List;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

final class DungeonGenerationFixtureTest {
    private DungeonGenerationFixtureTest() { }
    static void execute() {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot chunk = new RemoteChunkSnapshot(region, new byte[32768], new byte[16384],
                new byte[16384], new byte[16384])
                .withBlock(8, 20, 8, new BlockState(52, 0))
                .withBlock(10, 20, 8, new BlockState(54, 0));
        DungeonGenerationFixture.Evidence first = DungeonGenerationFixture.observe(
                new RemoteWorldView(List.of(chunk)), 0, 0, 0, 0);
        DungeonGenerationFixture.Evidence second = DungeonGenerationFixture.observe(
                new RemoteWorldView(List.of(chunk)), 0, 0, 0, 0);
        require(first.equals(second) && first.chunks() == 1 && first.spawners().size() == 1
                && first.linkedChests().size() == 1 && first.digest().matches("[0-9a-f]{64}"),
                "dungeon generation fixture is not equatable");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
