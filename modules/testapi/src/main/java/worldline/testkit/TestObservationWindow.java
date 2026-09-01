package worldline.testkit;

import java.util.Objects;
import java.util.function.Supplier;

/** Telemetered fixed-duration window for native TestKit stability observations. */
public final class TestObservationWindow {
    private long observedTicks;

    public <T> T observe(Supplier<T> probe, int ticks) {
        Objects.requireNonNull(probe, "probe");
        if (ticks < 1)
            throw new IllegalArgumentException("observation ticks must be positive");
        observedTicks += ticks;
        return probe.get();
    }

    public long observedTicks() { return observedTicks; }

    public String evidence() { return "observed-ticks=" + observedTicks; }
}
