package worldline.testkit;

import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

final class TorchSupportBreakFixtureTest {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STONE = new BlockState(1, 0);
    private static final RemoteItemStack DROP = new RemoteItemStack(50, 1, 0);

    private TorchSupportBreakFixtureTest() { }

    static void execute() {
        TorchSupportBreakFixture.Evidence first = evidence(), second = evidence();
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.torchMetadata() == 1 && first.supportId() == 1
                && first.dropDamage() == 0 && first.persisted().equals(AIR),
                "torch support-break evidence drifted");
        fail(() -> TorchSupportBreakFixture.observe(new BlockState(50, 5),
                STONE, AIR, DROP, AIR));
        fail(() -> TorchSupportBreakFixture.observe(new BlockState(50, 1),
                AIR, AIR, DROP, AIR));
        fail(() -> TorchSupportBreakFixture.observe(new BlockState(50, 1),
                STONE, new BlockState(50, 1), DROP, AIR));
        fail(() -> TorchSupportBreakFixture.observe(new BlockState(50, 1),
                STONE, AIR, new RemoteItemStack(50, 2, 0), AIR));
        fail(() -> TorchSupportBreakFixture.observe(new BlockState(50, 1),
                STONE, AIR, new RemoteItemStack(76, 1, 0), AIR));
        fail(() -> TorchSupportBreakFixture.observe(new BlockState(50, 1),
                STONE, AIR, DROP, new BlockState(50, 1)));
    }

    private static TorchSupportBreakFixture.Evidence evidence() {
        return TorchSupportBreakFixture.observe(new BlockState(50, 1),
                STONE, AIR, DROP, AIR);
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid torch support-break evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
