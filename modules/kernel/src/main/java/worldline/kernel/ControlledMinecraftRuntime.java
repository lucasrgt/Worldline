package worldline.kernel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import worldline.api.EntityCensus;
import worldline.api.GamePlayer;
import worldline.api.GameUi;
import worldline.api.GameWorld;
import worldline.api.InvariantMinecraftRuntime;
import worldline.api.InvariantSample;
import worldline.api.ItemCensus;
import worldline.api.ItemCensusObserver;
import worldline.api.RuntimeState;
import worldline.api.UiMinecraftRuntime;
import worldline.api.WorldSource;

/** Lifecycle policy shared by every future game backend. */
public final class ControlledMinecraftRuntime implements UiMinecraftRuntime, InvariantMinecraftRuntime {
    private final GameBackend backend;
    private RuntimeState state = RuntimeState.NEW;
    private ItemCensusObserver observer;
    private Set<Long> previousChunks;

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
        if (observer != null) {
            observer.observe(sample());
        }
    }

    @Override
    public void watch(ItemCensusObserver observer) {
        if (state == RuntimeState.CLOSED) {
            throw new IllegalStateException("Cannot watch invariants while runtime state is CLOSED");
        }
        this.observer = Objects.requireNonNull(observer, "observer");
        this.previousChunks = null;
    }

    private InvariantSample sample() {
        GameWorld world = world();
        Set<Long> chunks = world.loadedChunks();
        ItemCensus imported = ItemCensus.empty();
        ItemCensus importedBlocks = ItemCensus.empty();
        EntityCensus importedEntities = EntityCensus.empty();
        if (previousChunks != null) {
            Set<Long> added = new HashSet<Long>(chunks);
            added.removeAll(previousChunks);
            if (!added.isEmpty()) {
                imported = world.itemsInChunks(added);
                importedBlocks = world.blocksInChunks(added);
                importedEntities = EntityCensus.inChunks(world.entities(), added);
            }
        }
        previousChunks = new HashSet<Long>(chunks);
        GamePlayer player = player();
        return InvariantSample.of(player.items().plus(world.items()), world.blocks(),
                EntityCensus.from(world.entities()), imported, importedBlocks, importedEntities,
                player.wear().plus(world.wear()), world.time(), player.health(), world.peaceful());
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
