import worldline.trace.CanonicalTrace;

/** Executes the torch-and-dust fixture against the official obfuscated server JAR. */
public final class WorldlineOfficialOracle {
    private static final long SEED = 17320110707L;
    private static final int TORCH_X = 8;
    private static final int WIRE_X = 9;
    private static final int OBSERVE_X = 10;
    private static final int Y = 65;
    private static final int Z = 8;

    private WorldlineOfficialOracle() {}

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleMemorySaveHandler(SEED, "worldline-smoke"),
                "worldline-smoke", SEED, null);
        preload(world);
        require(world.a(TORCH_X, 64, Z) == na.u.bn, "fixture stone missing");
        require(world.a(TORCH_X, Y, Z) == 0, "torch cell is not air");
        require(world.a(WIRE_X, Y, Z) == 0, "wire cell is not air");

        CanonicalTrace trace = new CanonicalTrace(SEED);
        snapshot(trace, "initial", world);
        require(world.b(TORCH_X, Y, Z, na.aR.bn, 5), "torch placement failed");
        require(world.e(WIRE_X, Y, Z, na.aw.bn), "wire placement failed");
        snapshot(trace, "placed", world);
        for (int tick = 1; tick <= 4; tick++) {
            world.h();
            snapshot(trace, "tick" + tick, world);
        }

        require(world.a(TORCH_X, Y, Z) == na.aR.bn, "torch missing");
        require(world.a(WIRE_X, Y, Z) == na.aw.bn, "wire missing");
        require(world.c(WIRE_X, Y, Z) > 0, "wire has no power");
        require(world.r(OBSERVE_X, Y, Z), "observer is unpowered");
        require(na.aR.d(), "torch does not provide power");
        require(world.a(TORCH_X, 64, Z) == na.u.bn, "fixture stone changed");
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
        int powered = world.r(OBSERVE_X, Y, Z) ? 1 : 0;
        int provides = na.aR.d() ? 1 : 0;
        trace.record(label, world.m(), world.b.size(),
                world.a(TORCH_X, Y, Z), world.a(WIRE_X, Y, Z),
                world.c(WIRE_X, Y, Z), powered, provides);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
