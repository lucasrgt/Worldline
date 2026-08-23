package worldline.stationapi;

import java.nio.file.Path;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.GamePlayer;
import worldline.api.GameWorld;
import worldline.api.RuntimeState;
import worldline.api.WorldSource;

/** Real external StationAPI client controlled one game tick at a time over localhost. */
public final class StationApiRuntime implements AutomatedMinecraftRuntime {
    private final String session;
    private final Path worldPath;
    private final StationApiProtocol protocol;
    private final StationApiProcesses processes;
    private final StationApiWorld world;
    private final StationApiPlayer player;
    private volatile StationApiSnapshot snapshot;
    private volatile RuntimeState state = RuntimeState.WORLD_LOADED;

    StationApiRuntime(String session, Path worldPath, StationApiProtocol protocol,
            StationApiProcesses processes) throws Exception {
        this.session = session; this.worldPath = worldPath.toAbsolutePath().normalize();
        this.protocol = protocol; this.processes = processes;
        snapshot = protocol.ready(session); world = new StationApiWorld(this); player = new StationApiPlayer(this);
    }

    @Override public void bootHeadless() {
        throw new UnsupportedOperationException("M620 qualifies a graphical StationAPI client, not headless boot");
    }
    @Override public void loadWorld(WorldSource source) {
        requireOpen();
        if (source == null || !source.path().toAbsolutePath().normalize().equals(worldPath)) {
            throw new IllegalStateException("StationAPI session is already bound to its requested world");
        }
    }
    @Override public synchronized void tick() {
        requireOpen(); long before = snapshot.tick;
        try { snapshot = protocol.tick(session); }
        catch (Exception error) { throw new IllegalStateException("StationAPI tick failed", error); }
        if (snapshot.tick != before + 1L) {
            throw new IllegalStateException("StationAPI tick control drifted: " + before + " -> " + snapshot.tick);
        }
    }
    @Override public RuntimeState state() { return state; }
    @Override public GameWorld world() { requireLoaded(); return world; }
    @Override public GamePlayer player() { requireLoaded(); return player; }
    StationApiSnapshot snapshot() { requireLoaded(); return snapshot; }

    @Override public synchronized void close() {
        if (state == RuntimeState.CLOSED) return; Throwable failure = null;
        try { protocol.stop(session); } catch (Throwable error) { failure = error; }
        try { protocol.close(); } catch (Throwable error) {
            if (failure == null) failure = error; else failure.addSuppressed(error);
        }
        try { processes.close(); } catch (Throwable error) {
            if (failure == null) failure = error; else failure.addSuppressed(error);
        }
        state = RuntimeState.CLOSED;
        if (failure != null) throw new IllegalStateException("StationAPI session cleanup failed", failure);
    }

    private void requireLoaded() {
        requireOpen();
        if (state != RuntimeState.WORLD_LOADED) throw new IllegalStateException("StationAPI world is not loaded");
    }
    private void requireOpen() {
        if (state == RuntimeState.CLOSED) throw new IllegalStateException("StationAPI runtime is closed");
    }
}
