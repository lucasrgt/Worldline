package worldline.test;

import java.util.function.Predicate;
import java.util.function.Supplier;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.api.SustainedRemoteWorldMultiplayerSession;

/** Process-scoped wait telemetry and explicit fixed-duration observation windows. */
public final class WorldlineSmokeAwait {
    private static long waits, polls, failures, observedTicks;

    private WorldlineSmokeAwait() { }

    public static synchronized RemoteWorldView observe(
            SustainedRemoteWorldMultiplayerSession session, int ticks) {
        if (session == null) throw new IllegalArgumentException("null observation session");
        observedTicks += ticks; return session.sustainTicks(ticks);
    }

    public static RemoteWorldView awaitBlock(SustainedRemoteWorldMultiplayerSession session,
            BlockPosition position, BlockState expected, int maximumPolls) {
        WorldlineAwait local = new WorldlineAwait(maximumPolls);
        try { return local.awaitBlock(() -> local.observeWindow(
                () -> session.sustainTicks(1), 1), position, expected); }
        finally { record(local.telemetry()); }
    }

    public static RemoteWorldView awaitWorld(SustainedRemoteWorldMultiplayerSession session,
            Predicate<RemoteWorldView> accepted, String description, int maximumPolls) {
        WorldlineAwait local = new WorldlineAwait(maximumPolls);
        try { return local.awaitEntity(() -> local.observeWindow(
                () -> session.sustainTicks(1), 1), accepted, description); }
        finally { record(local.telemetry()); }
    }

    public static RemoteWorldView awaitBlockOrNull(SustainedRemoteWorldMultiplayerSession session,
            BlockPosition position, BlockState expected, int maximumPolls) {
        try { return awaitBlock(session, position, expected, maximumPolls); }
        catch (IllegalStateException absent) { return null; }
    }

    public static BlockState awaitBlockMatching(SustainedRemoteWorldMultiplayerSession session,
            BlockPosition position, Predicate<BlockState> accepted, String description,
            int maximumPolls) {
        WorldlineAwait local = new WorldlineAwait(maximumPolls);
        try { return local.awaitEntity(() -> {
            RemoteWorldView world = local.observeWindow(() -> session.sustainTicks(1), 1);
            return world.blockAt(position.x(), position.y(), position.z());
        }, accepted, description); }
        finally { record(local.telemetry()); }
    }

    public static RemoteInventoryView awaitSlot(SustainedRemoteWorldMultiplayerSession session,
            Supplier<RemoteInventoryView> probe, int slot, RemoteItemStack expected, int maximumPolls) {
        return await(session, probe, value -> value != null && slot < value.size()
                && !value.slot(slot).empty() && expected.equals(value.slot(slot).item()),
                "slot " + slot + " to become " + expected, maximumPolls);
    }

    public static <T> T awaitEntity(SustainedRemoteWorldMultiplayerSession session,
            Supplier<T> probe, Predicate<T> accepted, String description, int maximumPolls) {
        return await(session, probe, accepted, description, maximumPolls);
    }

    public static <T> T awaitEntityOrNull(SustainedRemoteWorldMultiplayerSession session,
            Supplier<T> probe, Predicate<T> accepted, String description, int maximumPolls) {
        try { return await(session, probe, accepted, description, maximumPolls); }
        catch (IllegalStateException absent) { return null; }
    }

    public static <T> T awaitCheckedEntity(SustainedRemoteWorldMultiplayerSession session,
            CheckedProbe<T> probe, Predicate<T> accepted, String description, int maximumPolls) {
        return awaitEntity(session, () -> {
            try { return probe.get(); }
            catch (Exception error) { throw new IllegalStateException(description + " probe failed", error); }
        }, accepted, description, maximumPolls);
    }

    public static <T> T awaitCheckedEntityOrNull(SustainedRemoteWorldMultiplayerSession session,
            CheckedProbe<T> probe, Predicate<T> accepted, String description, int maximumPolls) {
        try { return awaitCheckedEntity(session, probe, accepted, description, maximumPolls); }
        catch (IllegalStateException absent) { return null; }
    }

    public static synchronized AwaitTelemetry telemetry() {
        return new AwaitTelemetry(waits, polls, failures, observedTicks);
    }

    private static <T> T await(SustainedRemoteWorldMultiplayerSession session, Supplier<T> probe,
            Predicate<T> accepted, String description, int maximumPolls) {
        WorldlineAwait local = new WorldlineAwait(maximumPolls);
        try { return local.awaitEntity(() -> {
            local.observeWindow(() -> session.sustainTicks(1), 1); return probe.get();
        }, accepted, description); }
        finally { record(local.telemetry()); }
    }

    private static synchronized void record(AwaitTelemetry value) {
        waits += value.waits(); polls += value.polls(); failures += value.failures();
        observedTicks += value.observedTicks();
    }

    @FunctionalInterface public interface CheckedProbe<T> { T get() throws Exception; }
}
