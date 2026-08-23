package worldline.smoketest;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.test.AwaitTelemetry;
import worldline.test.WorldlineAwait;
import worldline.test.WorldlineSmokeAwait;

/** Contract tests for deterministic smoke condition polling. */
public final class WorldlineAwaitTest {
    private WorldlineAwaitTest() {}

    public static void main(String[] arguments) {
        WorldlineAwait waits = new WorldlineAwait(4); AtomicInteger slotPolls = new AtomicInteger();
        RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
        RemoteInventoryView inventory = waits.awaitSlot(() -> inventory(
                slotPolls.incrementAndGet() >= 3 ? stone : null), 0, stone);
        require(inventory.slot(0).item().equals(stone), "awaitSlot result drifted");

        AtomicInteger blockPolls = new AtomicInteger(); BlockPosition origin = new BlockPosition(0, 1, 0);
        RemoteWorldView world = waits.awaitBlock(() -> world(blockPolls.incrementAndGet() >= 2 ? 1 : 0),
                origin, new BlockState(1, 0));
        require(world.blockAt(0, 1, 0).legacyId() == 1, "awaitBlock result drifted");

        AtomicInteger entityPolls = new AtomicInteger();
        String entity = waits.awaitEntity(() -> entityPolls.incrementAndGet() >= 2 ? "pig" : null,
                "pig"::equals, "pig entity");
        require(entity.equals("pig"), "awaitEntity result drifted");
        AwaitTelemetry telemetry = waits.telemetry();
        require(telemetry.waits() == 3 && telemetry.polls() == 7 && telemetry.failures() == 0,
                "wait telemetry drifted: " + telemetry.evidence());

        WorldlineAwait failing = new WorldlineAwait(2);
        rejects(() -> failing.awaitEntity(() -> null, value -> false, "missing entity"));
        require(failing.telemetry().failures() == 1 && failing.telemetry().polls() == 2,
                "failed wait telemetry absent");
        require("stable".equals(waits.observeWindow(() -> "stable", 20)),
                "observation window result drifted");
        require(waits.telemetry().observedTicks() == 20
                        && waits.telemetry().evidence().contains("observed-ticks=20"),
                "observation duration telemetry absent");
        String polled = WorldlineSmokeAwait.awaitEntity(session(),
                () -> "ready", "ready"::equals, "ready entity", 3);
        require(polled.equals("ready") && WorldlineSmokeAwait.telemetry().polls() == 1
                        && WorldlineSmokeAwait.telemetry().observedTicks() == 1,
                "process-scoped await telemetry absent");
        System.out.println("WorldlineAwaitTest passed");
    }

    private static RemoteInventoryView inventory(RemoteItemStack item) {
        return new RemoteInventoryView(0, Collections.singletonList(new RemoteInventorySlot(0, item)));
    }

    private static RemoteWorldView world(int id) {
        RemoteChunkObservation area = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 1);
        byte[] ids = new byte[16 * 128 * 16]; ids[1] = (byte) id;
        RemoteChunkSnapshot chunk = new RemoteChunkSnapshot(area, ids,
                new byte[ids.length / 2], new byte[ids.length / 2], new byte[ids.length / 2]);
        return new RemoteWorldView(Arrays.asList(chunk));
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("expected rejection"); }
        catch (IllegalStateException expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static worldline.api.SustainedRemoteWorldMultiplayerSession session() {
        return (worldline.api.SustainedRemoteWorldMultiplayerSession) java.lang.reflect.Proxy.newProxyInstance(
                WorldlineAwaitTest.class.getClassLoader(),
                new Class<?>[]{worldline.api.SustainedRemoteWorldMultiplayerSession.class},
                (proxy, method, arguments) -> method.getName().equals("sustainTicks") ? world(0) : null);
    }
}
