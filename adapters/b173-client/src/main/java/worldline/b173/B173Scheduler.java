package worldline.b173;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Single-thread deterministic action scheduler keyed by client tick. */
public final class B173Scheduler {
    private final List<Entry> entries = new ArrayList<>();
    private long sequence;
    private int tick;

    public synchronized void afterTicks(int delay, Runnable action) {
        if (delay < 1) throw new IllegalArgumentException("scheduler delay must be positive");
        if (action == null) throw new NullPointerException("action");
        entries.add(new Entry(tick + delay, sequence++, action));
        entries.sort(Comparator.comparingInt((Entry value) -> value.tick)
                .thenComparingLong(value -> value.sequence));
    }

    public synchronized int currentTick() { return tick; }

    public synchronized int pendingActions() { return entries.size(); }

    void advance() {
        List<Runnable> ready = new ArrayList<>();
        synchronized (this) {
            tick++;
            while (!entries.isEmpty() && entries.get(0).tick == tick) {
                ready.add(entries.remove(0).action);
            }
        }
        for (Runnable action : ready) action.run();
    }

    private static final class Entry {
        private final int tick;
        private final long sequence;
        private final Runnable action;
        private Entry(int tick, long sequence, Runnable action) {
            this.tick = tick;
            this.sequence = sequence;
            this.action = action;
        }
    }
}
