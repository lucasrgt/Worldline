package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockLifecycleScenario;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.ConformanceLayer;

/** Builds external official-server lifecycle rows without a provider-owned catalog. */
public final class B173LifecycleScenarioFactory {
    private B173LifecycleScenarioFactory() { }

    public static BlockLifecycleScenario selfDrop(String id, String subject, String archetype,
            boolean singular, int block, int metadata, int tool, int breakTicks) {
        return selfDrop(id, subject, archetype, singular, block, metadata, tool, 1, breakTicks);
    }

    public static BlockLifecycleScenario selfDrop(String id, String subject, String archetype,
            boolean singular, int block, int metadata, int tool, int toolAfterDamage,
            int breakTicks) {
        return harvest(id, subject, archetype, singular, block, metadata, tool,
                toolAfterDamage, breakTicks, new RemoteItemStack(block, 1, 0));
    }

    public static BlockLifecycleScenario harvest(String id, String subject, String archetype,
            boolean singular, int block, int metadata, int tool, int toolAfterDamage,
            int breakTicks, RemoteItemStack... expectedDrops) {
        if (expectedDrops == null) throw new NullPointerException("expectedDrops");
        BlockConformanceProfile profile = new BlockConformanceProfile(subject,
                Collections.singletonList(archetype), singular,
                Collections.<String, ConformanceLayer>emptyMap());
        BlockConformancePlan plan = new BlockConformancePlan(
                Collections.singletonList(profile), Arrays.asList(
                        new BlockConformanceTemplate("gameplay-placement", ConformanceLayer.UNIVERSAL),
                        new BlockConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                        new BlockConformanceTemplate("break-transition", ConformanceLayer.UNIVERSAL),
                        new BlockConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE)));
        RemoteItemStack placed = new RemoteItemStack(block, 1, 0);
        return BlockLifecycleScenario.from(id, plan, subject, B173LifecycleArena.SUPPORT,
                B173LifecycleArena.SUPPORT_STATE, BlockFace.UP, new BlockState(block, metadata),
                new BlockLifecycleSlot(1, 37, placed, null),
                new BlockLifecycleSlot(2, 38, new RemoteItemStack(tool, 1, 0),
                        new RemoteItemStack(tool, 1, toolAfterDamage)),
                Arrays.asList(expectedDrops), breakTicks, 40);
    }
}
