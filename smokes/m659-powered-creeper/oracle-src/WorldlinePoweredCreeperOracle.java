import worldline.trace.CanonicalTrace;

/** Executes the native same-identity lightning transformation against the official server JAR. */
public final class WorldlinePoweredCreeperOracle {
    private static final long SEED = 65920260826L;
    private static final int X = 8;
    private static final int Y = 65;
    private static final int Z = 8;

    private WorldlinePoweredCreeperOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        CanonicalTrace trace = new CanonicalTrace(SEED);
        dj world = new dj(new OraclePoweredCreeperMemorySave(SEED, "powered-creeper"),
                "powered-creeper", SEED, null);
        world.q = 1;
        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                world.c(chunkX, chunkZ);
            }
        }
        ec creeper = new ec(world);
        creeper.c(X + 0.5D, Y, Z + 0.5D);
        require(world.b(creeper) && world.b.contains(creeper), "official creeper join failed");
        int identity = creeper.aG;
        require(!creeper.s(), "official creeper powered precondition absent");
        trace.record("initial", world.m(), world.b.size(), 1, X, Y, Z);

        require(!creeper.s() && creeper.aG == identity,
                "official creeper prerequisite identity drifted");
        c lightning = new c(world, creeper.aP, creeper.aQ, creeper.aR);
        boolean joined = world.b(lightning)
                && world.b.contains(lightning) && world.b.contains(creeper);
        boolean sameCell = cell(lightning.aP) == cell(creeper.aP)
                && cell(lightning.aQ) == cell(creeper.aQ)
                && cell(lightning.aR) == cell(creeper.aR);
        require(joined && !lightning.bh && sameCell,
                "official lightning was not observed at the creeper cell");
        creeper.a(lightning);
        require(creeper.aG == identity && creeper.s(),
                "official same creeper did not become powered");
        trace.record("strike", 0L, 2, 1, 1, 1, lightning.aG != identity ? 1 : 0);
        trace.record("powered", 0L, 2, 1, 1, 1);

        world.h();
        world.e();
        require(creeper.aG == identity && creeper.s()
                        && cell(creeper.aP) == X && cell(creeper.aQ) == Y && cell(creeper.aR) == Z,
                "official powered state did not survive the observation tick");
        trace.record("held", world.m(), world.b.size(), 1, 1, 1, 1);
        trace.emitTo(System.out);
    }

    private static int cell(double coordinate) {
        return (int) Math.floor(coordinate);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
