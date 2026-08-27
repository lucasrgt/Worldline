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
                profile("b1.7.3:block/054", "container", true)),
                Arrays.asList(
                        template("gameplay-placement", ConformanceLayer.UNIVERSAL),
                        template("save-reload", ConformanceLayer.UNIVERSAL),
                        template("break-transition", ConformanceLayer.UNIVERSAL),
                        template("drop-matrix", ConformanceLayer.ARCHETYPE)));
        return Collections.unmodifiableList(Arrays.asList(
                scenario(plan, "cobblestone", "b1.7.3:block/004", 4, 1, 37,
                        257, 4, 40, 15),
                scenario(plan, "dirt", "b1.7.3:block/003", 3, 2, 38,
                        284, 5, 41, 5),
                scenario(plan, "empty-chest", "b1.7.3:block/054", 54, 3, 39,
                        286, 6, 42, 12)));
    }

    private static BlockLifecycleScenario scenario(BlockConformancePlan plan, String id,
            String subject, int block, int blockHotbar, int blockInventory,
            int tool, int toolHotbar, int toolInventory, int breakTicks) {
        RemoteItemStack placed = new RemoteItemStack(block, 1, 0);
        return BlockLifecycleScenario.from(id, plan, subject, B173LifecycleArena.SUPPORT,
                B173LifecycleArena.SUPPORT_STATE, BlockFace.UP, new BlockState(block, 0),
                new BlockLifecycleSlot(blockHotbar, blockInventory, placed, null),
                new BlockLifecycleSlot(toolHotbar, toolInventory,
                        new RemoteItemStack(tool, 1, 0), new RemoteItemStack(tool, 1, 1)),
                Collections.singletonList(placed), breakTicks, 40);
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
