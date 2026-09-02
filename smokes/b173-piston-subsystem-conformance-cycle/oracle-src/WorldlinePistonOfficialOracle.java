import worldline.testapi.PistonSubsystemEvidence;
import worldline.testapi.PistonSubsystemFixture;

/** Complete piston subsystem against the untouched obfuscated server JAR. */
public final class WorldlinePistonOfficialOracle {
    private static final long SEED = 17320110707L;

    private WorldlinePistonOfficialOracle() {
    }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OraclePistonSaveHandler(SEED, "worldline-piston"),
                "worldline-piston", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OraclePistonScenario scenario = new OraclePistonScenario(world, SEED);
        PistonSubsystemEvidence evidence = PistonSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=28|"))
            throw new IllegalStateException("official piston claim inventory drifted");
        scenario.emit();
    }
}
