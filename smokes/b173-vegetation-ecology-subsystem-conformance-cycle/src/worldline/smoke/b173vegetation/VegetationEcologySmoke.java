package worldline.smoke.b173vegetation;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.VegetationEcologyEvidence;
import worldline.testkit.VegetationEcologyFixture;

/** Executes the public vegetation ecology fixture against mapped Beta 1.7.3. */
public final class VegetationEcologySmoke {
    private static final long SEED = 17320110870L;

    private VegetationEcologySmoke() { }

    public static void main(String[] arguments) {
        VegetationEcologyBackend backend = new VegetationEcologyBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-vegetation")));
            runtime.tick();
            VegetationEcologyEvidence evidence = VegetationEcologyFixture.execute(backend);
            if (!evidence.canonical().contains("claims=19|"))
                throw new IllegalStateException("vegetation claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
