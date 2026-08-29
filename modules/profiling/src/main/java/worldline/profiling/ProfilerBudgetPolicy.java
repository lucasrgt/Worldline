package worldline.profiling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Evaluates deterministic metric budgets without hiding the statistic or severity. */
public final class ProfilerBudgetPolicy {
    public enum Statistic { MEAN, P95, P99, MAX }
    public enum Severity { NOTICE, WARNING, CRITICAL }
    private final List<Rule> rules;

    public ProfilerBudgetPolicy(List<Rule> rules) {
        if (rules == null) throw new NullPointerException("profiler budget rules");
        require(!rules.isEmpty() && rules.size() <= 256, "profiler budget rule count");
        for (Rule rule : rules) if (rule == null) throw new NullPointerException("profiler budget rule");
        this.rules = Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }

    public List<Finding> evaluate(ProfilerRun run) {
        if (run == null) throw new NullPointerException("profiler run");
        ProfilerSummary summary = new ProfilerSummary(run);
        List<Finding> findings = new ArrayList<Finding>();
        for (Rule rule : rules) {
            require(run.schema().contains(rule.metric), "budget metric is not captured: " + rule.metric);
            long actual = value(summary, rule);
            if (actual > rule.limit) findings.add(new Finding(rule, actual));
        }
        return Collections.unmodifiableList(findings);
    }

    private static long value(ProfilerSummary summary, Rule rule) {
        if (rule.statistic == Statistic.MEAN) return summary.mean(rule.metric);
        if (rule.statistic == Statistic.P95) return summary.percentile(rule.metric, 95, 100);
        if (rule.statistic == Statistic.P99) return summary.percentile(rule.metric, 99, 100);
        return summary.maximum(rule.metric);
    }

    public static final class Rule {
        private final String metric;
        private final Statistic statistic;
        private final long limit;
        private final Severity severity;
        private Rule(String metric, Statistic statistic, long limit, Severity severity) {
            this.metric = metric; this.statistic = statistic;
            this.limit = limit; this.severity = severity;
        }
        public static Rule of(String metric, Statistic statistic, long limit, Severity severity) {
            require(metric != null && statistic != null && severity != null && limit >= 0L,
                    "invalid profiler budget rule");
            return new Rule(metric, statistic, limit, severity);
        }
        public String metric() { return metric; }
        public Statistic statistic() { return statistic; }
        public long limit() { return limit; }
        public Severity severity() { return severity; }
    }

    public static final class Finding {
        private final Rule rule;
        private final long actual;
        private Finding(Rule rule, long actual) { this.rule = rule; this.actual = actual; }
        public Rule rule() { return rule; }
        public long actual() { return actual; }
        public long excess() { return actual - rule.limit; }
        public String code() {
            return "budget." + rule.severity.name().toLowerCase(java.util.Locale.ROOT);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
