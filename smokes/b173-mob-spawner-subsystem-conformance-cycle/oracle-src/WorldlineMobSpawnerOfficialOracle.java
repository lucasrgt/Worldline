import worldline.testkit.MobSpawnerSubsystemEvidence;
import worldline.testkit.MobSpawnerSubsystemFixture;

/** Complete mob-spawner subsystem against the untouched obfuscated server JAR. */
public final class WorldlineMobSpawnerOfficialOracle {
    private static final long SEED = 17320110752L;
    private WorldlineMobSpawnerOfficialOracle() {
    }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleMobSpawnerSaveHandler(SEED, "worldline-mob-spawner"),
                "worldline-mob-spawner", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleMobSpawnerScenario scenario = new OracleMobSpawnerScenario(world, SEED);
        MobSpawnerSubsystemEvidence evidence = MobSpawnerSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=7|"))
            throw new IllegalStateException("official mob-spawner claim inventory drifted");
        scenario.emit();
    }
}
