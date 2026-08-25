package worldline.testkit;

import java.util.Objects;

/**
 * Reusable multiplayer sleep-quorum contract: while the quorum is open, a solo
 * sleeper must hold through every bounded probe; completing the quorum must
 * wake exactly the declared sleeper count. Adapter-specific clock and packet
 * details stay with the caller.
 */
public final class SleepQuorumFixture {
    private SleepQuorumFixture() { }

    public static Evidence await(int expectedSleepers, int holdTicks, int probeTicks,
            Hold hold, Completion completion) {
        if (expectedSleepers < 2 || holdTicks < 1 || probeTicks < 1 || probeTicks > holdTicks
                || hold == null || completion == null)
            throw new IllegalArgumentException("invalid sleep quorum boundary");
        for (int tick = probeTicks; tick <= holdTicks; tick += probeTicks)
            if (!hold.soloSleeperHolds(tick))
                throw new IllegalStateException(
                        "open quorum did not hold at tick " + tick + " of " + holdTicks);
        int woken = completion.completeQuorum();
        if (woken != expectedSleepers)
            throw new IllegalStateException("completed quorum woke " + woken + " of "
                    + expectedSleepers + " sleepers");
        return new Evidence(expectedSleepers, holdTicks, probeTicks, woken);
    }

    @FunctionalInterface public interface Hold {
        /** True while the solo sleeper is still asleep and no skip occurred. */
        boolean soloSleeperHolds(int tick);
    }

    @FunctionalInterface public interface Completion {
        /** Performs the completing sleep and returns the observed woken-sleeper count. */
        int completeQuorum();
    }

    public static final class Evidence {
        private final int expectedSleepers, holdTicks, probeTicks, wokenSleepers;
        Evidence(int expectedSleepers, int holdTicks, int probeTicks, int wokenSleepers) {
            this.expectedSleepers = expectedSleepers; this.holdTicks = holdTicks;
            this.probeTicks = probeTicks; this.wokenSleepers = wokenSleepers;
        }
        public int expectedSleepers() { return expectedSleepers; }
        public int holdTicks() { return holdTicks; }
        public int probeTicks() { return probeTicks; }
        public int wokenSleepers() { return wokenSleepers; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return expectedSleepers == value.expectedSleepers && holdTicks == value.holdTicks
                    && probeTicks == value.probeTicks && wokenSleepers == value.wokenSleepers;
        }
        @Override public int hashCode() {
            return Objects.hash(expectedSleepers, holdTicks, probeTicks, wokenSleepers);
        }
    }
}
