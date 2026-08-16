package worldline.b173;

import worldline.api.RuntimeSnapshot;
import worldline.api.WorldSource;

/** Public construction entrypoint for the reusable b1.7.3 adapter. */
public final class B173Runtimes {
    private B173Runtimes() {}

    public static B173Runtime create(long seed) {
        return create(seed, 1_000_000L);
    }

    public static B173Runtime create(long seed, long initialMillis) {
        return new B173Runtime(seed, initialMillis);
    }

    public static B173Runtime replay(B173Checkpoint checkpoint) {
        if (checkpoint == null) throw new NullPointerException("checkpoint");
        B173Runtime runtime = create(checkpoint.seed(), checkpoint.initialClockMillis());
        try {
            runtime.bootHeadless();
            runtime.loadWorld(WorldSource.at(checkpoint.worldPath()));
            int cursor = apply(checkpoint, runtime, 0, 0);
            for (int tick = 1; tick <= checkpoint.tick(); tick++) {
                cursor = apply(checkpoint, runtime, tick, cursor);
                runtime.tick();
            }
            if (cursor != checkpoint.actions().size()) {
                throw new IllegalStateException("checkpoint contains an event beyond its tick");
            }
            runtime.acceptReplay(checkpoint.actions());
            if (!runtime.observe().fingerprint().equals(checkpoint.stateFingerprint())) {
                throw new IllegalStateException("replay did not restore the checkpoint state");
            }
            return runtime;
        } catch (RuntimeException error) {
            runtime.close();
            throw error;
        }
    }

    public static B173Runtime restore(RuntimeSnapshot snapshot) {
        if (snapshot == null) throw new NullPointerException("snapshot");
        return replay(B173SnapshotCodec.decode(snapshot));
    }

    private static int apply(B173Checkpoint checkpoint, B173Runtime runtime, int tick, int start) {
        int cursor = start;
        while (cursor < checkpoint.actions().size()
                && checkpoint.actions().get(cursor).tick == tick) {
            checkpoint.actions().get(cursor++).apply(runtime.backend());
        }
        return cursor;
    }
}
