import worldline.testkit.LockedChestSubsystemEvidence;
import worldline.testkit.LockedChestSubsystemFixture;

/** Complete locked-chest subsystem against the untouched obfuscated server JAR. */
public final class WorldlineLockedChestOfficialOracle {
    private static final long SEED = 17320110795L;
    private WorldlineLockedChestOfficialOracle() {
    }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleLockedChestSaveHandler(SEED, "worldline-locked-chest"),
                "worldline-locked-chest", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleLockedChestScenario scenario = new OracleLockedChestScenario(world, SEED);
        LockedChestSubsystemEvidence evidence = LockedChestSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=9|"))
            throw new IllegalStateException("official locked-chest claim inventory drifted");
        scenario.emit();
    }
}
