package worldline.analysis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Separates expanded deterministic render work from host/runtime stalls. */
public final class FrameAttribution {
    public enum Cause { LOGICAL_WORK, RUNTIME_STALL, MIXED, INCONCLUSIVE }

    public static final class Frame {
        private final long frameMicros, hostPauseMicros;
        private final Map<String, Long> counters;

        private Frame(long frameMicros, long hostPauseMicros, Map<String, Long> counters) {
            if (frameMicros < 0 || hostPauseMicros < 0) throw new IllegalArgumentException("negative time");
            if (counters == null || counters.isEmpty()) throw new IllegalArgumentException("work counters required");
            TreeMap<String, Long> copy = new TreeMap<>();
            for (Map.Entry<String, Long> item : counters.entrySet()) {
                String name = item.getKey(); Long value = item.getValue();
                if (name == null || !name.matches("[a-z][a-z0-9.]*") || value == null || value < 0)
                    throw new IllegalArgumentException("invalid work counter");
                if (copy.put(name, value) != null) throw new IllegalArgumentException("duplicate work counter");
            }
            this.frameMicros = frameMicros; this.hostPauseMicros = hostPauseMicros;
            this.counters = Collections.unmodifiableMap(copy);
        }

        public static Frame of(long frameMicros, long hostPauseMicros, Map<String, Long> counters) {
            return new Frame(frameMicros, hostPauseMicros, counters);
        }
        public long frameMicros() { return frameMicros; }
        public long hostPauseMicros() { return hostPauseMicros; }
        public Map<String, Long> counters() { return counters; }
    }

    public static final class Result {
        private final Cause cause;
        private final String counter;
        private final long baseline, observed, frameRatioTenths, hostPauseMicros;

        private Result(Cause cause, String counter, long baseline, long observed,
                long frameRatioTenths, long hostPauseMicros) {
            this.cause = cause; this.counter = counter; this.baseline = baseline;
            this.observed = observed; this.frameRatioTenths = frameRatioTenths;
            this.hostPauseMicros = hostPauseMicros;
        }
        public Cause cause() { return cause; }
        public String topCounter() { return counter; }
        public long workDelta() { return observed - baseline; }
        public long frameRatioTenths() { return frameRatioTenths; }
        public long hostPauseMicros() { return hostPauseMicros; }
        public String canonical() {
            return "cause=" + cause + "|frameRatioTenths=" + frameRatioTenths
                    + "|topCounter=" + counter + "|baseline=" + baseline
                    + "|observed=" + observed + "|hostPauseUs=" + hostPauseMicros;
        }
    }

    private FrameAttribution() {}

    public static Result compare(Frame baseline, Frame observed) {
        if (baseline == null || observed == null) throw new NullPointerException("frame");
        Set<String> names = new TreeSet<>(baseline.counters.keySet());
        names.addAll(observed.counters.keySet());
        String top = "none"; long topBase = 0, topValue = 0, topScore = Long.MIN_VALUE;
        for (String name : names) {
            long before = value(baseline, name), after = value(observed, name);
            long delta = after - before;
            long score = delta <= 0 ? delta : scaledRatio(after, Math.max(1L, before));
            if (score > topScore || score == topScore && name.compareTo(top) < 0) {
                top = name; topBase = before; topValue = after; topScore = score;
            }
        }
        boolean slow = observed.frameMicros >= baseline.frameMicros + 5000L
                && observed.frameMicros * 2L >= baseline.frameMicros * 3L;
        boolean expanded = topValue - topBase >= 4L
                && topValue >= Math.max(topBase + 4L, topBase * 2L);
        boolean hostPause = observed.hostPauseMicros >= 5000L;
        Cause cause = !slow ? Cause.INCONCLUSIVE
                : expanded && hostPause ? Cause.MIXED
                : expanded ? Cause.LOGICAL_WORK : Cause.RUNTIME_STALL;
        long frameRatio = scaledRatio(observed.frameMicros, Math.max(1L, baseline.frameMicros));
        return new Result(cause, top, topBase, topValue, frameRatio, observed.hostPauseMicros);
    }

    private static long value(Frame frame, String name) {
        Long value = frame.counters.get(name); return value == null ? 0L : value;
    }

    private static long scaledRatio(long numerator, long denominator) {
        if (numerator > Long.MAX_VALUE / 10L) return Long.MAX_VALUE;
        return numerator * 10L / denominator;
    }
}
