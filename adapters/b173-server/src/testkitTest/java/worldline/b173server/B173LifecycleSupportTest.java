package worldline.b173server;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.BlockState;
import worldline.api.BlockFace;
import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockLifecyclePlan;
import worldline.testkit.BlockLifecycleNeighbor;
import worldline.testkit.BlockLifecycleSlot;
import worldline.testkit.BlockLifecycleScenario;

/** Static contract checks for scenario-declared lifecycle substrates and item variants. */
final class B173LifecycleSupportTest {
    private B173LifecycleSupportTest() { }

    static void verify() {
        Map<String, String> fixture = new LinkedHashMap<String, String>();
        fixture.put(BlockLifecyclePlan.PLACEMENT_SLOT_OPTION, "1:37:57:1:0");
        fixture.put(BlockLifecyclePlan.BREAK_SLOT_OPTION, "2:38:278:1:0");
        fixture.put(BlockLifecyclePlan.SUPPORT_STATE_OPTION, "3:0");
        fixture.put(BlockLifecyclePlan.OVERHEAD_STATE_OPTION, "1:0");
        fixture.put(BlockLifecyclePlan.NEIGHBOR_STATE_OPTION, "none");
        fixture.put(BlockLifecyclePlan.NEIGHBOR_FACE_OPTION, "none");
        fixture.put(BlockLifecyclePlan.NEIGHBOR_SLOT_OPTION, "none");
        B173LifecycleLoadout loadout = B173LifecycleLoadout.from(new TestRuntimeRequest(
                B173ServerLifecycleFixtures.SEED, Paths.get("."), null,
                "official block lifecycle > arbitrary-external-row", fixture));
        require(loadout.placement.legacyId() == 57 && loadout.tool.legacyId() == 278
                        && loadout.support.legacyId() == 3 && loadout.supportHotbar == 3
                        && loadout.overhead.equals(new BlockState(1, 0)),
                "runtime lifecycle options did not select their loadout");
        BlockLifecycleScenario variant = B173LifecycleScenarioFactory.harvestOnSupport(
                "spruce-sapling", "b1.7.3:block/006", "vegetation", false,
                6, 1, 1, new BlockState(3, 0), 280, 0, 1,
                new RemoteItemStack(6, 1, 1));
        require(variant.placementSlot().before().damage() == 1
                        && variant.placedState().metadata() == 1
                        && variant.supportState().equals(new BlockState(3, 0))
                        && variant.expectedDrops().get(0).damage() == 1,
                "supported variant lifecycle scenario drifted");
        BlockLifecycleScenario shaded = B173LifecycleScenarioFactory.harvestUnderOverhead(
                "brown-mushroom", "b1.7.3:block/039", "vegetation", false,
                39, 0, 0, new BlockState(3, 0), new BlockState(1, 0),
                280, 0, 1, new RemoteItemStack(39, 1, 0));
        require(shaded.overheadState().equals(new BlockState(1, 0))
                        && shaded.overhead().equals(new worldline.api.BlockPosition(4, 73, 4)),
                "overhead lifecycle scenario drifted");
        BlockLifecycleScenario cake = B173LifecycleScenarioFactory.harvestFromItem(
                "cake", "b1.7.3:block/092", "cake", true, 92, 354, 0, 0,
                new BlockState(1, 0), 280, 0, 1);
        require(cake.placementSlot().before().legacyId() == 354
                        && cake.placedState().legacyId() == 92
                        && cake.expectedDrops().isEmpty(),
                "distinct placement item lifecycle scenario drifted");
        BlockLifecycleNeighbor water = new BlockLifecycleNeighbor(BlockFace.EAST,
                new BlockState(9, 0), new BlockLifecycleSlot(4, 40,
                        new RemoteItemStack(9, 1, 0), null));
        BlockLifecycleScenario cane = B173LifecycleScenarioFactory.harvestBesideNeighbor(
                "sugar-cane", "b1.7.3:block/083", "vegetation", false,
                83, 338, 0, 0, new BlockState(3, 0), water,
                280, 0, 1, new RemoteItemStack(338, 1, 0));
        require(cane.neighbor().state().equals(new BlockState(9, 0))
                        && cane.neighborPosition().equals(
                                new worldline.api.BlockPosition(5, 71, 4))
                        && cane.placementSlot().before().legacyId() == 338,
                "neighbor-aware lifecycle scenario drifted");
        Map<String, String> hydratedFixture = new LinkedHashMap<String, String>(fixture);
        hydratedFixture.put(BlockLifecyclePlan.OVERHEAD_STATE_OPTION, "none");
        hydratedFixture.put(BlockLifecyclePlan.NEIGHBOR_STATE_OPTION, "9:0");
        hydratedFixture.put(BlockLifecyclePlan.NEIGHBOR_FACE_OPTION, "EAST");
        hydratedFixture.put(BlockLifecyclePlan.NEIGHBOR_SLOT_OPTION, "4:40:9:1:0");
        B173LifecycleLoadout hydratedLoadout = B173LifecycleLoadout.from(
                new TestRuntimeRequest(B173ServerLifecycleFixtures.SEED, Paths.get("."), null,
                        "official block lifecycle > hydrated-row", hydratedFixture));
        require(hydratedLoadout.neighborHotbar == 4
                        && hydratedLoadout.neighborItem.legacyId() == 9
                        && hydratedLoadout.neighborFace == BlockFace.EAST
                        && hydratedLoadout.neighborState.equals(new BlockState(9, 0)),
                "neighbor lifecycle options did not select their loadout");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
