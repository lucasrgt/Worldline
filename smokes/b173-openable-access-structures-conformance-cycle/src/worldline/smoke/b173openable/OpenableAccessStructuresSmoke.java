package worldline.smoke.b173openable;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.OpenableAccessStructuresEvidence;
import worldline.testapi.OpenableAccessStructuresFixture;

/** Executes the public openable access structures fixture against mapped Beta 1.7.3. */
public final class OpenableAccessStructuresSmoke {
    private static final long SEED = 17320110900L;

    private OpenableAccessStructuresSmoke() { }

    public static void main(String[] arguments) {
        OpenableAccessStructuresBackend backend = new OpenableAccessStructuresBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-openable-access")));
            runtime.tick();
            OpenableAccessStructuresEvidence evidence = OpenableAccessStructuresFixture.execute(backend);
            if (!evidence.canonical().contains("claims=10|"))
                throw new IllegalStateException("openable access claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
