package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.BlockStateDomainDriver;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Proves causal ordering, exact domain closure, inventory use, and reload evidence. */
public final class BlockStateDomainFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 64, 4);
    private static final BlockPosition LOWER = new BlockPosition(4, 65, 4);
    private static final BlockPosition UPPER = new BlockPosition(4, 66, 4);
    private BlockStateDomainFixtureTest() { }

    public static void main(String[] arguments) {
        BlockConformancePlan plan = new BlockConformancePlan(List.of(
                new BlockConformanceProfile("b1.7.3:block/064", List.of("directional"),
                        true, Map.of())), List.of(new BlockConformanceTemplate(
                                "state-domain", ConformanceLayer.ARCHETYPE)));
        BlockStateDomainScenario scenario = new BlockStateDomainScenario("door-domain",
                plan.caseFor("b1.7.3:block/064", "state-domain"),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(324, 1, 0), null),
                List.of(new BlockState(64, 0), new BlockState(64, 8), new BlockState(64, 4)),
                List.of(
                        BlockStateDomainStep.place("place", SUPPORT, BlockFace.UP, -90F, 0F,
                                List.of(observation(LOWER, 0), observation(UPPER, 8))),
                        BlockStateDomainStep.activate("open", LOWER, BlockFace.UP,
                                List.of(observation(LOWER, 4), observation(UPPER, 8)))), 5);
        FakeDriver driver = new FakeDriver();
        BlockStateDomainEvidence evidence = BlockStateDomainFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.SINGULAR
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.domain().equals(scenario.domain()), "state-domain evidence drifted");
        require(evidence.canonical().equals(
                "schema=worldline.block-state-domain-evidence.v1\n"
                + "scenario=door-domain\nsubject=b1.7.3:block/064\n"
                + "claim.state-domain=b1.7.3:block/064#state-domain|SINGULAR\n"
                + "domain=64:0,64:8,64:4\n"
                + "step.1=place|PLACE_HELD|4:65:4:64:0,4:66:4:64:8\n"
                + "step.2=open|ACTIVATE|4:65:4:64:4,4:66:4:64:8\n"
                + "reload=FRESH_LOGIN\n"), "canonical state-domain evidence drifted");
        require(driver.actions.equals(List.of("inventory", "select:1", "look:-90.0:0.0",
                "ticks:2", "place", "await:8", "await:0", "await:8", "ticks:2", "inventory",
                "activate", "await:8", "await:4", "await:8", "ticks:2", "inventory", "reload", "await:8",
                "await:4", "await:8")),
                "state-domain action order drifted: " + driver.actions);
        rejects(() -> new BlockStateDomainScenario("bad", scenario.claim(),
                scenario.placementSlot(), List.of(new BlockState(64, 0)), scenario.steps(), 1));
        System.out.println("BlockStateDomainFixtureTest passed");
    }

    private static BlockStateObservation observation(BlockPosition position, int metadata) {
        return new BlockStateObservation(position, new BlockState(64, metadata));
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid state domain was accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeDriver implements BlockStateDomainDriver {
        final List<String> actions = new ArrayList<>();
        boolean consumed, opened, reloaded;

        @Override public RemoteInventoryView inventory() {
            actions.add("inventory");
            List<RemoteInventorySlot> slots = new ArrayList<>();
            for (int index = 0; index < 45; index++) slots.add(new RemoteInventorySlot(index,
                    index == 37 && !consumed ? new RemoteItemStack(324, 1, 0) : null));
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) { actions.add("select:" + slot); }
        @Override public void look(float yaw, float pitch) {
            actions.add("look:" + yaw + ":" + pitch);
        }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "placement input drifted");
            consumed = true; actions.add("place");
        }
        @Override public void activateBlock(BlockPosition position, BlockFace face) {
            require(consumed && position.equals(LOWER), "activation input drifted");
            opened = true; actions.add("activate");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && (!position.equals(LOWER) || expected.metadata() == (opened ? 4 : 0)),
                    "unexpected fake state-domain observation " + expected);
            actions.add("await:" + expected.metadata()); return view(position, expected);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            actions.add("ticks:" + ticks); return view(LOWER, new BlockState(0, 0));
        }
        @Override public void saveAndReload() { reloaded = true; actions.add("reload"); }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload boundary requested early"); return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() { }
    }

    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(List.of(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }
}
