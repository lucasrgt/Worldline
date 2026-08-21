package worldline.test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GamePlayer;
import worldline.api.GamePosition;

/** Capabilities and diagnostics owned by one isolated test attempt. */
public interface TestContext {
    long seed();
    int attempt();
    AutomatedMinecraftRuntime runtime();
    Path artifactDirectory();
    void attach(String name, byte[] bytes);
    void skip(String reason);
    void step(String name, TestAction action) throws Exception;
    void onFinished(TestHook hook);
    void onFailed(TestHook hook);

    default void attach(String name, String text) {
        attach(name, text.getBytes(StandardCharsets.UTF_8));
    }
    default void tick() { runtime().tick(); }
    default void tick(int count) { runtime().tick(count); }
    default GamePlayer player() { return runtime().player(); }
    default int health() { return player().health(); }
    default int selectedHotbarSlot() { return player().selectedHotbarSlot(); }
    default GamePosition position() { return player().position(); }
    default BlockState block(int x, int y, int z) {
        return block(new BlockPosition(x, y, z));
    }
    default BlockState block(BlockPosition position) { return runtime().world().block(position); }
    default void setBlock(BlockPosition position, BlockState state) {
        TestMappingAccess.requireWrite(state);
        if (!runtime().world().setBlock(position, state)) {
            throw new IllegalStateException("runtime rejected block write at " + position);
        }
    }
    default void step(String name, CheckedStep action) throws Exception {
        if (action == null) throw new NullPointerException("action");
        step(name, context -> action.run());
    }

    @FunctionalInterface interface CheckedStep { void run() throws Exception; }
    @FunctionalInterface interface TestAction { void run(TestContext context) throws Exception; }
}
