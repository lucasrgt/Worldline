package worldline.smoke.b173portalblock;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.PortalBlockSubsystemEvidence;
import worldline.testkit.PortalBlockSubsystemFixture;

/** Executes the public portal-block fixture against mapped Beta 1.7.3. */
public final class PortalBlockSubsystemSmoke {
    private static final long SEED = 17320110707L;
    private PortalBlockSubsystemSmoke() { }
    public static void main(String[] arguments) {
        PortalBlockSubsystemBackend backend = new PortalBlockSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-portal-block")));
            runtime.tick();
            PortalBlockSubsystemEvidence evidence = PortalBlockSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=9|"))
                throw new IllegalStateException("portal block claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
