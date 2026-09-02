package worldline.smoke.b173fluidfrozen;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testapi.FluidFrozenMatterEvidence;
import worldline.testapi.FluidFrozenMatterFixture;

/** Executes the public fluid and frozen-matter fixture against mapped Beta 1.7.3. */
public final class FluidFrozenMatterSmoke {
    private static final long SEED = 17320110872L;

    private FluidFrozenMatterSmoke() { }

    public static void main(String[] arguments) {
        FluidFrozenMatterBackend backend = new FluidFrozenMatterBackend(SEED);
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-fluid-frozen")));
            runtime.tick();
            FluidFrozenMatterEvidence evidence = FluidFrozenMatterFixture.execute(backend);
            if (!evidence.canonical().contains("claims=21|"))
                throw new IllegalStateException("fluid and frozen-matter claim inventory drifted");
            backend.emit();
        } finally {
            runtime.close();
        }
    }
}
