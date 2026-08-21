package worldline.analysis;

import worldline.trace.CanonicalStateDocument;

/** Proves deterministic, self-contained HTML evidence rendering. */
public final class TraceHtmlTest {
    private TraceHtmlTest() {}

    public static void main(String[] arguments) {
        singleMode();
        diffMode();
        System.out.println("TraceHtmlTest passed");
    }

    private static void singleMode() {
        CanonicalStateDocument trace = document(4242L, "v2|seed=4242|schema=tick,block65"
                + "|before=0,0|after=1,20");
        String html = TraceHtml.render(trace, null);
        require(html.startsWith("<!DOCTYPE html>") && html.endsWith("</body></html>\n"),
                "html framing drifted");
        require(html.contains("TRACE mode") && html.contains("4242")
                && html.contains(trace.signature()) && html.contains("schema: tick, block65")
                && html.contains(">after<"), "single-mode content drifted");
        require(!html.contains("DIVERGED") && !html.contains("<script"),
                "single mode invented diff content or scripts");
        equal(TraceHtml.render(trace, null), html, "single render determinism");
    }

    private static void diffMode() {
        CanonicalStateDocument left = document(7L, "v2|seed=7|schema=tick,block65"
                + "|before=0,0|after=1,20");
        CanonicalStateDocument right = document(7L, "v2|seed=7|schema=tick,block65"
                + "|before=0,0|after=1,41");
        String html = TraceHtml.render(left, right);
        require(html.contains("DIFF mode") && html.contains("RESULT: DIVERGED at record 1")
                && html.contains(", field block65</p>"), "diff verdict drifted: " + html);
        require(html.contains("class=\"first div\"") && html.contains("class=\"same\""),
                "row highlighting drifted");
        require(html.contains(left.signature()) && html.contains(right.signature()),
                "provenance signatures lost");
        equal(TraceHtml.render(left, right), html, "diff render determinism");
        String equalHtml = TraceHtml.render(left, left);
        require(equalHtml.contains("RESULT: traces are EQUAL")
                && !equalHtml.contains("first div"), "equal mode drifted");
        rejects(() -> TraceHtml.render(null, right));
    }

    private static CanonicalStateDocument document(long seed, String body) {
        return CanonicalStateDocument.parse(body);
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid input was accepted"); }
        catch (Exception expected) { }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) throw new AssertionError(label + " failed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
