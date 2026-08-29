import worldline.testkit.VegetationEcologyEvidence;
import worldline.testkit.VegetationEcologyFixture;

/** Vegetation ecology matrix against the untouched official server JAR. */
public final class WorldlineVegetationEcologyOfficialOracle {
    private static final long SEED = 17320110870L;

    private WorldlineVegetationEcologyOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleVegetationMemorySaveHandler(SEED, "worldline-vegetation"),
                "worldline-vegetation", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleVegetationEcologyScenario scenario =
                new OracleVegetationEcologyScenario(world, SEED);
        VegetationEcologyEvidence evidence = VegetationEcologyFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=19|"))
            throw new IllegalStateException("official vegetation claim inventory drifted");
        scenario.emit();
    }
}
