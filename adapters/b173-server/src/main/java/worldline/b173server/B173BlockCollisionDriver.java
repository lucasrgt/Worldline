package worldline.b173server;

import java.util.Objects;
import java.util.function.Supplier;
import worldline.api.BlockCollisionDriver;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;

/** Fresh-login collision driver over a caller-owned official server. */
public final class B173BlockCollisionDriver implements BlockCollisionDriver {
    private final B173DedicatedServer server;
    private final Supplier<B173WireClient> sessions;
    private final PlayerPose origin;
    private B173WireClient session;
    private ReloadBoundary boundary;

    public B173BlockCollisionDriver(B173DedicatedServer server, B173WireClient connectedSession,
            PlayerPose origin, Supplier<B173WireClient> sessions) {
        this.server = Objects.requireNonNull(server, "server");
        this.session = Objects.requireNonNull(connectedSession, "connectedSession");
        this.origin = Objects.requireNonNull(origin, "origin");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    @Override public RemoteInventoryView inventory() { return session.inventory(); }
    @Override public void selectHeldSlot(int slot) { session.selectHeldSlot(slot); }
    @Override public void look(float yaw, float pitch) { session.look(yaw, pitch); }
    @Override public void useHeldItemOnBlock(BlockPosition support, BlockFace face) {
        session.useHeldItemOnBlock(support, face);
    }
    @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
        return session.awaitBlock(position, expected);
    }
    @Override public RemoteWorldView sustainTicks(int ticks) { return session.sustainTicks(ticks); }
    @Override public PlayerPose origin() { return origin; }
    @Override public MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks) {
        return session.moveAndObserve(dx, dy, dz, ticks);
    }

    @Override public void saveAndReload() {
        session.close();
        try {
            B173FixtureSupport.awaitPlayers(server, 0);
            server.save();
            session = Objects.requireNonNull(sessions.get(), "reconnected session");
            session.connect(); session.synchronizePose(); session.awaitInventory();
            boundary = ReloadBoundary.FRESH_LOGIN;
        } catch (Exception error) {
            throw new IllegalStateException("fresh-login collision reload failed", error);
        }
    }

    @Override public ReloadBoundary reloadBoundary() {
        if (boundary == null) throw new IllegalStateException("no reload has completed");
        return boundary;
    }

    @Override public void close() { session.close(); }
}
