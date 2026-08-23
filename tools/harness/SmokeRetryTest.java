import java.io.EOFException;

/** Proves centralized retry decisions and telemetry without sleeping. */
public final class SmokeRetryTest {
    public static void main(String[] arguments) throws Exception {
        SmokeRetry.afterEofFailure(0, 1, new EOFException("transient"), 0L);
        require(SmokeRetry.telemetry().contains("retries=1"), "retry was not counted");
        EOFException exhausted = new EOFException("exhausted");
        try {
            SmokeRetry.afterEofFailure(1, 1, exhausted, 0L);
            throw new IllegalStateException("exhausted retry was accepted");
        } catch (EOFException expected) {
            require(expected == exhausted, "terminal failure identity changed");
        }
        IllegalStateException ordinary = new IllegalStateException("ordinary");
        try {
            SmokeRetry.afterEofFailure(0, 1, ordinary, 0L);
            throw new IllegalStateException("ordinary failure was accepted");
        } catch (IllegalStateException expected) {
            require(expected == ordinary, "ordinary failure identity changed");
        }
        String telemetry = SmokeRetry.telemetry();
        require(telemetry.contains("failures=2") && telemetry.contains("policy-calls=3"),
                "retry telemetry drifted: " + telemetry);
        System.out.println("  smoke retry self-test: passed; " + telemetry);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
