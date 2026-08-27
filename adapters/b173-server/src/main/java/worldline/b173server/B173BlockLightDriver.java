package worldline.b173server;

import java.util.Objects;
import java.util.function.Supplier;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockLightDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;

/** Fresh-login light-plane driver over a caller-owned official server. */
public final class B173BlockLightDriver implements BlockLightDriver {
    private final B173DedicatedServer server;
    private final Supplier<B173WireClient> sessions;
    private B173WireClient session;
    private ReloadBoundary boundary;

    public B173BlockLightDriver(B173DedicatedServer server, B173WireClient connectedSession,
            Supplier<B173WireClient> sessions) {
        this.server = Objects.requireNonNull(server, "server");
        this.session = Objects.requireNonNull(connectedSession, "connectedSession");
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
    @Override public RemoteWorldView observe() { return session.sustainTicks(1); }

    @Override public void saveAndReload() {
        session.close();
        try {
            B173FixtureSupport.awaitPlayers(server, 0); server.save();
            session = Objects.requireNonNull(sessions.get(), "reconnected session");
            session.connect(); session.synchronizePose(); session.awaitInventory();
            session.awaitRemoteChunk(0, 0); boundary = ReloadBoundary.FRESH_LOGIN;
        } catch (Exception error) {
            throw new IllegalStateException("fresh-login light reload failed", error);
        }
    }
    @Override public ReloadBoundary reloadBoundary() {
        if (boundary == null) throw new IllegalStateException("no light reload has completed");
        return boundary;
    }
    @Override public void close() { session.close(); }
}
