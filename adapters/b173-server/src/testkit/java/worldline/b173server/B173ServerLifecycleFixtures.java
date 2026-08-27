package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleScenario;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.ConformanceLayer;

/** The exact official b1.7.3 lifecycle rows currently provisioned by the provider. */
public final class B173ServerLifecycleFixtures {
    public static final long SEED = B173LifecycleArena.SEED;

    private B173ServerLifecycleFixtures() { }

    public static List<BlockLifecycleScenario> scenarios() {
        BlockConformancePlan plan = new BlockConformancePlan(Arrays.asList(
                profile("b1.7.3:block/004", "simple-solid", false),
                profile("b1.7.3:block/003", "shovel-soft", false),
                profile("b1.7.3:block/054", "container", true),
                profile("b1.7.3:block/001", "simple-solid", false),
                profile("b1.7.3:block/005", "simple-solid", false),
                profile("b1.7.3:block/024", "simple-solid", false),
                profile("b1.7.3:block/045", "simple-solid", false)),
                Arrays.asList(
                        template("gameplay-placement", ConformanceLayer.UNIVERSAL),
                        template("save-reload", ConformanceLayer.UNIVERSAL),
                        template("break-transition", ConformanceLayer.UNIVERSAL),
                        template("drop-matrix", ConformanceLayer.ARCHETYPE)));
        return Collections.unmodifiableList(Arrays.asList(
                scenario(plan, "cobblestone", "b1.7.3:block/004", 4, 4, 1, 37, 15),
                scenario(plan, "dirt", "b1.7.3:block/003", 3, 3, 2, 38, 5),
                scenario(plan, "empty-chest", "b1.7.3:block/054", 54, 54, 3, 39, 80),
                scenario(plan, "stone", "b1.7.3:block/001", 1, 4, 4, 40, 15),
                scenario(plan, "planks", "b1.7.3:block/005", 5, 5, 5, 41, 80),
                scenario(plan, "sandstone", "b1.7.3:block/024", 24, 24, 6, 42, 15),
                scenario(plan, "brick", "b1.7.3:block/045", 45, 45, 7, 43, 15)));
    }

    private static BlockLifecycleScenario scenario(BlockConformancePlan plan, String id,
            String subject, int block, int drop, int blockHotbar, int blockInventory,
            int breakTicks) {
        RemoteItemStack placed = new RemoteItemStack(block, 1, 0);
        return BlockLifecycleScenario.from(id, plan, subject, B173LifecycleArena.SUPPORT,
                B173LifecycleArena.SUPPORT_STATE, BlockFace.UP, new BlockState(block, 0),
                new BlockLifecycleSlot(blockHotbar, blockInventory, placed, null),
                new BlockLifecycleSlot(8, 44,
                        new RemoteItemStack(257, 1, 0), new RemoteItemStack(257, 1, 1)),
                Collections.singletonList(new RemoteItemStack(drop, 1, 0)), breakTicks, 40);
    }

    private static BlockConformanceProfile profile(String subject, String archetype,
            boolean singular) {
        return new BlockConformanceProfile(subject, Collections.singletonList(archetype),
                singular, Collections.<String, ConformanceLayer>emptyMap());
    }

    private static BlockConformanceTemplate template(String id, ConformanceLayer layer) {
        return new BlockConformanceTemplate(id, layer);
    }
}
