package worldline.smoke.b173dirtfloralifecycle;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Dirt-supported flowers and sapling variants over the public lifecycle provider. */
public final class B173DirtFloraLifecycleSmoke {
    private static final BlockState DIRT = new BlockState(3, 0);
    private static final int STICK = 280;

    private B173DirtFloraLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "dirt-flora", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                row("dandelion", "037", 37, 0), row("rose", "038", 38, 0),
                row("oak-sapling", "006", 6, 0), row("spruce-sapling", "006", 6, 1),
                row("birch-sapling", "006", 6, 2)));
    }

    private static BlockLifecycleScenario row(String id, String legacy,
            int block, int damage) {
        return B173LifecycleScenarioFactory.harvestOnSupport(id,
                "b1.7.3:block/" + legacy, "vegetation", false, block, damage, damage,
                DIRT, STICK, 0, 1, new RemoteItemStack(block, 1, damage));
    }
}
