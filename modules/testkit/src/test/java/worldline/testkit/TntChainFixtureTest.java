package worldline.testkit;

import java.util.Collections;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteObjectSpawn;

final class TntChainFixtureTest {
    private TntChainFixtureTest() { }
    static void execute() {
        BlockPosition first = new BlockPosition(4, 70, 4), second = new BlockPosition(5, 70, 4);
        RemoteObjectSpawn one = object(1, first), two = object(2, second);
        RemoteExplosion a = explosion(first), b = explosion(second);
        TntChainFixture.Evidence left = TntChainFixture.observe(first, second, one, a, two, b,
                new BlockState(0, 0), new BlockState(0, 0));
        TntChainFixture.Evidence right = TntChainFixture.observe(first, second, one, a, two, b,
                new BlockState(0, 0), new BlockState(0, 0));
        require(left.equals(right) && left.hashCode() == right.hashCode()
                && left.primedObjects() == 2 && left.objectType() == 50
                && left.strength() == 4 && left.adjacent() && left.bothAir(),
                "TNT chain evidence drifted");
        fail(() -> TntChainFixture.observe(first, new BlockPosition(7, 70, 4), one, a, two, b,
                new BlockState(0, 0), new BlockState(0, 0)));
    }
    private static RemoteObjectSpawn object(int id, BlockPosition position) {
        return new RemoteObjectSpawn(id, 50, position.x() * 32 + 16,
                position.y() * 32 + 16, position.z() * 32 + 16, 0, 0, 0, 0);
    }
    private static RemoteExplosion explosion(BlockPosition position) {
        return new RemoteExplosion(position.x() + 0.5D, position.y() + 0.5D,
                position.z() + 0.5D, 4F, Collections.singletonList(position));
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid TNT chain evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
