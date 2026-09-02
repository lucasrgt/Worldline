import worldline.testapi.RedstoneOreSubsystemEvidence;
import worldline.testapi.RedstoneOreSubsystemFixture;

/** Complete redstone-ore subsystem against the untouched obfuscated server JAR. */
public final class WorldlineRedstoneOreOfficialOracle {
    private static final long SEED = 17320110707L;
    private WorldlineRedstoneOreOfficialOracle() { }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleRedstoneOreSaveHandler(SEED, "worldline-redstone-ore"),
                "worldline-redstone-ore", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleRedstoneOreScenario scenario = new OracleRedstoneOreScenario(world, SEED);
        RedstoneOreSubsystemEvidence evidence = RedstoneOreSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=13|"))
            throw new IllegalStateException("official redstone ore claim inventory drifted");
        scenario.emit();
    }
}
