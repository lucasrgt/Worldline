import java.util.Locale;

/** Keeps immutable wave report formatting stable without growing the metric controller. */
final class WaveReportFormat {
    private WaveReportFormat() { }
    static String rate(double value) { return String.format(Locale.ROOT, "%.6f", value); }
    static String decimal(double value) {
        return value < 0 ? "-1" : String.format(Locale.ROOT, "%.3f", value);
    }
    static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
