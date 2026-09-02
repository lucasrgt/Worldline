package worldline.smoke.b173bedrock;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.BedrockSubsystemEvidence;
import worldline.testapi.BedrockSubsystemFixture;

/** Executes the public bedrock fixture against mapped Beta 1.7.3. */
public final class BedrockSubsystemSmoke {
    private static final long SEED = 17320110707L;
    private BedrockSubsystemSmoke() { }
    public static void main(String[] arguments) {
        BedrockSubsystemBackend backend = new BedrockSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-bedrock")));
            runtime.tick();
            BedrockSubsystemEvidence evidence = BedrockSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=9|"))
                throw new IllegalStateException("bedrock claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
