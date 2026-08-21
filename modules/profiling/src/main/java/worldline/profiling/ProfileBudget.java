package worldline.profiling;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Machine-relative wall-clock budgets checked against a {@link TickProfile}.
 * Missing optional keys are ignored; unknown keys fail closed.
 */
public final class ProfileBudget {
    private final Long totalNanos, meanNanos, medianNanos, p95Nanos, maxNanos;
    private final Long modSharePercent;

    private ProfileBudget(Long totalNanos, Long meanNanos, Long medianNanos,
            Long p95Nanos, Long maxNanos, Long modSharePercent) {
        this.totalNanos = totalNanos; this.meanNanos = meanNanos;
        this.medianNanos = medianNanos; this.p95Nanos = p95Nanos;
        this.maxNanos = maxNanos; this.modSharePercent = modSharePercent;
    }

    public static ProfileBudget parse(Path path) throws IOException {
        require(path != null, "budget path");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        Long total = optional(properties, "tick.total.nanos.max");
        Long mean = optional(properties, "tick.mean.nanos.max");
        Long median = optional(properties, "tick.median.nanos.max");
        Long p95 = optional(properties, "tick.p95.nanos.max");
        Long max = optional(properties, "tick.max.nanos.max");
        Long share = optional(properties, "mod.share.percent.max");
        return new ProfileBudget(total, mean, median, p95, max, share);
    }

    /** Human-readable violations; empty means the profile is within budget. */
    public List<String> violations(TickProfile profile) {
        if (profile == null) throw new NullPointerException("profile");
        List<String> violations = new ArrayList<>();
        check(violations, "tick.total.nanos", profile.total(), totalNanos);
        check(violations, "tick.mean.nanos", profile.mean(), meanNanos);
        check(violations, "tick.median.nanos", profile.median(), medianNanos);
        check(violations, "tick.p95.nanos", profile.p95(), p95Nanos);
        check(violations, "tick.max.nanos", profile.max(), maxNanos);
        check(violations, "mod.share.percent", profile.modSharePercent(), modSharePercent);
        return violations;
    }

    private static void check(List<String> violations, String name, long actual, Long limit) {
        if (limit != null && actual > limit) {
            violations.add(name + "=" + actual + ">" + limit);
        }
    }

    private static Long optional(Properties properties, String key) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) return null;
        require(raw.matches("[0-9]{1,18}"), "invalid budget value for " + key + ": " + raw);
        return Long.parseLong(raw);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
