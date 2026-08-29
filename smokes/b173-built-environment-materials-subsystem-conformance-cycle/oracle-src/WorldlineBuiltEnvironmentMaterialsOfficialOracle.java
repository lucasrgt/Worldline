import worldline.testkit.BuiltEnvironmentMaterialsEvidence;
import worldline.testkit.BuiltEnvironmentMaterialsFixture;

/** Construction-material matrix against the untouched official server JAR. */
public final class WorldlineBuiltEnvironmentMaterialsOfficialOracle {
    private static final long SEED = 17320110850L;

    private WorldlineBuiltEnvironmentMaterialsOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleBuiltEnvironmentMemorySaveHandler(
                SEED, "worldline-built-environment"),
                "worldline-built-environment", SEED, null);
        for (int chunkX = -4; chunkX <= 5; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleBuiltEnvironmentMaterialsScenario scenario =
                new OracleBuiltEnvironmentMaterialsScenario(world, SEED);
        BuiltEnvironmentMaterialsEvidence evidence =
                BuiltEnvironmentMaterialsFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=47|"))
            throw new IllegalStateException("official built-environment claim inventory drifted");
        scenario.emit();
    }
}
