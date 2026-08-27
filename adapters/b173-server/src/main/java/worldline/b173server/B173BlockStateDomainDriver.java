package worldline.b173server;

import java.util.Objects;
import java.util.function.Supplier;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.BlockStateDomainDriver;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;

/** Fresh-login state-domain driver over a caller-owned official server. */
public final class B173BlockStateDomainDriver implements BlockStateDomainDriver {
    private final B173DedicatedServer server;
    private final Supplier<B173WireClient> sessions;
    private B173WireClient session;
    private ReloadBoundary boundary;

    public B173BlockStateDomainDriver(B173DedicatedServer server,
            B173WireClient connectedSession, Supplier<B173WireClient> sessions) {
        this.server = Objects.requireNonNull(server, "server");
        this.session = Objects.requireNonNull(connectedSession, "connectedSession");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    @Override public RemoteInventoryView inventory() { return session.inventory(); }
    @Override public void selectHeldSlot(int slot) { session.selectHeldSlot(slot); }
    @Override public void look(float yaw, float pitch) { session.look(yaw, pitch); }
    @Override public void placeHeldBlock(BlockPosition support, BlockFace face) {
        session.placeHeldBlock(support, face);
    }
    @Override public void activateBlock(BlockPosition position, BlockFace face) {
        session.activateBlock(position, face);
    }
    @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
        return session.awaitBlock(position, expected);
    }
    @Override public RemoteWorldView sustainTicks(int ticks) { return session.sustainTicks(ticks); }

    @Override public void saveAndReload() {
        session.close();
        try {
            B173FixtureSupport.awaitPlayers(server, 0);
            server.save();
            session = Objects.requireNonNull(sessions.get(), "reconnected session");
            session.connect();
            session.synchronizePose();
            session.awaitInventory();
            boundary = ReloadBoundary.FRESH_LOGIN;
        } catch (Exception error) {
            throw new IllegalStateException("fresh-login state-domain reload failed", error);
        }
    }

    @Override public ReloadBoundary reloadBoundary() {
        if (boundary == null) throw new IllegalStateException("no reload has completed");
        return boundary;
    }

    @Override public void close() { session.close(); }
}
