package worldline.testkit;

/** Escaping and status symbols shared by reporters. */
final class ReportText {
    private ReportText() {}
    static String symbol(TestStatus status, boolean unicode) {
        if (!unicode) {
            if (status == TestStatus.PASSED) return "PASS";
            if (status == TestStatus.FAILED) return "FAIL";
            if (status == TestStatus.SKIPPED) return "SKIP";
            if (status == TestStatus.TODO) return "TODO";
            if (status == TestStatus.FLAKY) return "FLAKY";
            if (status == TestStatus.INTERRUPTED) return "STOP";
            return "RUN";
        }
        if (status == TestStatus.PASSED) return "✓";
        if (status == TestStatus.FAILED) return "×";
        if (status == TestStatus.SKIPPED) return "↓";
        if (status == TestStatus.TODO) return "□";
        if (status == TestStatus.FLAKY) return "!";
        if (status == TestStatus.INTERRUPTED) return "■";
        return "·";
    }
    static String json(String value) {
        if (value == null) return "null";
        StringBuilder result = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char item = value.charAt(index);
            if (item == '"' || item == '\\') result.append('\\').append(item);
            else if (item == '\n') result.append("\\n");
            else if (item == '\r') result.append("\\r");
            else if (item == '\t') result.append("\\t");
            else if (item < 0x20) result.append(String.format("\\u%04x", (int) item));
            else result.append(item);
        }
        return result.append('"').toString();
    }
    static String xml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
