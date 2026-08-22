import worldline.trace.CanonicalTrace;

/** Executes the seeded pig-wander fixture against the official server JAR. */
public final class WorldlinePigWanderOracle {
    private static final long SEED = 50320240820L;
    private static final int TICKS = 240;
    private static final double START_X = 8.5D;
    private static final double START_Y = 65.0D;
    private static final double START_Z = 8.5D;
    private WorldlinePigWanderOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        CanonicalTrace trace = new CanonicalTrace(SEED);
        run(trace, "open", false);
        run(trace, "caged", true);
        trace.emitTo(System.out);
    }

    private static void run(CanonicalTrace trace, String label, boolean caged) {
        dj world = new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke", caged),
                "worldline-smoke", SEED, null);
        world.r.setSeed(SEED);
        preload(world);
        require(world.a(8, 64, 8) == na.u.bn, "fixture stone missing");
        OracleSeededPig pig = new OracleSeededPig(world);
        pig.seedBehavior(SEED);
        pig.c(START_X, START_Y, START_Z);
        require(world.b(pig), "fixture pig was rejected");
        long maximumHorizontal = 0L;
        long minimumY = milli(pig.aQ);
        long maximumY = minimumY;
        snapshot(trace, label + "-seed", world, pig);
        for (int tick = 1; tick <= TICKS; tick++) {
            world.h();
            world.e();
            long dx = milli(pig.aP - START_X);
            long dz = milli(pig.aR - START_Z);
            maximumHorizontal = Math.max(maximumHorizontal,
                    Math.round(Math.sqrt(dx * dx + dz * dz)));
            minimumY = Math.min(minimumY, milli(pig.aQ));
            maximumY = Math.max(maximumY, milli(pig.aQ));
            require(!pig.bh && world.b.size() == 1, "pig left the fixture");
            if (tick % 5 == 0) snapshot(trace, label + "-tick" + tick, world, pig);
        }
        require(pig.bw == TICKS, "pig tick age drifted");
        require(minimumY >= 64_900L && maximumY <= 66_100L, "pig escaped vertically");
        if (caged) require(maximumHorizontal <= 250L, "caged pig escaped: " + maximumHorizontal);
        else require(maximumHorizontal >= 500L && maximumHorizontal <= 12_000L,
                "open pig did not wander within bounds: " + maximumHorizontal);
    }

    private static void preload(dj world) {
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) world.c(chunkX, chunkZ);
        }
    }

    private static void snapshot(CanonicalTrace trace, String label, dj world, oc pig) {
        trace.record(label, world.m(), world.b.size(), (int) milli(pig.aP),
                (int) milli(pig.aQ), (int) milli(pig.aR), pig.bw, pig.bh ? 1 : 0);
    }

    private static long milli(double value) { return Math.round(value * 1000.0D); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
