import worldline.trace.CanonicalTrace;

/** Executes the clear-sky cycle directly against official server symbols. */
public final class WorldlineSkyBrightnessCycleOracle {
    private static final long SEED = 65420260825L;
    private static final long[] TIMES = {
        0L, 6000L, 12000L, 12500L, 13000L, 13500L, 14000L,
        18000L, 22000L, 22500L, 23000L, 23500L, 23999L
    };

    private WorldlineSkyBrightnessCycleOracle() { }

    public static void main(String[] arguments) {
        dj world = new dj(new OracleSkySaveHandler(SEED, "sky-brightness-cycle"),
                "sky-brightness-cycle", SEED, null);
        int[] flattened = new int[TIMES.length * 2];
        for (int index = 0; index < TIMES.length; index++) {
            world.b(TIMES[index]);
            world.g();
            require(world.m() == TIMES[index], "official world time drifted");
            int calculated = world.a(1.0F);
            require(world.f == calculated, "official skylight field drifted");
            flattened[index * 2] = Math.toIntExact(TIMES[index]);
            flattened[index * 2 + 1] = calculated;
        }
        CanonicalTrace trace = new CanonicalTrace(SEED);
        trace.record("clear-sky-cycle", 23999L, 0, flattened);
        trace.emitTo(System.out);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
