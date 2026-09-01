import worldline.testkit.RedstoneInputControlsSubsystemEvidence;
import worldline.testkit.RedstoneInputControlsSubsystemFixture;

/** Complete redstone input-control subsystem against the untouched official server JAR. */
public final class WorldlineRedstoneInputControlsOfficialOracle {
    private static final long SEED = 17320110690L;

    private WorldlineRedstoneInputControlsOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleRedstoneInputsMemorySaveHandler(
                SEED, "worldline-redstone-inputs"),
                "worldline-redstone-inputs", SEED, null);
        for (int chunkX = -3; chunkX <= 3; chunkX++)
            for (int chunkZ = -3; chunkZ <= 3; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleRedstoneInputControlsScenario scenario =
                new OracleRedstoneInputControlsScenario(world, SEED);
        RedstoneInputControlsSubsystemEvidence evidence =
                RedstoneInputControlsSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=20|"))
            throw new IllegalStateException(
                    "official redstone input-control claim inventory drifted");
        scenario.emit();
    }
}
