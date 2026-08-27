package worldline.smoke.snowlayernonstackingb173;

import worldline.api.WorldSource;
import worldline.kernel.GameBackend;

/** Advances a native wet or dry ambient scheduler for the snow-layer fixture. */
public final class SnowLayerNonstackingBackend implements GameBackend {
    private final long seed;
    private final boolean snowfall;
    private SnowLayerWorld world;

    SnowLayerNonstackingBackend(long seed, boolean snowfall) {
        this.seed = seed;
        this.snowfall = snowfall;
    }

    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new SnowLayerWorld(
                new SnowMemorySaveHandler(seed, name, snowfall), name, seed, snowfall);
        world.prepare();
        world.rand.setSeed(seed);
        require(world.observation()[0] == 0, "snow fixture did not begin as air");
    }

    @Override
    public void tick() {
        world.ambientPass();
    }

    int[] observation() {
        return world.observation();
    }

    @Override
    public void close() {
        world = null;
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
