import worldline.testapi.PortalBlockSubsystemEvidence;
import worldline.testapi.PortalBlockSubsystemFixture;

/** Complete portal-block subsystem against the untouched obfuscated server JAR. */
public final class WorldlinePortalBlockOfficialOracle {
    private static final long SEED = 17320110707L;
    private WorldlinePortalBlockOfficialOracle() { }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OraclePortalBlockSaveHandler(SEED, "worldline-portal-block"),
                "worldline-portal-block", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OraclePortalBlockScenario scenario = new OraclePortalBlockScenario(world, SEED);
        PortalBlockSubsystemEvidence evidence = PortalBlockSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=9|"))
            throw new IllegalStateException("official portal block claim inventory drifted");
        scenario.emit();
    }
}
