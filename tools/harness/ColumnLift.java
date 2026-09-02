/** Canonical eight-step column lift used by official b1.7.3 fixture construction. */
public final class ColumnLift {
    static final int STEPS = 8;

    private ColumnLift() { }

    @FunctionalInterface
    interface Step {
        void run(int lift) throws Exception;
    }

    static void times(Step step) throws Exception {
        times(STEPS, step);
    }

    static void times(int steps, Step step) throws Exception {
        if (step == null) throw new IllegalArgumentException("null lift step");
        if (steps < 0 || steps > 128) throw new IllegalArgumentException("invalid lift count");
        for (int lift = 0; lift < steps; lift++) step.run(lift);
    }

    static void selfTest() throws Exception {
        int[] seen = {0};
        times(value -> seen[0] += value + 1);
        require(seen[0] == 36, "eight-step lift did not visit 0..7");
        System.out.println("  column lift self-test: passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
