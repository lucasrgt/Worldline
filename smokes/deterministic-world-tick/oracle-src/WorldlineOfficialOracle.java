import worldline.trace.CanonicalTrace;

/** Executes the canonical fixture directly against the official obfuscated server JAR. */
public final class WorldlineOfficialOracle {
    private static final long SEED = 17320110707L;
    private static final int X = 8;
    private static final int Z = 8;
    private static final int SAND_Y = 70;

    private WorldlineOfficialOracle() {}

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        ei.a = true;
        dj world = new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"),
                "worldline-smoke", SEED, null);
        preload(world);
        require(world.a(X, 64, Z) == na.u.bn, "fixture stone missing");
        require(world.a(X, SAND_Y, Z) == 0, "drop cell is not air");

        CanonicalTrace trace = new CanonicalTrace(SEED);
        snapshot(trace, "initial", world);
        require(world.e(X, SAND_Y, Z, na.F.bn), "sand placement failed");
        snapshot(trace, "placed", world);
        for (int tick = 1; tick <= 8; tick++) {
            world.h();
            snapshot(trace, "tick" + tick, world);
        }

        require(world.a(X, SAND_Y, Z) == 0, "sand remained in drop cell");
        require(world.a(X, 65, Z) == na.F.bn, "sand did not land on stone");
        require(world.a(X, 64, Z) == na.u.bn, "fixture stone changed");
        trace.emitTo(System.out);
    }

    private static void preload(dj world) {
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                world.c(chunkX, chunkZ);
            }
        }
    }

    private static void snapshot(CanonicalTrace trace, String label, dj world) {
        int[] column = new int[SAND_Y - 63];
        for (int y = 64; y <= SAND_Y; y++) {
            column[y - 64] = world.a(X, y, Z);
        }
        trace.record(label, world.m(), world.b.size(), column);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
