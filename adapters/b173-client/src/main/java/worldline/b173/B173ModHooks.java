package worldline.b173;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ordered multi-mod installation, lifecycle callbacks, and the deterministic
 * scheduled-action queue drained at the start of each controlled tick.
 */
final class B173ModHooks {
    private final B173ClientBackend owner;
    private final List<B173Mod> mods = new ArrayList<>();
    private final Map<Integer, List<Runnable>> scheduled = new TreeMap<>();
    private long lastModNanos;

    B173ModHooks(B173ClientBackend owner) { this.owner = owner; }

    void install(B173Mod mod) {
        if (mod == null) throw new NullPointerException("mod");
        mods.add(mod);
        try {
            mod.onLoad(owner);
        } catch (RuntimeException | Error failure) {
            mods.remove(mods.size() - 1);
            throw failure;
        }
    }

    void at(int tick, Runnable action) {
        if (action == null) throw new NullPointerException("action");
        if (tick <= owner.clientTick()) {
            throw new IllegalArgumentException("scheduled tick " + tick + " already ran");
        }
        List<Runnable> actions = scheduled.get(tick);
        if (actions == null) { actions = new ArrayList<>(); scheduled.put(tick, actions); }
        actions.add(action);
    }

    void beforeTick(int tick) {
        List<Runnable> actions = scheduled.remove(tick);
        if (actions == null) return;
        for (Runnable action : actions) action.run();
    }

    void onTick(B173ModContext context) {
        long start = System.nanoTime();
        for (B173Mod mod : new ArrayList<>(mods)) mod.onTick(context);
        lastModNanos = System.nanoTime() - start;
    }

    long lastModNanos() { return lastModNanos; }

    void dispose() {
        try {
            for (int index = mods.size() - 1; index >= 0; index--) mods.get(index).onDispose();
        } finally {
            mods.clear();
            scheduled.clear();
        }
    }
}
