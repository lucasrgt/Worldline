import worldline.testapi.FireSubsystemEvidence;
import worldline.testapi.FireSubsystemFixture;

/** Complete fire subsystem against the untouched obfuscated server JAR. */
public final class WorldlineFireOfficialOracle {
    private static final long SEED = 17320110510L;
    private WorldlineFireOfficialOracle() {
    }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleFireSaveHandler(SEED, "worldline-fire"),
                "worldline-fire", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleFireScenario scenario = new OracleFireScenario(world, SEED);
        FireSubsystemEvidence evidence = FireSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=8|"))
            throw new IllegalStateException("official fire claim inventory drifted");
        scenario.emit();
    }
}
