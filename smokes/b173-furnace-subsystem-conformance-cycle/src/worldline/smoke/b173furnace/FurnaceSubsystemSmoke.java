package worldline.smoke.b173furnace;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.FurnaceSubsystemEvidence;
import worldline.testapi.FurnaceSubsystemFixture;

/** Executes the public furnace fixture against mapped Beta 1.7.3. */
public final class FurnaceSubsystemSmoke {
    private static final long SEED = 17320110707L;
    private FurnaceSubsystemSmoke() {
    }
    public static void main(String[] arguments) {
        FurnaceSubsystemBackend backend = new FurnaceSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-furnace")));
            runtime.tick();
            FurnaceSubsystemEvidence evidence = FurnaceSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=11|idle:tick-policy+neighbor-response"))
                throw new IllegalStateException("furnace claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
