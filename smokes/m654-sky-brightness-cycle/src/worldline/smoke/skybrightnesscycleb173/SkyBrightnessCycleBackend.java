package worldline.smoke.skybrightnesscycleb173;

import net.minecraft.src.World;
import worldline.api.SkyBrightnessCycleEvidence;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Captures exact clear-weather skylight subtraction at canonical day-cycle times. */
public final class SkyBrightnessCycleBackend implements GameBackend {
    private static final long[] TIMES = {
        0L, 6000L, 12000L, 12500L, 13000L, 13500L, 14000L,
        18000L, 22000L, 22500L, 23000L, 23500L, 23999L
    };
    private final long seed;
    private World world;
    private SkyBrightnessCycleEvidence evidence;

    SkyBrightnessCycleBackend(long seed) {
        this.seed = seed;
    }

    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new SkyMemorySaveHandler(seed, name), name, seed, null);
    }

    @Override
    public void tick() {
        int[] subtraction = new int[TIMES.length];
        for (int index = 0; index < TIMES.length; index++) {
            world.func_32005_b(TIMES[index]);
            world.calculateInitialSkylight();
            require(world.getWorldTime() == TIMES[index], "mapped world time drifted");
            int calculated = world.calculateSkylightSubtracted(1.0F);
            require(world.skylightSubtracted == calculated, "mapped skylight field drifted");
            subtraction[index] = calculated;
        }
        evidence = SkyBrightnessCycleEvidence.capture(TIMES, subtraction);
    }

    void record(CanonicalTrace trace) {
        require(evidence != null, "sky brightness evidence absent");
        trace.record("clear-sky-cycle", 23999L, 0, evidence.flattened());
    }

    @Override
    public void close() {
        evidence = null;
        world = null;
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
