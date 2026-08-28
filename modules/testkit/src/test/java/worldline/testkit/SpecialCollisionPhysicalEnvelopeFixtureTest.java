package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import worldline.api.BlockCollisionDriver;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockLightDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.BlockStateDomainDriver;
import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Inlines the cobweb physical-envelope row through public fixtures and FakeDriver. */
public final class SpecialCollisionPhysicalEnvelopeFixtureTest {
    private static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    private static final BlockPosition TARGET = new BlockPosition(4, 72, 4);
    private static final PlayerPose ORIGIN = new PlayerPose(4.5D, 72D, 3.5D, 0F, 0F);
    private static final BlockState COBWEB = new BlockState(30, 0);
    private static final BlockState AIR = new BlockState(0, 0);

    private SpecialCollisionPhysicalEnvelopeFixtureTest() {
    }

    public static void main(String[] arguments) {
        collision();
        light();
        stateDomain();
        System.out.println("SpecialCollisionPhysicalEnvelopeFixtureTest passed");
    }

    private static void collision() {
        BlockCollisionScenario scenario = new BlockCollisionScenario(
                "cobweb-static-physical-envelope",
                claim("collision-shape"),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(30, 1, 0), null),
                0F, 0F,
                List.of(new BlockCollisionPlacement(SUPPORT, BlockFace.UP, COBWEB)),
                List.of(new BlockCollisionProbe("level", 0D, 0D, 1D, 10,
                        BlockCollisionExpectation.PASSABLE)));
        CollisionDriver driver = new CollisionDriver();
        BlockCollisionEvidence evidence = BlockCollisionFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.treatments().get(0).contains("PASSABLE"),
                "cobweb collision evidence drifted");
        require(evidence.canonical().contains("placement.1=4:72:4:30:0")
                && evidence.canonical().contains("treatment.1=level|PASSABLE|UNCHALLENGED"),
                "canonical cobweb collision evidence drifted");
    }

    private static void light() {
        BlockLightScenario scenario = new BlockLightScenario(
                "cobweb-static-physical-envelope",
                claim("light-behavior"),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(30, 1, 0), null),
                0F, 0F,
                List.of(new BlockLightPlacement(SUPPORT, BlockFace.UP, COBWEB)),
                List.of(new BlockLightProbe("source", TARGET,
                        new BlockLightExpectation(AIR, 0, 15),
                        new BlockLightExpectation(COBWEB, 0, 14))));
        LightDriver driver = new LightDriver();
        BlockLightEvidence evidence = BlockLightFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.treatments().get(0).contains("30:0")
                && evidence.treatments().get(0).contains("sky=14"),
                "cobweb light evidence drifted");
    }

    private static void stateDomain() {
        List<BlockPosition> pads = List.of(
                new BlockPosition(4, 71, 4), new BlockPosition(6, 71, 4),
                new BlockPosition(4, 71, 6), new BlockPosition(6, 71, 6));
        List<BlockStateDomainStep> steps = new ArrayList<BlockStateDomainStep>();
        float[] yaws = {0F, 90F, 180F, -90F};
        for (int index = 0; index < yaws.length; index++) {
            BlockPosition pad = pads.get(index);
            steps.add(BlockStateDomainStep.place("place-yaw-" + index, pad, BlockFace.UP,
                    yaws[index], 0F, List.of(new BlockStateObservation(
                            BlockFace.UP.adjacent(pad), COBWEB))));
        }
        BlockStateDomainScenario scenario = new BlockStateDomainScenario(
                "cobweb-static-physical-envelope",
                claim("state-domain"),
                new BlockLifecycleSlot(1, 37, new RemoteItemStack(30, 4, 0), null),
                List.of(COBWEB), steps, 40);
        StateDriver driver = new StateDriver(pads);
        BlockStateDomainEvidence evidence = BlockStateDomainFixture.execute(scenario, driver);
        require(evidence.layer() == ConformanceLayer.ARCHETYPE
                && evidence.boundary() == ReloadBoundary.FRESH_LOGIN
                && evidence.domain().equals(List.of(COBWEB))
                && evidence.steps().size() == 4,
                "cobweb state-domain evidence drifted");
        require(evidence.canonical().contains("domain=30:0")
                && evidence.canonical().contains("step.1=place-yaw-0|PLACE_HELD|4:72:4:30:0"),
                "canonical cobweb state-domain evidence drifted");
    }

    private static BlockConformanceCase claim(String template) {
        BlockConformancePlan plan = new BlockConformancePlan(List.of(
                new BlockConformanceProfile("b1.7.3:block/030",
                        List.of("transparent-solid", "special-collision"), false, Map.of())),
                List.of(new BlockConformanceTemplate(template, ConformanceLayer.ARCHETYPE)));
        return plan.caseFor("b1.7.3:block/030", template);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static RemoteWorldView view(BlockPosition position, BlockState state) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot empty = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        return new RemoteWorldView(List.of(empty.withBlock(
                position.x(), position.y(), position.z(), state)));
    }

    private static final class CollisionDriver implements BlockCollisionDriver {
        PlayerPose current = ORIGIN;
        boolean consumed, reloaded;

        @Override public RemoteInventoryView inventory() {
            List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>();
            for (int index = 0; index < 45; index++) {
                slots.add(new RemoteInventorySlot(index,
                        index == 37 && !consumed ? new RemoteItemStack(30, 1, 0) : null));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) {
        }
        @Override public void look(float yaw, float pitch) {
        }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "cobweb placement drifted");
            consumed = true;
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && position.equals(TARGET), "unexpected cobweb observation");
            return view(position, expected);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            return view(TARGET, AIR);
        }
        @Override public PlayerPose origin() {
            return ORIGIN;
        }
        @Override public MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks) {
            PlayerPose attempted = new PlayerPose(current.x() + dx, current.y() + dy,
                    current.z() + dz, 0F, 0F);
            current = attempted;
            return new MovementOutcome(attempted, attempted, MovementDisposition.UNCHALLENGED);
        }
        @Override public void saveAndReload() {
            reloaded = true;
        }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload requested early");
            return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() {
        }
    }

    private static final class LightDriver implements BlockLightDriver {
        boolean consumed, reloaded;
        @Override public RemoteInventoryView inventory() {
            List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>();
            for (int index = 0; index < 45; index++) {
                slots.add(new RemoteInventorySlot(index,
                        index == 37 && !consumed ? new RemoteItemStack(30, 1, 0) : null));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) {
        }
        @Override public void look(float yaw, float pitch) {
        }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(support.equals(SUPPORT) && face == BlockFace.UP, "cobweb light drifted");
            consumed = true;
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && position.equals(TARGET), "unexpected cobweb light wait");
            return lightView(true);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            require(ticks == 2, "light inventory barrier drifted");
            return lightView(true);
        }
        @Override public RemoteWorldView observe() {
            return lightView(reloaded);
        }
        @Override public void saveAndReload() {
            reloaded = true;
        }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload requested early");
            return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() {
        }
    }

    private static RemoteWorldView lightView(boolean treatment) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        byte[] ids = new byte[32768];
        byte[] metadata = new byte[16384];
        byte[] block = new byte[16384];
        byte[] sky = new byte[16384];
        java.util.Arrays.fill(sky, (byte) 255);
        if (treatment) {
            int index = (TARGET.x() * 16 + TARGET.z()) * 128 + TARGET.y();
            ids[index] = 30;
            int pair = sky[index >> 1] & 255;
            sky[index >> 1] = (byte) ((index & 1) == 0
                    ? pair & 240 | 14 : pair & 15 | 14 << 4);
        }
        return new RemoteWorldView(List.of(new RemoteChunkSnapshot(region,
                ids, metadata, block, sky)));
    }

    private static final class StateDriver implements BlockStateDomainDriver {
        final List<BlockPosition> pads;
        boolean consumed, reloaded;
        StateDriver(List<BlockPosition> pads) {
            this.pads = pads;
        }
        @Override public RemoteInventoryView inventory() {
            List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>();
            for (int index = 0; index < 45; index++) {
                slots.add(new RemoteInventorySlot(index,
                        index == 37 && !consumed ? new RemoteItemStack(30, 4, 0) : null));
            }
            return new RemoteInventoryView(0, slots);
        }
        @Override public void selectHeldSlot(int slot) {
        }
        @Override public void look(float yaw, float pitch) {
        }
        @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
            require(pads.contains(support) && face == BlockFace.UP, "cobweb domain placement");
            consumed = true;
        }
        @Override public void activateBlock(BlockPosition position, BlockFace face) {
            throw new AssertionError("cobweb domain does not activate");
        }
        @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
            require(consumed && expected.equals(COBWEB), "unexpected cobweb domain wait");
            return view(position, expected);
        }
        @Override public RemoteWorldView sustainTicks(int ticks) {
            return view(TARGET, AIR);
        }
        @Override public void saveAndReload() {
            reloaded = true;
        }
        @Override public ReloadBoundary reloadBoundary() {
            require(reloaded, "reload boundary requested early");
            return ReloadBoundary.FRESH_LOGIN;
        }
        @Override public void close() {
        }
    }
}
