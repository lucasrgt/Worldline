package worldline.smoke.b173redstoneinputs;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.RedstoneInputControlsSubsystemEvidence;
import worldline.testkit.RedstoneInputControlsSubsystemFixture;

/** Executes the public redstone input-control fixture against mapped Beta 1.7.3. */
public final class RedstoneInputControlsSmoke {
    private static final long SEED = 17320110690L;

    private RedstoneInputControlsSmoke() { }

    public static void main(String[] arguments) {
        RedstoneInputControlsBackend backend = new RedstoneInputControlsBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-redstone-inputs")));
            runtime.tick();
            RedstoneInputControlsSubsystemEvidence evidence =
                    RedstoneInputControlsSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=20|"))
                throw new IllegalStateException("redstone input-control claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
