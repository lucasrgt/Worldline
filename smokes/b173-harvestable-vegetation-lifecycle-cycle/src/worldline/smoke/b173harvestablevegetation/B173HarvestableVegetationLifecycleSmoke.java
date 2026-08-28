package worldline.smoke.b173harvestablevegetation;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Shear-obtained leaf-variant lifecycle family. */
public final class B173HarvestableVegetationLifecycleSmoke {
    private B173HarvestableVegetationLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "shear-harvested-leaf-variants",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                leaves("oak-leaves", 0),
                leaves("spruce-leaves", 1),
                leaves("birch-leaves", 2)));
    }

    private static BlockLifecycleScenario leaves(String scenario, int variant) {
        return B173LifecycleScenarioFactory.harvestOnSupport(scenario,
                "b1.7.3:block/018", "shear-harvested-vegetation", false,
                18, variant, variant | 8, new BlockState(1, 0), 359, 1, 5,
                new RemoteItemStack(18, 1, variant));
    }
}
