import worldline.trace.CanonicalTrace;

/** Powered lever and 20-tick button pulse against the official server JAR. */
public final class WorldlineOfficialOracle {
    private static final long SEED = 17320110707L;
    private static final int Y = 65;

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
        require(world.b(8, Y, 8, na.aK.bn, 13), "lever placement failed");
        require(world.e(9, Y, 8, na.aw.bn), "lever wire placement failed");
        require(world.e(7, Y, 10, na.u.bn), "button wall placement failed");
        require(world.b(8, Y, 10, na.aS.bn, 9), "button placement failed");
        require(world.e(9, Y, 10, na.aw.bn), "button wire placement failed");
        world.c(8, Y, 10, na.aS.bn, 20);
        snapshot(trace, "placed", world);
        require(world.c(9, Y, 8) > 0, "lever wire dark");
        require((world.c(8, Y, 10) & 8) != 0, "button not pressed");
        require(world.c(9, Y, 10) > 0, "button wire dark");
        for (int tick = 1; tick <= 22; tick++) {
            world.h();
            snapshot(trace, "tick" + tick, world);
        }
        require(world.c(9, Y, 8) > 0, "lever released");
        require((world.c(8, Y, 10) & 8) == 0, "button stayed pressed");
        require(world.c(9, Y, 10) == 0, "button wire stayed powered");
        trace.emitTo(System.out);
    }

    private static void snapshot(CanonicalTrace trace, String label, dj world) {
        trace.record(label, world.m(), world.b.size(),
                world.c(9, Y, 8), world.c(8, Y, 10), world.c(9, Y, 10));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
