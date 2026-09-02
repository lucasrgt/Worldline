import worldline.testapi.RailNetworkSubsystemEvidence;
import worldline.testapi.RailNetworkSubsystemFixture;

/** Complete rail-network subsystem against the untouched official server JAR. */
public final class WorldlineRailNetworkOfficialOracle {
    private static final long SEED = 17320110660L;

    private WorldlineRailNetworkOfficialOracle() { }

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        dj world = new dj(new OracleRailNetworkMemorySaveHandler(
                SEED, "worldline-rail-network"), "worldline-rail-network", SEED, null);
        for (int chunkX = -4; chunkX <= 4; chunkX++)
            for (int chunkZ = -4; chunkZ <= 4; chunkZ++)
                world.c(chunkX, chunkZ);
        world.e();
        world.h();
        OracleRailNetworkSubsystemScenario scenario =
                new OracleRailNetworkSubsystemScenario(world, SEED);
        RailNetworkSubsystemEvidence evidence = RailNetworkSubsystemFixture.execute(scenario);
        if (!evidence.canonical().contains("claims=14|"))
            throw new IllegalStateException("official rail-network claim inventory drifted");
        scenario.emit();
    }
}
