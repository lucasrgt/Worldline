package worldline.smoke.b173slablifecycle;

import java.util.Arrays;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173LifecycleScenarioFactory;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.testkit.BlockLifecycleFamilyCycle;
import worldline.testkit.BlockLifecycleScenario;

/** Complete placement, persistence, and disassembly lifecycle for the Beta slab family. */
public final class B173SlabLifecycleSmoke {
    private static final BlockState STONE = new BlockState(1, 0);

    private B173SlabLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockLifecycleFamilyCycle.run(arguments, "slab-state", 17_320_110_707L,
                "worldline.b173.lifecycle.serverJar",
                new B173ServerLifecycleTestRuntimeProvider(), Arrays.asList(
                        slab("stone-slab", 0), slab("sandstone-slab", 1),
                        slab("wood-slab", 2), slab("cobblestone-slab", 3),
                        doubleSlab()));
    }

    private static BlockLifecycleScenario slab(String id, int metadata) {
        return B173LifecycleScenarioFactory.harvestOnSupport(id, "b1.7.3:block/044",
                "stateful-slab", false, 44, metadata, metadata, STONE,
                278, 1, 20, new RemoteItemStack(44, 1, metadata));
    }

    private static BlockLifecycleScenario doubleSlab() {
        return B173LifecycleScenarioFactory.harvestOnSupport("double-stone-slab",
                "b1.7.3:block/043", "stateful-slab", false, 43, 0, 0, STONE,
                278, 1, 20, new RemoteItemStack(44, 2, 0));
    }
}
