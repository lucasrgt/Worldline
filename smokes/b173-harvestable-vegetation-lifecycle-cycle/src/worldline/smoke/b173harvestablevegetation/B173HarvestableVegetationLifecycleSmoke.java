package worldline.smoke.b173harvestablevegetation;

import java.util.Arrays;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleNeighbor;
import worldline.testkit.BlockLifecycleScenario;
import worldline.testkit.BlockLifecycleSlot;

/** Shear-obtained vegetation and one hydrated age-zero crop lifecycle family. */
public final class B173HarvestableVegetationLifecycleSmoke {
    private B173HarvestableVegetationLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "harvestable-vegetation",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                leaves(), tallGrass(), deadBush(), crops()));
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
                31, 1, 1, new BlockState(3, 0), 359, 1, 1,
                new RemoteItemStack(31, 1, 1));
    }

    private static BlockLifecycleScenario deadBush() {
        return B173LifecycleScenarioFactory.harvestOnSupport("dead-bush",
                "b1.7.3:block/032", "shear-harvested-vegetation", false,
                32, 0, 0, new BlockState(12, 0), 359, 1, 1,
                new RemoteItemStack(32, 1, 0));
    }

    private static BlockLifecycleScenario crops() {
        BlockLifecycleNeighbor water = new BlockLifecycleNeighbor(BlockFace.EAST,
                new BlockState(9, 0), new BlockLifecycleSlot(4, 40,
                        new RemoteItemStack(9, 1, 0), null));
        return B173LifecycleScenarioFactory.harvestBesideNeighbor("crops",
                "b1.7.3:block/059", "cultivated-crop", false,
                59, 295, 0, 0, new BlockState(60, 7), water,
                280, 0, 1, new RemoteItemStack(295, 1, 0));
    }
}
