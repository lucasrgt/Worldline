package worldline.smoke.b173lockedchest;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.LockedChestSubsystemEvidence;
import worldline.testkit.LockedChestSubsystemFixture;

/** Executes the public locked-chest fixture against mapped Beta 1.7.3. */
public final class LockedChestSubsystemSmoke {
    private static final long SEED = 17320110795L;
    private LockedChestSubsystemSmoke() {
    }
    public static void main(String[] arguments) {
        LockedChestSubsystemBackend backend = new LockedChestSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-locked-chest")));
            runtime.tick();
            LockedChestSubsystemEvidence evidence = LockedChestSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=9|"))
                throw new IllegalStateException("locked-chest claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
