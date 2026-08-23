/** Proves deterministic Markdown rendering from the strict verify receipt. */
public final class VerifySummaryTest {
    public static void main(String[] arguments) {
        String json = "{\"profile\":\"verify\",\"status\":\"passed\",\"elapsed_ms\":12,"
                + "\"stages\":[{\"name\":\"tests\",\"status\":\"passed\",\"elapsed_ms\":7}]}";
        String summary = VerifySummary.render(json);
        require(summary.contains("Profile: `verify`") && summary.contains("| tests | passed | 7 |"),
                "verify summary drifted");
        System.out.println("  verify summary self-test: passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
