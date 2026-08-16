package worldline.trace;

public final class CanonicalStateDocumentTest {
    private CanonicalStateDocumentTest() {}

    public static void main(String[] arguments) {
        String value = "v2|seed=7|schema=x,y|start=1,-2|tick1=3,4";
        CanonicalStateDocument parsed = CanonicalStateDocument.parse(value);
        require(parsed.seed() == 7L && parsed.fields().size() == 2 && parsed.records().size() == 2
                && parsed.records().get(1).label().equals("tick1")
                && parsed.records().get(0).value(1) == -2L && parsed.canonical().equals(value)
                && parsed.signature().length() == 64, "parsed trace fields failed");
        require(CanonicalStateDocument.parse("v2|seed=7|schema=x").records().isEmpty(),
                "empty canonical trace failed");
        failure("v2|seed=07|schema=x|start=1", "canonical");
        failure("v2|seed=7|schema=x,x|start=1,2", "duplicate");
        failure("v2|seed=7|schema=x,y|start=1", "width");
        failure("v1|seed=7|schema=x|start=1", "format");
        System.out.println("CanonicalStateDocumentTest passed");
    }

    private static void failure(String value, String message) {
        try { CanonicalStateDocument.parse(value); throw new AssertionError("expected trace failure"); }
        catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains(message), "unexpected trace failure");
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
