package worldline.testkit;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/** Reusable contract for nondeterministic actions with a deterministic hard bound. */
public final class BoundedAttempts {
    private BoundedAttempts() {}

    public static <T> Result<T> until(
            int maximum, IntFunction<T> attempt, Predicate<T> accepted) {
        if (maximum < 1) throw new IllegalArgumentException("maximum must be positive");
        Objects.requireNonNull(attempt, "attempt");
        Objects.requireNonNull(accepted, "accepted");
        T last = null;
        for (int number = 1; number <= maximum; number++) {
            last = attempt.apply(number);
            if (accepted.test(last)) return new Result<T>(number, last);
        }
        throw new IllegalStateException("outcome absent within " + maximum + " attempts");
    }

    public static final class Result<T> {
        private final int attempts;
        private final T value;

        private Result(int attempts, T value) {
            this.attempts = attempts;
            this.value = value;
        }

        public int attempts() { return attempts; }
        public T value() { return value; }
    }
}
