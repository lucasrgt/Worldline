package worldline.smoke.b173supportplants;

import java.util.Arrays;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testapi.BlockLifecycleNeighbor;
import worldline.testapi.BlockLifecycleScenario;
import worldline.testapi.BlockLifecycleSlot;

/** Support- and hydration-sensitive cactus and sugar-cane gameplay lifecycles. */
public final class B173SupportDependentPlantLifecycleSmoke {
    private B173SupportDependentPlantLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "support-dependent-plants",
                17_320_110_707L, "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(),
                Arrays.asList(cactus(), sugarCane()));
    }

    private static BlockLifecycleScenario cactus() {
        return B173LifecycleScenarioFactory.harvestOnSupport("cactus",
                "b1.7.3:block/081", "support-dependent-plant", false,
                81, 0, 0, new BlockState(12, 0), 280, 0, 1,
                new RemoteItemStack(81, 1, 0));
    }

    private static BlockLifecycleScenario sugarCane() {
        BlockLifecycleNeighbor water = new BlockLifecycleNeighbor(BlockFace.EAST,
                new BlockState(9, 0), new BlockLifecycleSlot(4, 40,
                        new RemoteItemStack(9, 1, 0), null));
        return B173LifecycleScenarioFactory.harvestBesideNeighbor("sugar-cane",
                "b1.7.3:block/083", "support-dependent-plant", false,
                83, 338, 0, 0, new BlockState(3, 0), water,
                280, 0, 1, new RemoteItemStack(338, 1, 0));
    }
}
