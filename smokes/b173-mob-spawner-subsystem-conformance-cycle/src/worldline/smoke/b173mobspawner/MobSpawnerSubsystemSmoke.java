package worldline.smoke.b173mobspawner;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.MobSpawnerSubsystemEvidence;
import worldline.testkit.MobSpawnerSubsystemFixture;

/** Executes the public mob-spawner fixture against mapped Beta 1.7.3. */
public final class MobSpawnerSubsystemSmoke {
    private static final long SEED = 17320110752L;
    private MobSpawnerSubsystemSmoke() {
    }
    public static void main(String[] arguments) {
        MobSpawnerSubsystemBackend backend = new MobSpawnerSubsystemBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-mob-spawner")));
            runtime.tick();
            MobSpawnerSubsystemEvidence evidence = MobSpawnerSubsystemFixture.execute(backend);
            if (!evidence.canonical().contains("claims=7|"))
                throw new IllegalStateException("mob-spawner claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
