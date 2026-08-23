package worldline.profiling;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable complete-frame census with a closed metric schema. Contiguous
 * sequence numbers make omitted frames observable; monotonic timestamps bind
 * every metric row to one ordered timeline.
 */
public final class FrameCensus {
    private final String[] metrics;
    private final Map<String, Integer> indexes;
    private final long[][] rows;

    private FrameCensus(String[] metrics, Map<String, Integer> indexes, long[][] rows) {
        this.metrics = metrics; this.indexes = indexes; this.rows = rows;
    }

    /** Rows contain sequence, monotonic nanoseconds, then one value per metric. */
    public static FrameCensus of(String[] metricNames, long[][] sourceRows) {
        if (metricNames == null || sourceRows == null) throw new NullPointerException("census");
        require(metricNames.length > 0, "frame census requires metrics");
        Map<String, Integer> indexes = new LinkedHashMap<>();
        String[] metrics = metricNames.clone();
        for (int index = 0; index < metrics.length; index++) {
            String name = metrics[index];
            require(name != null && name.matches("[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*"),
                    "invalid frame metric: " + name);
            require(indexes.put(name, index) == null, "duplicate frame metric: " + name);
        }
        require(sourceRows.length > 0, "frame census requires rows");
        long[][] rows = new long[sourceRows.length][];
        long priorSequence = -1L, priorTime = -1L;
        for (int index = 0; index < sourceRows.length; index++) {
            long[] row = sourceRows[index];
            require(row != null && row.length == metrics.length + 2,
                    "frame census row width drift at " + index);
            require(row[0] >= 0L, "negative frame sequence at " + index);
            if (index > 0) {
                require(priorSequence < Long.MAX_VALUE && row[0] == priorSequence + 1L,
                        "noncontiguous frame sequence at " + index);
                require(row[1] > priorTime, "nonmonotonic frame time at " + index);
            } else require(row[1] >= 0L, "negative frame time");
            for (int column = 2; column < row.length; column++)
                require(row[column] >= 0L, "negative frame metric at " + index);
            rows[index] = row.clone(); priorSequence = row[0]; priorTime = row[1];
        }
        return new FrameCensus(metrics, indexes, rows);
    }

    public int frames() { return rows.length; }
    public int metrics() { return metrics.length; }
    public long sequence(int frame) { return rows[frame][0]; }
    public long monotonicNanos(int frame) { return rows[frame][1]; }

    public long value(int frame, String metric) {
        Integer index = indexes.get(metric);
        if (index == null) throw new IllegalArgumentException("unknown frame metric: " + metric);
        return rows[frame][index.intValue() + 2];
    }

    public String[] metricNames() { return metrics.clone(); }
    public long[] row(int frame) { return rows[frame].clone(); }

    @Override public String toString() {
        return "FrameCensus[frames=" + frames() + ",metrics=" + Arrays.toString(metrics) + "]";
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
