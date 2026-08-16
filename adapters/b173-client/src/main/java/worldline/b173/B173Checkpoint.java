package worldline.b173;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable replay-backed logical snapshot of a realized client state. */
public final class B173Checkpoint {
    private final long seed;
    private final long initialMillis;
    private final Path world;
    private final int tick;
    private final List<B173Action> actions;
    private final String state;

    B173Checkpoint(long seed, long initialMillis, Path world, int tick,
            List<B173Action> actions, String state) {
        this.seed = seed;
        this.initialMillis = initialMillis;
        this.world = world;
        this.tick = tick;
        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
        this.state = state;
    }

    public long seed() { return seed; }

    public long initialClockMillis() { return initialMillis; }

    public Path worldPath() { return world; }

    public int tick() { return tick; }

    public int eventCount() { return actions.size(); }

    public String stateFingerprint() { return state; }

    public B173Runtime restore() { return B173Runtimes.replay(this); }

    public B173Runtime replay() { return B173Runtimes.replay(this); }

    List<B173Action> actions() { return actions; }
}
