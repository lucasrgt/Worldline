package worldline.b173;

import java.util.ArrayList;
import java.util.List;
import worldline.api.GamePlayer;
import worldline.api.GameUi;
import worldline.api.GameWorld;
import worldline.api.InvariantMinecraftRuntime;
import worldline.api.CauseDrop;
import worldline.api.ItemCensusObserver;
import worldline.api.ItemRecipe;
import worldline.api.RuntimeState;
import worldline.api.RuntimeSnapshot;
import worldline.api.SnapshotMinecraftRuntime;
import worldline.api.UiMinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;

/** Reusable controlled runtime backed by the mapped Beta 1.7.3 client. */
public final class B173Runtime implements SnapshotMinecraftRuntime, UiMinecraftRuntime,
        InvariantMinecraftRuntime {
    private final B173ClientBackend backend;
    private final ControlledMinecraftRuntime lifecycle;
    private final B173VirtualClock clock;
    private final B173VirtualFileSystem files;
    private final B173Scheduler scheduler;
    private final long seed;
    private final long initialMillis;
    private final List<B173Action> actions = new ArrayList<>();
    private WorldSource source;

    B173Runtime(long seed, long initialMillis) {
        this.seed = seed;
        this.initialMillis = initialMillis;
        clock = new B173VirtualClock(initialMillis);
        files = new B173VirtualFileSystem();
        scheduler = new B173Scheduler();
        backend = new B173ClientBackend(seed, clock, files, scheduler);
        lifecycle = new ControlledMinecraftRuntime(backend);
    }

    @Override
    public void bootHeadless() { lifecycle.bootHeadless(); }

    @Override
    public void loadWorld(WorldSource source) { lifecycle.loadWorld(source); this.source = source; }

    @Override
    public void tick() { lifecycle.tick(); }

    @Override
    public RuntimeState state() { return lifecycle.state(); }

    @Override
    public GameWorld world() { return lifecycle.world(); }

    @Override
    public GamePlayer player() { return lifecycle.player(); }

    @Override
    public GameUi ui() { return lifecycle.ui(); }

    @Override
    public void watch(ItemCensusObserver observer) { lifecycle.watch(observer); }

    @Override
    public List<ItemRecipe> recipes() { return B173Recipes.snapshot(); }

    @Override
    public List<worldline.api.ItemStackRecipe> stackRecipes() { return B173Recipes.stackSnapshot(); }

    @Override
    public List<CauseDrop> drops() { return B173Causes.withMobs(); }

    @Override
    public List<ItemRecipe> transforms() { return B173Transforms.swaps(); }

    @Override
    public List<CauseDrop> fluids() { return B173Transforms.fluids(); }

    @Override
    public List<worldline.api.FoodHeal> foods() { return B173Foods.snapshot(); }

    @Override
    public List<worldline.api.SpawnRule> spawns() { return B173Spawns.snapshot(); }

    @Override
    public void close() { lifecycle.close(); }

    public B173Observation observe() { return backend.observe(); }

    public void assertHeadless() { backend.assertHeadless(); }

    public String minecraftClassSource() { return backend.minecraftClassSource(); }

    public B173VirtualClock clock() { return clock; }

    public B173VirtualFileSystem fileSystem() { return files; }

    public B173Scheduler scheduler() { return scheduler; }

    public void key(int key, boolean pressed) { key(key, pressed, (char) 0); }

    public void key(int key, boolean pressed, char character) {
        backend.key(key, pressed, character);
        actions.add(B173Action.key(scheduler.currentTick(), key, pressed, character));
    }

    public void tap(int key) { key(key, true); key(key, false); }

    public void mouse(int button, boolean pressed, int wheel, int x, int y) {
        backend.mouse(button, pressed, wheel, x, y);
        actions.add(B173Action.mouse(scheduler.currentTick(), button, pressed, wheel, x, y));
    }

    public void reseed(long seed) {
        backend.reseed(seed);
        actions.add(B173Action.reseed(scheduler.currentTick(), seed));
    }

    public boolean networkConnected() { return false; }

    public boolean timerThreadAlive() { return backend.timerThreadAlive(); }

    public B173Gui gui() { return (B173Gui) ui(); }

    public void installMod(B173Mod mod) {
        if (mod == null) throw new NullPointerException("mod");
        backend.install(mod);
    }

    /** Installs several mods in the given deterministic order. */
    public void installMods(List<B173Mod> mods) {
        if (mods == null) throw new NullPointerException("mods");
        for (B173Mod mod : mods) installMod(mod);
    }

    /** Wall-clock nanoseconds spent in mod callbacks during the last tick. */
    public long lastModNanos() { return backend.lastModNanos(); }

    @Override
    public RuntimeSnapshot snapshot() { return B173SnapshotCodec.encode(checkpoint()); }

    public B173Checkpoint checkpoint() {
        if (state() != RuntimeState.WORLD_LOADED || source == null) {
            throw new IllegalStateException("snapshot requires a loaded world");
        }
        if (scheduler.pendingActions() != 0) {
            throw new IllegalStateException("snapshot requires a drained scheduler");
        }
        B173Observation observation = observe();
        return new B173Checkpoint(seed, initialMillis, source.path(), observation.clientTick(),
                actions, observation.fingerprint());
    }

    B173ClientBackend backend() { return backend; }

    void acceptReplay(List<B173Action> history) { actions.addAll(history); }
}
