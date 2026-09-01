import worldline.testkit.FurnaceSubsystemEvidence;
import worldline.testkit.FurnaceSubsystemFixture;

/** Complete furnace subsystem against the untouched obfuscated server JAR. */
public final class WorldlineFurnaceOfficialOracle {
    private static final long SEED = 17320110707L;
    private WorldlineFurnaceOfficialOracle() {
    }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleFurnaceSaveHandler(SEED, "worldline-furnace"),
                "worldline-furnace", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleFurnaceScenario scenario = new OracleFurnaceScenario(world, SEED);
        FurnaceSubsystemEvidence evidence = FurnaceSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=11|idle:tick-policy+neighbor-response"))
            throw new IllegalStateException("official furnace claim inventory drifted");
        scenario.emit();
    }
}
