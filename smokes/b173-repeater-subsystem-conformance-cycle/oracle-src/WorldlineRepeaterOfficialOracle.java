import worldline.testkit.RepeaterSubsystemEvidence;
import worldline.testkit.RepeaterSubsystemFixture;

/** Complete repeater subsystem against the untouched obfuscated server JAR. */
public final class WorldlineRepeaterOfficialOracle {
    private static final long SEED = 17320110707L;

    private WorldlineRepeaterOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleRepeaterSaveHandler(SEED, "worldline-repeater"),
                "worldline-repeater", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleRepeaterScenario scenario = new OracleRepeaterScenario(world, SEED);
        RepeaterSubsystemEvidence evidence = RepeaterSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=14|"))
            throw new IllegalStateException("official repeater claim inventory drifted");
        scenario.emit();
    }
}
