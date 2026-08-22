import worldline.trace.CanonicalTrace;

/** Torch-powered piston facing east against the official obfuscated server JAR. */
public final class WorldlineOfficialOracle {
    private static final long SEED = 17320110707L;
    private static final int Y = 65;
    private static final int Z = 8;

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
        require(world.b(8, Y, Z, na.aa.bn, 5), "piston placement failed");
        require(world.b(7, Y, Z, na.aR.bn, 5), "torch placement failed");
        snapshot(trace, "placed", world);
        for (int tick = 1; tick <= 8; tick++) {
            world.e();
            world.h();
            snapshot(trace, "tick" + tick, world);
        }
        require((world.c(8, Y, Z) & 8) != 0, "piston did not extend");
        int head = world.a(9, Y, Z);
        require(head == na.ab.bn || head == na.ad.bn, "piston head missing");
        trace.emitTo(System.out);
    }

    private static void snapshot(CanonicalTrace trace, String label, dj world) {
        trace.record(label, world.m(), world.b.size(),
                world.a(8, Y, Z), world.c(8, Y, Z), world.a(9, Y, Z));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
