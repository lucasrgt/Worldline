package worldline.smoke.b173redstonesignals;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.RedstoneSignalConsumersEvidence;
import worldline.testapi.RedstoneSignalConsumersFixture;

/** Executes the public redstone signal-consumer fixture against mapped Beta 1.7.3. */
public final class RedstoneSignalConsumersSmoke {
    private static final long SEED = 17320110855L;

    private RedstoneSignalConsumersSmoke() {
    }

    public static void main(String[] arguments) {
        RedstoneSignalConsumersBackend backend = new RedstoneSignalConsumersBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-redstone-signals")));
            runtime.tick();
            RedstoneSignalConsumersEvidence evidence = RedstoneSignalConsumersFixture.execute(backend);
            if (!evidence.canonical().contains("claims=18|"))
                throw new IllegalStateException("redstone signal-consumer claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
