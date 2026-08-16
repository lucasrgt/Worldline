package worldline.analysis;

import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateDocument.Record;

/** Stable tabular text view of a canonical state trace. */
public final class TraceRenderer {
    private TraceRenderer() {}

    public static String render(CanonicalStateDocument trace) {
        if (trace == null) throw new NullPointerException("trace");
        StringBuilder result = new StringBuilder("format=v2\nseed=").append(trace.seed())
                .append("\nrecords=").append(trace.records().size()).append("\nsignature=")
                .append(trace.signature()).append("\nindex\tlabel");
        for (String field : trace.fields()) result.append('\t').append(field);
        result.append('\n');
        for (int index = 0; index < trace.records().size(); index++) {
            Record record = trace.records().get(index);
            result.append(index).append('\t').append(record.label());
            for (Long value : record.values()) result.append('\t').append(value);
            result.append('\n');
        }
        return result.toString();
    }
}
