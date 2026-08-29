package worldline.profiling;

import java.util.Locale;

/** Dependency-free deterministic exports for CI, dashboards, and metrics collectors. */
public final class ProfilerExport {
    private ProfilerExport() {}

    public static String json(ProfilerRun run) {
        if (run == null) throw new NullPointerException("profiler run");
        ProfilerSummary summary = new ProfilerSummary(run);
        StringBuilder out = new StringBuilder(256 + run.schema().size() * 128);
        out.append("{\"schema\":1,\"mode\":\"").append(run.mode().name().toLowerCase(Locale.ROOT))
                .append("\",\"frames\":").append(summary.frames()).append(",\"metrics\":[");
        for (int index = 0; index < run.schema().size(); index++) {
            if (index > 0) out.append(',');
            ProfilerMetric metric = run.schema().metric(index);
            out.append("{\"name\":\"").append(metric.name()).append("\",\"owner\":\"")
                    .append(metric.owner()).append("\",\"category\":\"")
                    .append(metric.category().name().toLowerCase(Locale.ROOT)).append("\",\"unit\":\"")
                    .append(metric.unit().name().toLowerCase(Locale.ROOT)).append("\",\"mean\":")
                    .append(summary.mean(metric.name())).append(",\"p95\":")
                    .append(summary.percentile(metric.name(), 95, 100)).append(",\"max\":")
                    .append(summary.maximum(metric.name())).append('}');
        }
        return out.append("]}").toString();
    }

    public static String openMetrics(ProfilerRun run) {
        if (run == null) throw new NullPointerException("profiler run");
        ProfilerSummary summary = new ProfilerSummary(run);
        StringBuilder out = new StringBuilder(256 + run.schema().size() * 192);
        for (ProfilerMetric metric : run.schema().metrics()) {
            String name = "worldline_profiler_" + metric.name().replace('.', '_');
            out.append("# TYPE ").append(name).append(" gauge\n");
            labels(out, name, metric, "mean", summary.mean(metric.name()));
            labels(out, name, metric, "p95", summary.percentile(metric.name(), 95, 100));
            labels(out, name, metric, "max", summary.maximum(metric.name()));
        }
        return out.append("# EOF\n").toString();
    }

    private static void labels(StringBuilder out, String name, ProfilerMetric metric,
            String statistic, long value) {
        out.append(name).append("{owner=\"").append(metric.owner()).append("\",category=\"")
                .append(metric.category().name().toLowerCase(Locale.ROOT)).append("\",statistic=\"")
                .append(statistic).append("\"} ").append(value).append('\n');
    }
}
