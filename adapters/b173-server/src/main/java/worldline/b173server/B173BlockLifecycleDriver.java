package worldline.b173server;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import worldline.api.BlockFace;
import worldline.api.BlockLifecycleDriver;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;

/** Fresh-login lifecycle driver over a caller-owned official server. */
public final class B173BlockLifecycleDriver implements BlockLifecycleDriver {
    private final B173DedicatedServer server;
    private final Supplier<B173WireClient> sessions;
    private B173WireClient session;
    private ReloadBoundary boundary;

    public B173BlockLifecycleDriver(B173DedicatedServer server, B173WireClient connectedSession,
            Supplier<B173WireClient> sessions) {
        this.server = Objects.requireNonNull(server, "server");
        this.session = Objects.requireNonNull(connectedSession, "connectedSession");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    @Override public RemoteInventoryView inventory() { return session.inventory(); }
    @Override public void selectHeldSlot(int slot) { session.selectHeldSlot(slot); }
    @Override public void placeHeldBlock(BlockPosition support, BlockFace face) {
        session.placeHeldBlock(support, face);
    }
    @Override public void useHeldPlacementItem(BlockPosition support, BlockFace face) {
        session.useHeldItemOnBlock(support, face);
    }
    @Override public void beginBreak(BlockPosition position) { session.beginBreak(position); }
    @Override public void finishBreak(BlockPosition position) { session.finishBreak(position); }
    @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
        return session.awaitBlock(position, expected);
    }
    @Override public RemoteWorldView sustainTicks(int ticks) { return session.sustainTicks(ticks); }
    @Override public List<RemoteDroppedItem> droppedItems() { return session.droppedItems(); }

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
            throw new IllegalStateException("fresh-login block reload failed", error);
        }
    }

    @Override public ReloadBoundary reloadBoundary() {
        if (boundary == null) throw new IllegalStateException("no reload has completed");
        return boundary;
    }

    @Override public void close() { session.close(); }
}
