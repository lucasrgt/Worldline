import worldline.testkit.BedSubsystemEvidence;
import worldline.testkit.BedSubsystemFixture;

/** Complete bed subsystem against the untouched obfuscated server JAR. */
public final class WorldlineBedOfficialOracle {
    private static final long SEED = 17320110726L;
    private WorldlineBedOfficialOracle() { }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleBedSaveHandler(SEED, "worldline-bed"),
                "worldline-bed", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleBedScenario scenario = new OracleBedScenario(world, SEED);
        BedSubsystemEvidence evidence = BedSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=7|"))
            throw new IllegalStateException("official bed claim inventory drifted");
        scenario.emit();
    }
}
