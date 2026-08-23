package worldline.test;

import java.util.function.Predicate;
import java.util.function.Supplier;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Reusable bounded polling for asynchronous smoke observations. */
public final class WorldlineAwait {
    private final int maximumPolls;
    private long waits;
    private long polls;
    private long failures;
    private long observedTicks;

    public WorldlineAwait(int maximumPolls) {
        if (maximumPolls < 1 || maximumPolls > 100_000)
            throw new IllegalArgumentException("maximum polls must be 1..100000");
        this.maximumPolls = maximumPolls;
    }

    public RemoteInventoryView awaitSlot(Supplier<RemoteInventoryView> probe,
            int slot, RemoteItemStack expected) {
        if (slot < 0 || expected == null) throw new IllegalArgumentException("invalid slot expectation");
        return await("slot " + slot + " to become " + expected, probe,
                value -> value != null && slot < value.size() && !value.slot(slot).empty()
                        && expected.equals(value.slot(slot).item()));
    }

    public RemoteWorldView awaitBlock(Supplier<RemoteWorldView> probe,
            BlockPosition position, BlockState expected) {
        if (position == null || expected == null) throw new IllegalArgumentException("invalid block expectation");
        return await("block " + position + " to become " + expected, probe,
                value -> value != null && loaded(value, position)
                        && expected.equals(value.blockAt(position.x(), position.y(), position.z())));
    }

    public <T> T awaitEntity(Supplier<T> probe, Predicate<T> accepted, String description) {
        if (accepted == null || description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("invalid entity expectation");
        return await(description, probe, accepted);
    }

    public <T> T observeWindow(Supplier<T> probe, int ticks) {
        if (probe == null || ticks < 1) throw new IllegalArgumentException("invalid observation window");
        observedTicks += ticks; return probe.get();
    }

    public AwaitTelemetry telemetry() {
        return new AwaitTelemetry(waits, polls, failures, observedTicks);
    }

    private <T> T await(String description, Supplier<T> probe, Predicate<T> accepted) {
        if (probe == null) throw new IllegalArgumentException("null wait probe");
        waits++;
        for (int attempt = 1; attempt <= maximumPolls; attempt++) {
            polls++; T value = probe.get();
            if (accepted.test(value)) return value;
        }
        failures++;
        throw new IllegalStateException(description + " after " + maximumPolls + " polls");
    }

    private static boolean loaded(RemoteWorldView world, BlockPosition position) {
        return world.containsChunk(Math.floorDiv(position.x(), 16), Math.floorDiv(position.z(), 16));
    }
}
