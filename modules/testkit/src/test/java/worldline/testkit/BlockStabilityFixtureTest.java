package worldline.testkit;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockStabilityEvidence;
import worldline.testapi.BlockStabilityFixture;
import worldline.testapi.BlockStabilityScenario;
import worldline.testapi.ConformanceLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Proves the bounded window and direct-neighbor-removal action order. */
public final class BlockStabilityFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 64, 4);
    private static final BlockPosition TARGET = new BlockPosition(4, 65, 4);
    private static final BlockPosition OVERHEAD = new BlockPosition(4, 66, 4);
    private static final BlockState STONE = new BlockState(1, 0);
    private static final BlockState COBBLE = new BlockState(4, 0);
    private BlockStabilityFixtureTest() { }

    public static void main(String[] arguments) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile("b1.7.3:block/004",
                        Collections.singletonList("simple-solid"), false,
                        Collections.<String, ConformanceLayer>emptyMap())), Arrays.asList(
                new BlockConformanceTemplate("tick-policy", ConformanceLayer.ARCHETYPE),
                new BlockConformanceTemplate("neighbor-response", ConformanceLayer.ARCHETYPE)));
        BlockStabilityScenario scenario = new BlockStabilityScenario("cobble-stability",
                plan.caseFor("b1.7.3:block/004", "tick-policy"),
                plan.caseFor("b1.7.3:block/004", "neighbor-response"), SUPPORT, STONE,
                COBBLE, STONE, slot(1, 4, 0, -1), slot(2, 278, 0, 1), 200, 20, 40);
        FakeDriver driver = new FakeDriver();
        BlockStabilityEvidence evidence = BlockStabilityFixture.execute(scenario, driver);
        require(evidence.boundary() == BlockLifecycleDriver.ReloadBoundary.FRESH_LOGIN,
                "stability reload boundary drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-stability-evidence.v1\n"
                + "scenario=cobble-stability\nsubject=b1.7.3:block/004\n"
                + "claim.tick-policy=b1.7.3:block/004#tick-policy|ARCHETYPE\n"
                + "claim.neighbor-response=b1.7.3:block/004#neighbor-response|ARCHETYPE\n"
                + "support=4:64:4:1:0\ntarget=4:65:4:4:0\n"
                + "neighbor=4:66:4:1:0->0:0\ntick-window=200\n"
                + "neighbor-break-ticks=20\nneighbor-observation-ticks=40\n"
                + "reload=FRESH_LOGIN\n"), "canonical stability evidence drifted");
        require(driver.actions.equals(Arrays.asList("await:4:64:4:1:0", "await:4:65:4:0:0",
                "await:4:66:4:1:0", "inventory", "select:1", "place", "await:4:65:4:4:0",
                "inventory", "ticks:200", "await:4:65:4:4:0", "await:4:66:4:1:0",
                "inventory", "select:2", "begin:4:66:4", "ticks:20", "finish:4:66:4",
                "await:4:66:4:0:0", "ticks:40", "await:4:65:4:4:0", "inventory",
                "reload", "await:4:65:4:4:0", "await:4:66:4:0:0")),
                "stability action order drifted: " + driver.actions);
        rejects(() -> new BlockStabilityScenario("bad", scenario.tickPolicy(),
                scenario.neighborResponse(), SUPPORT, STONE, COBBLE, STONE,
                scenario.placementSlot(), scenario.breakSlot(), 0, 20, 40));
        System.out.println("BlockStabilityFixtureTest passed");
    }

    private static BlockLifecycleSlot slot(int hotbar, int id, int beforeDamage,
            int afterDamage) {
        return new BlockLifecycleSlot(hotbar, hotbar + 36,
                new RemoteItemStack(id, 1, beforeDamage), afterDamage < 0 ? null
                        : new RemoteItemStack(id, 1, afterDamage));
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid stability row was accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockLifecycleDriver {
        final List<String> actions = new ArrayList<String>();
        boolean placed, overhead = true, damaged, reloaded;

        @Override public RemoteInventoryView inventory() {
            actions.add("inventory"); List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) {
                RemoteItemStack item = index == 37 && !placed ? new RemoteItemStack(4, 1, 0)
                        : index == 38 ? new RemoteItemStack(278, 1, damaged ? 1 : 0) : null;
                slots.add(new RemoteInventorySlot(index, item));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { actions.add("select:" + slot); }
        @Override public void placeHeldBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "placement drifted");
            placed = true; actions.add("place");
        }
        @Override public void beginBreak(BlockPosition position) {
            require(position.equals(OVERHEAD) && overhead, "break start drifted");
            actions.add("begin:" + token(position));
        }
        @Override public void finishBreak(BlockPosition position) {
            require(position.equals(OVERHEAD) && overhead, "break finish drifted");
            overhead = false; damaged = true; actions.add("finish:" + token(position));
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            BlockState actual = position.equals(SUPPORT) ? STONE : position.equals(TARGET)
                    ? (placed ? COBBLE : new BlockState(0, 0)) : position.equals(OVERHEAD)
                    ? (overhead ? STONE : new BlockState(0, 0)) : new BlockState(0, 0);
            require(actual.equals(expected), "unexpected stability observation " + expected);
            actions.add("await:" + token(position) + ":" + expected.legacyId() + ":"
                    + expected.metadata()); return view(position, actual);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            actions.add("ticks:" + ticks); return view(TARGET, placed ? COBBLE : STONE);
        }
        @Override public List<RemoteDroppedItem> droppedItems() { return Collections.emptyList(); }
        @Override public void saveAndReload() { reloaded = true; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload boundary requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static String token(BlockPosition value) {
        return value.x() + ":" + value.y() + ":" + value.z();
    }

    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(Collections.singletonList(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }
}
