package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleScenario;
import worldline.testapi.BlockLifecycleNeighbor;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockLifecycleDropMatrix;
import worldline.testapi.ConformanceLayer;

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
        return harvestOnSupport(id, subject, archetype, singular, block, 0, metadata,
                B173LifecycleArena.SUPPORT_STATE, tool, toolAfterDamage, breakTicks,
                expectedDrops);
    }

    public static BlockLifecycleScenario harvestOnSupport(String id, String subject,
            String archetype, boolean singular, int block, int placementDamage, int metadata,
            BlockState supportState, int tool, int toolAfterDamage, int breakTicks,
            RemoteItemStack... expectedDrops) {
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                block, metadata, supportState, null, null, BlockFace.UP,
                tool, toolAfterDamage, breakTicks,
                expectedDrops);
    }

    public static BlockLifecycleScenario harvestRepeatedDropOnSupport(String id, String subject,
            String archetype, boolean singular, int block, int placementDamage, int metadata,
            BlockState supportState, int tool, int toolAfterDamage, int breakTicks,
            RemoteItemStack drop, int minimumEntities, int maximumEntities) {
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                block, metadata, supportState, null, null, BlockFace.UP, tool,
                toolAfterDamage, breakTicks,
                BlockLifecycleDropMatrix.repeated(drop, minimumEntities, maximumEntities));
    }

    public static BlockLifecycleScenario harvestUnderOverhead(String id, String subject,
            String archetype, boolean singular, int block, int placementDamage, int metadata,
            BlockState supportState, BlockState overheadState, int tool, int toolAfterDamage,
            int breakTicks, RemoteItemStack... expectedDrops) {
        if (overheadState == null) throw new NullPointerException("overheadState");
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                block, metadata, supportState, overheadState, null, BlockFace.UP,
                tool, toolAfterDamage,
                breakTicks, expectedDrops);
    }

    public static BlockLifecycleScenario harvestFromItem(String id, String subject,
            String archetype, boolean singular, int block, int placementItem,
            int placementDamage, int metadata, BlockState supportState, int tool,
            int toolAfterDamage, int breakTicks, RemoteItemStack... expectedDrops) {
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                placementItem, metadata, supportState, null, null, BlockFace.UP,
                tool, toolAfterDamage,
                breakTicks, expectedDrops);
    }

    public static BlockLifecycleScenario harvestBesideNeighbor(String id, String subject,
            String archetype, boolean singular, int block, int placementItem,
            int placementDamage, int metadata, BlockState supportState,
            BlockLifecycleNeighbor neighbor, int tool, int toolAfterDamage, int breakTicks,
            RemoteItemStack... expectedDrops) {
        if (neighbor == null) throw new NullPointerException("neighbor");
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                placementItem, metadata, supportState, null, neighbor, BlockFace.UP, tool,
                toolAfterDamage, breakTicks, expectedDrops);
    }

    public static BlockLifecycleScenario harvestOnFace(String id, String subject,
            String archetype, boolean singular, int block, int placementItem,
            int placementDamage, int metadata, BlockState supportState, BlockFace face,
            int tool, int toolAfterDamage, int breakTicks,
            RemoteItemStack... expectedDrops) {
        if (face == null) throw new NullPointerException("face");
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                placementItem, metadata, supportState, null, null, face, tool,
                toolAfterDamage, breakTicks, expectedDrops);
    }

    private static BlockLifecycleScenario harvestInEnvironment(String id, String subject,
            String archetype, boolean singular, int block, int placementDamage,
            int placementItem, int metadata, BlockState supportState, BlockState overheadState,
            BlockLifecycleNeighbor neighbor, BlockFace face, int tool, int toolAfterDamage,
            int breakTicks,
            RemoteItemStack... expectedDrops) {
        if (expectedDrops == null) throw new NullPointerException("expectedDrops");
        return harvestInEnvironment(id, subject, archetype, singular, block, placementDamage,
                placementItem, metadata, supportState, overheadState, neighbor, face, tool,
                toolAfterDamage, breakTicks,
                BlockLifecycleDropMatrix.exact(Arrays.asList(expectedDrops)));
    }

    private static BlockLifecycleScenario harvestInEnvironment(String id, String subject,
            String archetype, boolean singular, int block, int placementDamage,
            int placementItem, int metadata, BlockState supportState, BlockState overheadState,
            BlockLifecycleNeighbor neighbor, BlockFace face, int tool, int toolAfterDamage,
            int breakTicks, BlockLifecycleDropMatrix dropMatrix) {
        BlockConformanceProfile profile = new BlockConformanceProfile(subject,
                Collections.singletonList(archetype), singular,
                Collections.<String, ConformanceLayer>emptyMap());
        BlockConformancePlan plan = new BlockConformancePlan(
                Collections.singletonList(profile), Arrays.asList(
                        new BlockConformanceTemplate("gameplay-placement", ConformanceLayer.UNIVERSAL),
                        new BlockConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                        new BlockConformanceTemplate("break-transition", ConformanceLayer.UNIVERSAL),
                        new BlockConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE)));
        RemoteItemStack placed = new RemoteItemStack(placementItem, 1, placementDamage);
        BlockLifecycleSlot placementSlot = new BlockLifecycleSlot(1, 37, placed, null);
        BlockLifecycleSlot breakSlot = new BlockLifecycleSlot(2, 38,
                new RemoteItemStack(tool, 1, 0),
                new RemoteItemStack(tool, 1, toolAfterDamage));
        return new BlockLifecycleScenario(id,
                plan.caseFor(subject, "gameplay-placement"),
                plan.caseFor(subject, "save-reload"),
                plan.caseFor(subject, "break-transition"),
                plan.caseFor(subject, "drop-matrix"), B173LifecycleArena.SUPPORT,
                supportState, overheadState, neighbor, face, new BlockState(block, metadata),
                placementSlot, breakSlot, dropMatrix, breakTicks, 40);
    }
}
