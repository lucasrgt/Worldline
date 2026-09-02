import worldline.testapi.FluidFrozenMatterEvidence;
import worldline.testapi.FluidFrozenMatterFixture;

/** Fluid and frozen-matter matrix against the untouched official server JAR. */
public final class WorldlineFluidFrozenMatterOfficialOracle {
    private static final long SEED = 17320110872L;

    private WorldlineFluidFrozenMatterOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleFluidFrozenMemorySaveHandler(SEED, "worldline-fluid-frozen"),
                "worldline-fluid-frozen", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleFluidFrozenMatterScenario scenario =
                new OracleFluidFrozenMatterScenario(world, SEED);
        FluidFrozenMatterEvidence evidence = FluidFrozenMatterFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=21|"))
            throw new IllegalStateException("official fluid/frozen claim inventory drifted");
        scenario.emit();
    }
}
