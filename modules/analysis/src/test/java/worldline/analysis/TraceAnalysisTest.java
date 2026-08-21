package worldline.analysis;

import java.util.LinkedHashMap;
import java.util.Map;
import worldline.trace.CanonicalStateDocument;

public final class TraceAnalysisTest {
    private TraceAnalysisTest() {}

    public static void main(String[] arguments) {
        CanonicalStateDocument baseline = trace("v2|seed=7|schema=x,y|start=1,2|tick1=3,4");
        TraceDiff equal = TraceDiff.compare(baseline, trace(baseline.canonical()));
        require(!equal.diverged() && equal.kind() == TraceDiff.Kind.NONE, "equal traces diverged");
        TraceDiff value = TraceDiff.compare(baseline,
                trace("v2|seed=7|schema=x,y|start=1,2|tick1=3,9"));
        require(value.kind() == TraceDiff.Kind.VALUE && value.recordIndex() == 1
                && value.recordLabel().equals("tick1") && value.fieldIndex() == 1
                && value.field().equals("y") && value.left().equals("4") && value.right().equals("9"),
                "value divergence was not exact");
        require(TraceDiff.compare(baseline, trace("v2|seed=8|schema=x,y|start=1,2|tick1=3,4"))
                .kind() == TraceDiff.Kind.SEED, "seed divergence failed");
        require(TraceDiff.compare(baseline, trace("v2|seed=7|schema=x,z|start=1,2|tick1=3,4"))
                .kind() == TraceDiff.Kind.SCHEMA, "schema divergence failed");
        require(TraceDiff.compare(baseline, trace("v2|seed=7|schema=x,y|start=1,2"))
                .kind() == TraceDiff.Kind.RECORD_COUNT, "record count divergence failed");
        String view = TraceRenderer.render(baseline);
        require(view.contains("records=2") && view.contains("index\tlabel\tx\ty")
                && view.contains("1\ttick1\t3\t4"), "trace view failed");
        frameAttribution();
        System.out.println("TraceAnalysisTest passed");
    }

    private static void frameAttribution() {
        FrameAttribution.Frame baseline = frame(16000, 0, 12, 3);
        FrameAttribution.Result logical = FrameAttribution.compare(baseline, frame(80000, 0, 720, 3));
        require(logical.cause() == FrameAttribution.Cause.LOGICAL_WORK
                && logical.topCounter().equals("models.transformed") && logical.workDelta() == 708,
                "logical work attribution failed");
        FrameAttribution.Result runtime = FrameAttribution.compare(baseline, frame(80000, 36000, 12, 3));
        require(runtime.cause() == FrameAttribution.Cause.RUNTIME_STALL
                && runtime.hostPauseMicros() == 36000, "runtime attribution failed");
        require(FrameAttribution.compare(baseline, frame(17000, 0, 720, 3)).cause()
                == FrameAttribution.Cause.INCONCLUSIVE, "non-spike attribution failed");
        require(FrameAttribution.compare(baseline, frame(80000, 6000, 720, 3)).cause()
                == FrameAttribution.Cause.MIXED, "mixed attribution failed");
    }

    private static FrameAttribution.Frame frame(long micros, long pause, long models, long draws) {
        Map<String, Long> counters = new LinkedHashMap<>();
        counters.put("models.transformed", models); counters.put("draw.calls", draws);
        return FrameAttribution.Frame.of(micros, pause, counters);
    }

    private static CanonicalStateDocument trace(String value) {
        return CanonicalStateDocument.parse(value);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
