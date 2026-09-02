package worldline.smoke.b173farmland;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.FarmlandSubsystemEvidence;
import worldline.testapi.FarmlandSubsystemFixture;

/** Executes the public farmland fixture against mapped Beta 1.7.3. */
public final class FarmlandSubsystemSmoke {
    private static final long SEED = 17320110660L;
    private FarmlandSubsystemSmoke() {
    }
    public static void main(String[] arguments) {
        FarmlandSubsystemBackend backend = new FarmlandSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-farmland")));
            runtime.tick();
            FarmlandSubsystemEvidence evidence = FarmlandSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=8|"))
                throw new IllegalStateException("farmland claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
