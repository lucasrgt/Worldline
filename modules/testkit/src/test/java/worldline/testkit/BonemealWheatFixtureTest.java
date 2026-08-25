package worldline.testkit;

import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

final class BonemealWheatFixtureTest {
    private BonemealWheatFixtureTest() { }

    static void execute() {
        BonemealWheatFixture.Evidence first = evidence(), second = evidence();
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.itemId() == 351 && first.damage() == 15
                && first.before().equals(new BlockState(59, 0))
                && first.after().equals(first.persisted()), "bonemeal wheat evidence drifted");
        fail(() -> BonemealWheatFixture.observe(new BlockState(59, 0),
                new RemoteItemStack(351, 1, 0), new BlockState(59, 7),
                new BlockState(59, 7)));
        fail(() -> BonemealWheatFixture.observe(new BlockState(59, 0),
                new RemoteItemStack(351, 1, 15), new BlockState(59, 6),
                new BlockState(59, 6)));
    }

    private static BonemealWheatFixture.Evidence evidence() {
        return BonemealWheatFixture.observe(new BlockState(59, 0),
                new RemoteItemStack(351, 1, 15), new BlockState(59, 7),
                new BlockState(59, 7));
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid bonemeal evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
