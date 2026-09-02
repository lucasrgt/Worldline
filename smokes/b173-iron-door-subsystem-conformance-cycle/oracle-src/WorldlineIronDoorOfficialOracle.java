import worldline.testapi.IronDoorSubsystemEvidence;
import worldline.testapi.IronDoorSubsystemFixture;

/** Complete iron-door subsystem against the untouched obfuscated server JAR. */
public final class WorldlineIronDoorOfficialOracle {
    private static final long SEED = 17320110771L;
    private WorldlineIronDoorOfficialOracle() { }
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleIronDoorSaveHandler(SEED, "worldline-iron-door"),
                "worldline-iron-door", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleIronDoorScenario scenario = new OracleIronDoorScenario(world, SEED);
        IronDoorSubsystemEvidence evidence = IronDoorSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=7|"))
            throw new IllegalStateException("official iron-door claim inventory drifted");
        scenario.emit();
    }
}
