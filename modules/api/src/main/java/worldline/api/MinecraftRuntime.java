package worldline.api;

/** A caller-controlled Minecraft lifecycle. */
public interface MinecraftRuntime extends AutoCloseable {
    void bootHeadless();

    void loadWorld(WorldSource source);

    void tick();

    default void tick(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("tick count must not be negative");
        }
        for (int index = 0; index < count; index++) {
            tick();
        }
    }

    RuntimeState state();

    @Override
    void close();
}
