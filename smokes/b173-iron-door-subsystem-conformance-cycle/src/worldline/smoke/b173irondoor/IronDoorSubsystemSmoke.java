package worldline.smoke.b173irondoor;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.IronDoorSubsystemEvidence;
import worldline.testkit.IronDoorSubsystemFixture;

/** Executes the public iron-door fixture against mapped Beta 1.7.3. */
public final class IronDoorSubsystemSmoke {
    private static final long SEED = 17320110771L;
    private IronDoorSubsystemSmoke() { }
    public static void main(String[] arguments) {
        IronDoorSubsystemBackend backend = new IronDoorSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-iron-door")));
            runtime.tick();
            IronDoorSubsystemEvidence evidence = IronDoorSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=7|"))
                throw new IllegalStateException("iron-door claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
