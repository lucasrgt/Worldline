package worldline.b173server;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockLifecyclePlan;
import worldline.testkit.BlockLifecycleScenario;

/** Static contract checks for scenario-declared lifecycle substrates and item variants. */
final class B173LifecycleSupportTest {
    private B173LifecycleSupportTest() { }

    static void verify() {
        Map<String, String> fixture = new LinkedHashMap<String, String>();
        fixture.put(BlockLifecyclePlan.PLACEMENT_SLOT_OPTION, "1:37:57:1:0");
        fixture.put(BlockLifecyclePlan.BREAK_SLOT_OPTION, "2:38:278:1:0");
        fixture.put(BlockLifecyclePlan.SUPPORT_STATE_OPTION, "3:0");
        B173LifecycleLoadout loadout = B173LifecycleLoadout.from(new TestRuntimeRequest(
                B173ServerLifecycleFixtures.SEED, Paths.get("."), null,
                "official block lifecycle > arbitrary-external-row", fixture));
        require(loadout.placement.legacyId() == 57 && loadout.tool.legacyId() == 278
                        && loadout.support.legacyId() == 3 && loadout.supportHotbar == 3,
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
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
