import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Computes the mandatory post-wave measures from immutable census and resolution reports. */
final class SwarmWaveReport {
    private SwarmWaveReport() { }

    static void write(Path censusValue, Path resolutionValue, Path outputValue) throws Exception {
        Path census = censusValue.toAbsolutePath().normalize();
        Path resolution = resolutionValue.toAbsolutePath().normalize();
        require(Files.isRegularFile(census) && Files.isRegularFile(resolution), "missing wave inputs");
        String source = Files.readString(census, StandardCharsets.UTF_8);
        String resolved = Files.readString(resolution, StandardCharsets.UTF_8);
        int total = count(source, "\\\"id\\\":\\\"m[0-9]+-");
        int qualified = field(source, "QUALIFIED"), dirty = field(source, "DIRTY_SUSPENDED");
        int failed = field(source, "FAILED_GATE"), retries = sum(source, "\"retries\":([0-9]+)");
        int integrated = count(source, "\"state\":\"QUALIFIED\"[^}]*\"integrated\":true");
        int rejected = field(resolved, "rejected"), stranded = field(resolved, "stranded");
        int scarFailures = field(resolved, "retryable") + rejected;
        List<Long> receiptTimes = values(source, "\"time_to_receipt_seconds\":([0-9]+)");
        Collections.sort(receiptTimes);
        long median = receiptTimes.isEmpty() ? -1 : receiptTimes.get(receiptTimes.size() / 2);
        String rate = total == 0 ? "0.000000" : String.format(java.util.Locale.ROOT,
                "%.6f", (double) qualified / total);
        Path output = outputValue.toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Files.writeString(output, "{\n  \"schema\":1,\n  \"created\":\"" + Instant.now()
                + "\",\n  \"census_sha256\":\"" + SwarmEvidenceArchive.sha256(census)
                + "\",\n  \"resolution_sha256\":\"" + SwarmEvidenceArchive.sha256(resolution)
                + "\",\n  \"first_pass_qualification_rate\":" + rate
                + ",\n  \"retries_total\":" + retries + ",\n  \"failures_by_scar\":{\""
                + SwarmPreflight.REQUIRED_SCAR + "\":" + scarFailures
                + "},\n  \"recurrences\":" + scarFailures
                + ",\n  \"median_time_to_receipt_seconds\":" + median
                + ",\n  \"dirty_count\":" + dirty + ",\n  \"failed_gate_count\":" + failed
                + ",\n  \"stranded_count\":" + stranded + ",\n  \"oracle_or_instability_rejections\":"
                + rejected + ",\n  \"qualified_contracts\":" + qualified
                + ",\n  \"integrated_contracts\":" + integrated + "\n}\n", StandardCharsets.UTF_8);
        System.out.println("swarm wave report: first-pass=" + rate + ", retries=" + retries
                + ", scar-failures=" + scarFailures + ", dirty=" + dirty + ", stranded="
                + stranded + ", rejected=" + rejected + ", qualified=" + qualified
                + ", integrated=" + integrated);
        System.out.println("  report: " + output);
    }

    private static int field(String text, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\":([0-9]+)").matcher(text);
        require(matcher.find(), "missing metric: " + name); return Integer.parseInt(matcher.group(1));
    }
    private static int count(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text); int count = 0;
        while (matcher.find()) count++; return count;
    }
    private static int sum(String text, String regex) {
        return values(text, regex).stream().mapToInt(Long::intValue).sum();
    }
    private static List<Long> values(String text, String regex) {
        List<Long> result = new ArrayList<>(); Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) result.add(Long.parseLong(matcher.group(1))); return result;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
