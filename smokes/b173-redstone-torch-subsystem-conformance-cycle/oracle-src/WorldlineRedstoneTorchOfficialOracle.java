import worldline.testkit.RedstoneTorchSubsystemEvidence;
import worldline.testkit.RedstoneTorchSubsystemFixture;

/** Complete redstone torch subsystem against the untouched obfuscated server JAR. */
public final class WorldlineRedstoneTorchOfficialOracle {
    private static final long SEED = 17320110707L;
    private WorldlineRedstoneTorchOfficialOracle() {
    }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleRedstoneTorchSaveHandler(SEED, "worldline-redstone-torch"),
                "worldline-redstone-torch", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleRedstoneTorchScenario scenario = new OracleRedstoneTorchScenario(world, SEED);
        RedstoneTorchSubsystemEvidence evidence = RedstoneTorchSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=13|"))
            throw new IllegalStateException("official redstone torch claim inventory drifted");
        scenario.emit();
    }
}
