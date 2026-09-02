package worldline.smoke.b173shadedmushroomlifecycle;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testapi.BlockLifecycleScenario;

/** Brown and red mushrooms under a gameplay-provisioned stone canopy. */
public final class B173ShadedMushroomLifecycleSmoke {
    private static final BlockState DIRT = new BlockState(3, 0);
    private static final BlockState STONE_OVERHEAD = new BlockState(1, 0);
    private static final int STICK = 280;

    private B173ShadedMushroomLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "shaded-mushrooms", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                        row("brown-mushroom", "039", 39),
                        row("red-mushroom", "040", 40)));
    }

    private static BlockLifecycleScenario row(String id, String legacy, int block) {
        return B173LifecycleScenarioFactory.harvestUnderOverhead(id,
                "b1.7.3:block/" + legacy, "vegetation", false, block, 0, 0,
                DIRT, STONE_OVERHEAD, STICK, 0, 1,
                new RemoteItemStack(block, 1, 0));
    }
}
