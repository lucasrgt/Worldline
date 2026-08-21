import worldline.trace.CanonicalTrace;

/** Delay-2/3/4 repeater fixture against the official obfuscated server JAR. */
public final class WorldlineOfficialOracle {
    private static final long SEED = 17320110707L;
    private static final int TORCH_X = 8;
    private static final int REPEATER_X = 9;
    private static final int Y = 65;
    private static final int[] Z = { 8, 9, 10 };
    private static final int[] META = { 5, 9, 13 };

    private WorldlineOfficialOracle() {}

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"),
                "worldline-smoke", SEED, null);
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                world.c(chunkX, chunkZ);
            }
        }
        CanonicalTrace trace = new CanonicalTrace(SEED);
        snapshot(trace, "initial", world);
        for (int index = 0; index < Z.length; index++) {
            require(world.b(TORCH_X, Y, Z[index], na.aR.bn, 5), "torch placement failed");
            require(world.b(REPEATER_X, Y, Z[index], na.bi.bn, META[index]),
                    "repeater placement failed");
        }
        snapshot(trace, "placed", world);
        for (int index = 0; index < Z.length; index++) {
            require(world.a(REPEATER_X, Y, Z[index]) == na.bi.bn, "repeater locked on during placement");
        }
        for (int tick = 1; tick <= 8; tick++) {
            world.h();
            snapshot(trace, "tick" + tick, world);
        }
        require(world.a(REPEATER_X, Y, Z[0]) == na.bj.bn, "delay-2 stayed idle");
        require(world.a(REPEATER_X, Y, Z[1]) == na.bj.bn, "delay-3 stayed idle");
        require(world.a(REPEATER_X, Y, Z[2]) == na.bj.bn, "delay-4 stayed idle");
        trace.emitTo(System.out);
    }

    private static void snapshot(CanonicalTrace trace, String label, dj world) {
        trace.record(label, world.m(), world.b.size(),
                world.a(REPEATER_X, Y, Z[0]), world.a(REPEATER_X, Y, Z[1]),
                world.a(REPEATER_X, Y, Z[2]));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
