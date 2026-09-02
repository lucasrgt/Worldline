package worldline.smoke.b173railnetwork;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.RailNetworkSubsystemEvidence;
import worldline.testapi.RailNetworkSubsystemFixture;

/** Executes the public rail-network fixture against mapped Beta 1.7.3. */
public final class RailNetworkSubsystemSmoke {
    private static final long SEED = 17320110660L;

    private RailNetworkSubsystemSmoke() { }

    public static void main(String[] arguments) {
        RailNetworkSubsystemBackend backend = new RailNetworkSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-rail-network")));
            runtime.tick();
            RailNetworkSubsystemEvidence evidence = RailNetworkSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=14|"))
                throw new IllegalStateException("rail-network claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
