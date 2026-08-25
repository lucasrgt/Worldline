package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.RemoteMobSpawn;

final class NaturalWolfPackFixtureTest {
    private NaturalWolfPackFixtureTest() { }
    static void execute() {
        RemoteMobSpawn first = wolf(11, 320, 640), second = wolf(12, 352, 672);
        NaturalWolfPackFixture.Evidence early = NaturalWolfPackFixture.await(64,
                attempt -> attempt == 2 ? Arrays.asList(first, second) : Collections.emptyList());
        NaturalWolfPackFixture.Evidence late = NaturalWolfPackFixture.await(64,
                attempt -> attempt == 9 ? Arrays.asList(first, second) : Collections.emptyList());
        require(early.equals(late) && early.minimumPackSize() == 2
                && early.maximumPackSize() == 8 && early.maximumAttempts() == 64,
                "natural wolf pack evidence retained successful RNG details");
    }
    private static RemoteMobSpawn wolf(int id, int x, int z) {
        return new RemoteMobSpawn(id, 95, x, 2080, z, 0, 0, 1, 0);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
