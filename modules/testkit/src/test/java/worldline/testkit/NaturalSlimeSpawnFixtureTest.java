package worldline.testkit;

import worldline.api.RemoteMobSpawn;

final class NaturalSlimeSpawnFixtureTest {
    private NaturalSlimeSpawnFixtureTest() { }
    static void execute() {
        long seed = 17320110707L; int cx = -2, cz = -2;
        RemoteMobSpawn spawn = new RemoteMobSpawn(7, 55, cx * 512 + 256, 320,
                cz * 512 + 256, 0, 0, 1, 0);
        NaturalSlimeSpawnFixture.Evidence first = NaturalSlimeSpawnFixture.await(
                seed, -5, 1, -5, 1, 4, attempt -> attempt == 2 ? spawn : null);
        NaturalSlimeSpawnFixture.Evidence second = NaturalSlimeSpawnFixture.await(
                seed, -5, 1, -5, 1, 4, attempt -> attempt == 3 ? spawn : null);
        require(first.equals(second) && first.maximumAttempts() == 4
                && first.qualifyingChunks() == 5 && NaturalSlimeSpawnFixture.slimeChunk(seed, cx, cz),
                "natural slime spawn evidence is not equatable");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
