package worldline.smoke.b173builtenvironment;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.BuiltEnvironmentMaterialsEvidence;
import worldline.testkit.BuiltEnvironmentMaterialsFixture;

/** Executes the public construction-material fixture against mapped Beta 1.7.3. */
public final class BuiltEnvironmentMaterialsSmoke {
    private static final long SEED = 17320110850L;

    private BuiltEnvironmentMaterialsSmoke() { }

    public static void main(String[] arguments) {
        BuiltEnvironmentMaterialsBackend backend = new BuiltEnvironmentMaterialsBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-built-environment")));
            runtime.tick();
            BuiltEnvironmentMaterialsEvidence evidence =
                    BuiltEnvironmentMaterialsFixture.execute(backend);
            if (!evidence.canonical().contains("claims=47|"))
                throw new IllegalStateException("built-environment claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
