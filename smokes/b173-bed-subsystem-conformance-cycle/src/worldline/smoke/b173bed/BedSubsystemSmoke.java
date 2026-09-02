package worldline.smoke.b173bed;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.BedSubsystemEvidence;
import worldline.testapi.BedSubsystemFixture;

/** Executes the public bed fixture against mapped Beta 1.7.3. */
public final class BedSubsystemSmoke {
    private static final long SEED = 17320110726L;
    private BedSubsystemSmoke() { }
    public static void main(String[] arguments) {
        BedSubsystemBackend backend = new BedSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-bed")));
            runtime.tick();
            BedSubsystemEvidence evidence = BedSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=7|"))
                throw new IllegalStateException("bed claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
