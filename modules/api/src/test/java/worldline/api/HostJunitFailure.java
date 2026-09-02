package worldline.api;

import org.junit.jupiter.api.Test;

/** Deliberate failing host test used to prove the JUnit engine reports failure. */
public final class HostJunitFailure {
    public HostJunitFailure() { }

    @Test
    public void deliberateFailure() {
        throw new AssertionError("deliberate JUnit engine failure");
    }
}
