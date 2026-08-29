package worldline.b173server;

import java.util.Objects;
import java.util.function.Supplier;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockRandomTickSpreadDriver;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;

/** Fresh-login random-tick spread driver over an official server session. */
public final class B173BlockRandomTickSpreadDriver implements BlockRandomTickSpreadDriver {
    private final B173DedicatedServer server;
    private final Supplier<B173WireClient> sessions;
    private final PlayerPose origin;
    private B173WireClient session;
    private ReloadBoundary boundary;

    public B173BlockRandomTickSpreadDriver(B173DedicatedServer server,
            B173WireClient connected, PlayerPose origin, Supplier<B173WireClient> sessions) {
        this.server = Objects.requireNonNull(server, "server");
        this.session = Objects.requireNonNull(connected, "connected");
        this.origin = Objects.requireNonNull(origin, "origin");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }
    @Override public RemoteInventoryView inventory() { return session.inventory(); }
    @Override public void selectHeldSlot(int slot) { session.selectHeldSlot(slot); }
    @Override public void placeHeldBlock(BlockPosition support, BlockFace face) {
        session.placeHeldBlock(support, face);
    }
    @Override public void beginBreak(BlockPosition position) { session.beginBreak(position); }
    @Override public void finishBreak(BlockPosition position) { session.finishBreak(position); }
    @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
        return session.awaitBlock(position, expected);
    }
    @Override public RemoteWorldView observe() { return session.sustainTicks(1); }
    @Override public RemoteWorldView sustainTicks(int ticks) { return session.sustainTicks(ticks); }
    @Override public PlayerPose origin() { return origin; }
    @Override public MovementOutcome moveAndObserve(double x, double y, double z, int ticks) {
        return session.moveAndObserve(x, y, z, ticks);
    }
    @Override public void saveAndReload() {
        session.close();
        try {
            B173FixtureSupport.awaitPlayers(server, 0); server.save();
            session = Objects.requireNonNull(sessions.get(), "reconnected spread session");
            session.connect(); session.synchronizePose(); session.awaitInventory();
            session.awaitRemoteChunk(0, 0); boundary = ReloadBoundary.FRESH_LOGIN;
        } catch (Exception error) {
            throw new IllegalStateException("fresh-login random-tick spread reload failed", error);
        }
    }
    @Override public ReloadBoundary reloadBoundary() {
        if (boundary == null) throw new IllegalStateException("no spread reload completed");
        return boundary;
    }
    @Override public void close() { session.close(); }
}
