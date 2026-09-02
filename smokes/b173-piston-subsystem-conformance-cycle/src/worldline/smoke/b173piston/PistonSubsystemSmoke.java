package worldline.smoke.b173piston;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.PistonSubsystemEvidence;
import worldline.testapi.PistonSubsystemFixture;

/** Executes the public piston subsystem fixture against mapped Beta 1.7.3. */
public final class PistonSubsystemSmoke {
    private static final long SEED = 17320110707L;

    private PistonSubsystemSmoke() {
    }

    public static void main(String[] arguments) {
        PistonSubsystemBackend backend = new PistonSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-piston")));
            runtime.tick();
            PistonSubsystemEvidence evidence = PistonSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=28|"))
                throw new IllegalStateException("piston claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
