package worldline.testkit;

import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

final class WallSignSupportBreakFixtureTest {
    private WallSignSupportBreakFixtureTest() { }

    static void execute() {
        WallSignSupportBreakFixture.Evidence first = evidence(), second = evidence();
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "wall sign support break evidence equality drifted");
        require(first.supportBefore().equals(new BlockState(1, 0))
                && first.signBefore().equals(new BlockState(68, 5))
                && first.supportAfter().equals(new BlockState(0, 0))
                && first.signAfter().equals(first.persistedSign())
                && first.drop().equals(new RemoteItemStack(323, 1, 0)),
                "wall sign support break evidence fields drifted");
        fail(() -> WallSignSupportBreakFixture.observe(
                new BlockState(3, 0), sign(), air(), air(), drop(), air()));
        fail(() -> observe(new BlockState(63, 0), drop()));
        fail(() -> observe(new RemoteItemStack(323, 2, 0)));
    }

    private static WallSignSupportBreakFixture.Evidence evidence() {
        return observe(air(), drop());
    }

    private static WallSignSupportBreakFixture.Evidence observe(BlockState persisted,
            RemoteItemStack drop) {
        return WallSignSupportBreakFixture.observe(new BlockState(1, 0), new BlockState(68, 5),
                air(), air(), drop, persisted);
    }

    private static WallSignSupportBreakFixture.Evidence observe(RemoteItemStack drop) {
        return observe(sign(), drop);
    }

    private static BlockState sign() { return new BlockState(68, 5); }
    private static BlockState air() { return new BlockState(0, 0); }
    private static RemoteItemStack drop() { return new RemoteItemStack(323, 1, 0); }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid wall sign evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
