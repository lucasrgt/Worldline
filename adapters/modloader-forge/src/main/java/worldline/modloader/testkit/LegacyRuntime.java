package worldline.modloader.testkit;

import java.nio.file.Path;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.GamePlayer;
import worldline.api.GameWorld;
import worldline.api.RuntimeState;
import worldline.api.WorldSource;

/** Real single-player legacy client controlled one game tick at a time. */
final class LegacyRuntime implements AutomatedMinecraftRuntime {
    private final Path worldPath;
    private final LegacyProtocol protocol;
    private final LegacyClientProcess process;
    private final int timeoutSeconds;
    private final LegacyWorld world;
    private final LegacyPlayer player;
    private volatile LegacySnapshot snapshot;
    private volatile RuntimeState state = RuntimeState.WORLD_LOADED;

    LegacyRuntime(Path worldPath, LegacyProtocol protocol, LegacyClientProcess process,
            int timeoutSeconds, LegacySnapshot snapshot) {
        this.worldPath = worldPath.toAbsolutePath().normalize(); this.protocol = protocol;
        this.process = process; this.timeoutSeconds = timeoutSeconds; this.snapshot = snapshot;
        world = new LegacyWorld(this); player = new LegacyPlayer(this);
    }

    @Override public void bootHeadless() {
        throw new UnsupportedOperationException("M767 qualifies a graphical legacy client");
    }
    @Override public void loadWorld(WorldSource source) {
        requireOpen();
        if (source == null || !source.path().toAbsolutePath().normalize().equals(worldPath))
            throw new IllegalStateException("legacy session is bound to its requested world");
    }
    @Override public synchronized void tick() {
        requireOpen(); long before = snapshot.tick;
        try { snapshot = protocol.tick(); }
        catch (Exception error) { throw new IllegalStateException("legacy tick failed", error); }
        if (snapshot.tick != before + 1L)
            throw new IllegalStateException("legacy tick control drifted: " + before + " -> " + snapshot.tick);
    }
    @Override public RuntimeState state() { return state; }
    @Override public GameWorld world() { requireOpen(); return world; }
    @Override public GamePlayer player() { requireOpen(); return player; }
    LegacySnapshot snapshot() { requireOpen(); return snapshot; }

    @Override public synchronized void close() {
        if (state == RuntimeState.CLOSED) return; Throwable failure = null;
        try { protocol.stop(); } catch (Throwable error) { failure = error; }
        try { protocol.close(); } catch (Throwable error) { failure = add(failure, error); }
        try { process.awaitExit(timeoutSeconds); } catch (Throwable error) {
            failure = add(failure, error); process.close();
        }
        state = RuntimeState.CLOSED;
        if (failure != null) throw new IllegalStateException("legacy session cleanup failed", failure);
    }

    private static Throwable add(Throwable first, Throwable next) {
        if (first == null) return next; first.addSuppressed(next); return first;
    }
    private void requireOpen() {
        if (state == RuntimeState.CLOSED) throw new IllegalStateException("legacy runtime is closed");
    }
}
