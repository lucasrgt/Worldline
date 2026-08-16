package worldline.kernel;

import java.util.Objects;
import worldline.api.GamePlayer;
import worldline.api.GameUi;
import worldline.api.GameWorld;
import worldline.api.RuntimeState;
import worldline.api.UiMinecraftRuntime;
import worldline.api.WorldSource;

/** Lifecycle policy shared by every future game backend. */
public final class ControlledMinecraftRuntime implements UiMinecraftRuntime {
    private final GameBackend backend;
    private RuntimeState state = RuntimeState.NEW;

    public ControlledMinecraftRuntime(GameBackend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
    }

    @Override
    public void bootHeadless() {
        requireState(RuntimeState.NEW, "boot headless");
        backend.bootHeadless();
        state = RuntimeState.HEADLESS_BOOTED;
    }

    @Override
    public void loadWorld(WorldSource source) {
        requireState(RuntimeState.HEADLESS_BOOTED, "load a world");
        backend.loadWorld(Objects.requireNonNull(source, "source"));
        state = RuntimeState.WORLD_LOADED;
    }

    @Override
    public void tick() {
        requireState(RuntimeState.WORLD_LOADED, "tick");
        backend.tick();
    }

    @Override
    public GameWorld world() {
        requireState(RuntimeState.WORLD_LOADED, "access the world");
        return backend.world();
    }

    @Override
    public GamePlayer player() {
        requireState(RuntimeState.WORLD_LOADED, "access the player");
        return backend.player();
    }

    @Override
    public GameUi ui() {
        requireState(RuntimeState.WORLD_LOADED, "access the UI");
        return backend.ui();
    }

    @Override
    public RuntimeState state() {
        return state;
    }

    @Override
    public void close() {
        if (state == RuntimeState.CLOSED) {
            return;
        }
        backend.close();
        state = RuntimeState.CLOSED;
    }

    private void requireState(RuntimeState expected, String action) {
        if (state != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + " while runtime state is " + state + "; expected " + expected);
        }
    }
}
