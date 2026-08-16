package worldline.trace;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class CanonicalTraceTest {
    private static final String VALUE = "v1|seed=7|initial:time=2,entities=1,column=1.0.12";
    private static final String SIGNATURE =
            "e54b8c0362a67717cf19406d39c1ffd12459bcdc1181afab8d965632b43b8b7b";

    private CanonicalTraceTest() {}

    public static void main(String[] arguments) throws Exception {
        CanonicalTrace trace = new CanonicalTrace(7);
        trace.record("initial", 2, 1, 1, 0, 12);
        equal(VALUE, trace.value(), "canonical value");
        equal(SIGNATURE, trace.signature(), "canonical signature");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        trace.emitTo(new PrintStream(bytes, true, "UTF-8"));
        String expected = CanonicalTrace.TRACE_PREFIX + VALUE + System.lineSeparator()
                + CanonicalTrace.SIGNATURE_PREFIX + SIGNATURE + System.lineSeparator();
        equal(expected, new String(bytes.toByteArray(), StandardCharsets.UTF_8), "protocol output");

        expectFailure(() -> trace.record("bad|label", 0, 0, 1), "delimiter");
        expectFailure(() -> trace.record("empty", 0, 0), "at least one");
        expandedStateTraceIsCanonical();
        System.out.println("CanonicalTraceTest passed");
    }

    private static void expandedStateTraceIsCanonical() {
        CanonicalStateTrace trace = new CanonicalStateTrace(7L, "tick", "x", "health");
        trace.record("loaded", 0L, 12L, 20L);
        trace.record("tick1", 1L, 13L, 19L);
        equal("v2|seed=7|schema=tick,x,health|loaded=0,12,20|tick1=1,13,19",
                trace.value(), "expanded trace");
        equal(64, trace.signature().length(), "expanded signature length");
    }

    private static void expectFailure(Runnable action, String messagePart) {
        try {
            action.run();
            throw new AssertionError("Expected failure containing: " + messagePart);
        } catch (RuntimeException error) {
            if (!error.getMessage().contains(messagePart)) {
                throw new AssertionError("Unexpected failure: " + error.getMessage(), error);
            }
        }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
