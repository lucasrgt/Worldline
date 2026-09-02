import worldline.testapi.FarmlandSubsystemEvidence;
import worldline.testapi.FarmlandSubsystemFixture;

/** Complete farmland subsystem against the untouched obfuscated server JAR. */
public final class WorldlineFarmlandOfficialOracle {
    private static final long SEED = 17320110660L;
    private WorldlineFarmlandOfficialOracle() {
    }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleFarmlandSaveHandler(SEED, "worldline-farmland"),
                "worldline-farmland", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleFarmlandScenario scenario = new OracleFarmlandScenario(world, SEED);
        FarmlandSubsystemEvidence evidence = FarmlandSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=8|"))
            throw new IllegalStateException("official farmland claim inventory drifted");
        scenario.emit();
    }
}
