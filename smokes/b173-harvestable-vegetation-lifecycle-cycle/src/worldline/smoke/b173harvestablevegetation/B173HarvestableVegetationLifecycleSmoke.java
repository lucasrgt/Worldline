package worldline.smoke.b173harvestablevegetation;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Shear-obtained leaf and tall-grass lifecycle family. */
public final class B173HarvestableVegetationLifecycleSmoke {
    private B173HarvestableVegetationLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "shear-harvested-foliage",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                leaves(), tallGrass()));
    }

    private static BlockLifecycleScenario leaves() {
        return B173LifecycleScenarioFactory.harvestOnSupport("leaves",
                "b1.7.3:block/018", "shear-harvested-vegetation", false,
                18, 0, 8, new BlockState(1, 0), 359, 1, 5,
                new RemoteItemStack(18, 1, 0));
    }

    private static BlockLifecycleScenario tallGrass() {
        return B173LifecycleScenarioFactory.harvestOnSupport("tall-grass",
                "b1.7.3:block/031", "shear-harvested-vegetation", false,
                31, 1, 0, new BlockState(3, 0), 359, 1, 1,
                new RemoteItemStack(31, 1, 0));
    }
}
