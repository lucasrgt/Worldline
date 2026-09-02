import worldline.testapi.OpenableAccessStructuresEvidence;
import worldline.testapi.OpenableAccessStructuresFixture;

/** Openable access-structure matrix against the untouched official server JAR. */
public final class WorldlineOpenableAccessStructuresOfficialOracle {
    private static final long SEED = 17320110900L;

    private WorldlineOpenableAccessStructuresOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleOpenableAccessMemorySaveHandler(SEED, "worldline-openable-access"),
                "worldline-openable-access", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleOpenableAccessStructuresScenario scenario =
                new OracleOpenableAccessStructuresScenario(world, SEED);
        OpenableAccessStructuresEvidence evidence = OpenableAccessStructuresFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=10|"))
            throw new IllegalStateException("official openable access claim inventory drifted");
        scenario.emit();
    }
}
