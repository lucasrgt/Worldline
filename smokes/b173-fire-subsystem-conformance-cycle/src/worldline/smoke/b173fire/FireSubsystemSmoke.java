package worldline.smoke.b173fire;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.FireSubsystemEvidence;
import worldline.testkit.FireSubsystemFixture;

/** Executes the public fire fixture against mapped Beta 1.7.3. */
public final class FireSubsystemSmoke {
    private static final long SEED = 17320110510L;
    private FireSubsystemSmoke() {
    }
    public static void main(String[] arguments) {
        FireSubsystemBackend backend = new FireSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-fire")));
            runtime.tick();
            FireSubsystemEvidence evidence = FireSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=8|"))
                throw new IllegalStateException("fire claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
