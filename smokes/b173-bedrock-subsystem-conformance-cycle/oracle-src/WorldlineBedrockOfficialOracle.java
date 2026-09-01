import worldline.testkit.BedrockSubsystemEvidence;
import worldline.testkit.BedrockSubsystemFixture;

/** Complete bedrock subsystem against the untouched obfuscated server JAR. */
public final class WorldlineBedrockOfficialOracle {
    private static final long SEED = 17320110707L;
    private WorldlineBedrockOfficialOracle() { }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleBedrockSaveHandler(SEED, "worldline-bedrock"),
                "worldline-bedrock", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleBedrockScenario scenario = new OracleBedrockScenario(world, SEED);
        BedrockSubsystemEvidence evidence = BedrockSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=9|"))
            throw new IllegalStateException("official bedrock claim inventory drifted");
        scenario.emit();
    }
}
