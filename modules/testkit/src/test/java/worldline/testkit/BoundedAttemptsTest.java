package worldline.testkit;

import java.util.concurrent.atomic.AtomicInteger;

/** Contract checks for bounded nondeterministic outcome polling. */
public final class BoundedAttemptsTest {
    private BoundedAttemptsTest() {}

    public static void main(String[] arguments) {
        AtomicInteger calls = new AtomicInteger();
        BoundedAttempts.Result<Integer> result = BoundedAttempts.until(
                5, ignored -> Integer.valueOf(calls.incrementAndGet()),
                value -> value.intValue() == 3);
        require(result.attempts() == 3 && result.value().intValue() == 3 && calls.get() == 3,
                "accepted bounded outcome drifted");
        failure(() -> BoundedAttempts.until(2, ignored -> Integer.valueOf(0),
                value -> value.intValue() == 1));
        failure(() -> BoundedAttempts.until(0, ignored -> Integer.valueOf(1),
                value -> true));
        System.out.println("BoundedAttemptsTest passed");
    }

    private static void failure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected bounded-attempt failure");
        } catch (IllegalArgumentException | IllegalStateException expected) {
            // Expected.
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
