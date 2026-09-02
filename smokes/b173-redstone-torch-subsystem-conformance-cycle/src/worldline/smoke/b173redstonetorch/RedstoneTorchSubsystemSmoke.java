package worldline.smoke.b173redstonetorch;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.RedstoneTorchSubsystemEvidence;
import worldline.testapi.RedstoneTorchSubsystemFixture;

/** Executes the public redstone torch fixture against mapped Beta 1.7.3. */
public final class RedstoneTorchSubsystemSmoke {
    private static final long SEED = 17320110707L;
    private RedstoneTorchSubsystemSmoke() {
    }
    public static void main(String[] arguments) {
        RedstoneTorchSubsystemBackend backend = new RedstoneTorchSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-redstone-torch")));
            runtime.tick();
            RedstoneTorchSubsystemEvidence evidence = RedstoneTorchSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=13|"))
                throw new IllegalStateException("redstone torch claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
