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
    private static final int PLACE_HOTBAR = 1, PLACE_INVENTORY = 37;
    private static final int BREAK_HOTBAR = 2, BREAK_INVENTORY = 38;

    private B173ServerLifecycleFixtures() { }

    public static List<BlockLifecycleScenario> scenarios() {
        BlockConformancePlan plan = new BlockConformancePlan(Arrays.asList(
                profile("b1.7.3:block/004", "simple-solid", false),
                profile("b1.7.3:block/003", "shovel-soft", false),
                profile("b1.7.3:block/054", "container", true),
                profile("b1.7.3:block/001", "simple-solid", false),
                profile("b1.7.3:block/005", "simple-solid", false),
                profile("b1.7.3:block/024", "simple-solid", false),
                profile("b1.7.3:block/045", "simple-solid", false),
                profile("b1.7.3:block/014", "ore", false),
                profile("b1.7.3:block/015", "ore", false),
                profile("b1.7.3:block/016", "ore", false),
                profile("b1.7.3:block/022", "mineral-storage", false),
                profile("b1.7.3:block/041", "mineral-storage", false),
                profile("b1.7.3:block/042", "mineral-storage", false),
                profile("b1.7.3:block/056", "ore", false),
                profile("b1.7.3:block/057", "mineral-storage", false),
                profile("b1.7.3:block/049", "obsidian", false),
                profile("b1.7.3:block/066", "rail", false),
                profile("b1.7.3:block/027", "rail", false),
                profile("b1.7.3:block/028", "rail", false)),
                Arrays.asList(
                        template("gameplay-placement", ConformanceLayer.UNIVERSAL),
                        template("save-reload", ConformanceLayer.UNIVERSAL),
                        template("break-transition", ConformanceLayer.UNIVERSAL),
                        template("drop-matrix", ConformanceLayer.ARCHETYPE)));
        return Collections.unmodifiableList(Arrays.asList(
                scenario(plan, "cobblestone", "b1.7.3:block/004", 4, 4, 257, 15),
                scenario(plan, "dirt", "b1.7.3:block/003", 3, 3, 257, 5),
                scenario(plan, "empty-chest", "b1.7.3:block/054", 54, 54, 257, 80),
                scenario(plan, "stone", "b1.7.3:block/001", 1, 4, 257, 15),
                scenario(plan, "planks", "b1.7.3:block/005", 5, 5, 257, 80),
                scenario(plan, "sandstone", "b1.7.3:block/024", 24, 24, 257, 15),
                scenario(plan, "brick", "b1.7.3:block/045", 45, 45, 257, 15),
                scenario(plan, "gold-ore", "b1.7.3:block/014", 14, 14, 278, 20),
                scenario(plan, "iron-ore", "b1.7.3:block/015", 15, 15, 278, 20),
                scenario(plan, "coal-ore", "b1.7.3:block/016", 16, 263, 278, 20),
                scenario(plan, "lapis-block", "b1.7.3:block/022", 22, 22, 278, 20),
                scenario(plan, "gold-block", "b1.7.3:block/041", 41, 41, 278, 20),
                scenario(plan, "iron-block", "b1.7.3:block/042", 42, 42, 278, 20),
                scenario(plan, "diamond-ore", "b1.7.3:block/056", 56, 264, 278, 20),
                scenario(plan, "diamond-block", "b1.7.3:block/057", 57, 57, 278, 20),
                scenario(plan, "obsidian", "b1.7.3:block/049", 49, 49, 278, 60),
                scenario(plan, "rail", "b1.7.3:block/066", 66, 66, 278, 20),
                scenario(plan, "powered-rail", "b1.7.3:block/027", 27, 27, 278, 20),
                scenario(plan, "detector-rail", "b1.7.3:block/028", 28, 28, 278, 20)));
    }

    private static BlockLifecycleScenario scenario(BlockConformancePlan plan, String id,
            String subject, int block, int drop, int tool, int breakTicks) {
        RemoteItemStack placed = new RemoteItemStack(block, 1, 0);
        return BlockLifecycleScenario.from(id, plan, subject, B173LifecycleArena.SUPPORT,
                B173LifecycleArena.SUPPORT_STATE, BlockFace.UP, new BlockState(block, 0),
                new BlockLifecycleSlot(PLACE_HOTBAR, PLACE_INVENTORY, placed, null),
                new BlockLifecycleSlot(BREAK_HOTBAR, BREAK_INVENTORY,
                        new RemoteItemStack(tool, 1, 0), new RemoteItemStack(tool, 1, 1)),
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
