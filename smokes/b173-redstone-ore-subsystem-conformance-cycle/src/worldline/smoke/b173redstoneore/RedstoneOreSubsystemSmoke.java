package worldline.smoke.b173redstoneore;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.RedstoneOreSubsystemEvidence;
import worldline.testkit.RedstoneOreSubsystemFixture;

/** Executes the public redstone-ore fixture against mapped Beta 1.7.3. */
public final class RedstoneOreSubsystemSmoke {
    private static final long SEED = 17320110707L;
    private RedstoneOreSubsystemSmoke() { }
    public static void main(String[] arguments) {
        RedstoneOreSubsystemBackend backend = new RedstoneOreSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-redstone-ore")));
            runtime.tick();
            RedstoneOreSubsystemEvidence evidence = RedstoneOreSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=13|"))
                throw new IllegalStateException("redstone ore claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
