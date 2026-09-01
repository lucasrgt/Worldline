import worldline.testkit.RedstoneSignalConsumersEvidence;
import worldline.testkit.RedstoneSignalConsumersFixture;

/** Signal-consumer matrix against the untouched official server JAR. */
public final class WorldlineRedstoneSignalConsumersOfficialOracle {
    private static final long SEED = 17320110855L;

    private WorldlineRedstoneSignalConsumersOfficialOracle() {
    }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleRedstoneSignalMemorySaveHandler(SEED, "worldline-redstone-signals"),
                "worldline-redstone-signals", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleRedstoneSignalConsumersScenario scenario =
                new OracleRedstoneSignalConsumersScenario(world, SEED);
        RedstoneSignalConsumersEvidence evidence = RedstoneSignalConsumersFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=18|"))
            throw new IllegalStateException("official redstone signal-consumer claim inventory drifted");
        scenario.emit();
    }
}
