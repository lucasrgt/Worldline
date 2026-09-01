package worldline.smoke.b173repeater;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.RepeaterSubsystemEvidence;
import worldline.testkit.RepeaterSubsystemFixture;

/** Executes the public repeater subsystem fixture against mapped Beta 1.7.3. */
public final class RepeaterSubsystemSmoke {
    private static final long SEED = 17320110707L;

    private RepeaterSubsystemSmoke() { }

    public static void main(String[] arguments) {
        RepeaterSubsystemBackend backend = new RepeaterSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-repeater")));
            runtime.tick();
            RepeaterSubsystemEvidence evidence = RepeaterSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=14|"))
                throw new IllegalStateException("repeater claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
