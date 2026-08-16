package worldline.analysis;

import java.util.List;
import worldline.trace.CanonicalStateDocument;
import worldline.trace.CanonicalStateDocument.Record;

/** Exact first structural or field divergence between two canonical traces. */
public final class TraceDiff {
    public enum Kind { NONE, SEED, SCHEMA, RECORD_LABEL, RECORD_COUNT, VALUE }
    private final Kind kind;
    private final int recordIndex, fieldIndex;
    private final String label, field, left, right;

    private TraceDiff(Kind kind, int recordIndex, String label, int fieldIndex,
            String field, String left, String right) {
        this.kind = kind; this.recordIndex = recordIndex; this.label = label;
        this.fieldIndex = fieldIndex; this.field = field; this.left = left; this.right = right;
    }

    public static TraceDiff compare(CanonicalStateDocument left, CanonicalStateDocument right) {
        if (left == null || right == null) throw new NullPointerException("trace");
        if (left.seed() != right.seed()) return at(Kind.SEED, -1, "", -1, "seed",
                Long.toString(left.seed()), Long.toString(right.seed()));
        List<String> leftFields = left.fields(), rightFields = right.fields();
        int commonFields = Math.min(leftFields.size(), rightFields.size());
        for (int field = 0; field < commonFields; field++) if (!leftFields.get(field).equals(rightFields.get(field)))
            return at(Kind.SCHEMA, -1, "", field, "schema", leftFields.get(field), rightFields.get(field));
        if (leftFields.size() != rightFields.size()) return at(Kind.SCHEMA, -1, "", commonFields,
                "schema", item(leftFields, commonFields), item(rightFields, commonFields));
        List<Record> leftRecords = left.records(), rightRecords = right.records();
        int commonRecords = Math.min(leftRecords.size(), rightRecords.size());
        for (int record = 0; record < commonRecords; record++) {
            Record a = leftRecords.get(record), b = rightRecords.get(record);
            if (!a.label().equals(b.label())) return at(Kind.RECORD_LABEL, record, a.label(), -1,
                    "label", a.label(), b.label());
            for (int field = 0; field < leftFields.size(); field++) if (a.value(field) != b.value(field))
                return at(Kind.VALUE, record, a.label(), field, leftFields.get(field),
                        Long.toString(a.value(field)), Long.toString(b.value(field)));
        }
        if (leftRecords.size() != rightRecords.size()) return at(Kind.RECORD_COUNT, commonRecords,
                commonRecords < leftRecords.size() ? leftRecords.get(commonRecords).label()
                        : rightRecords.get(commonRecords).label(), -1, "record",
                record(leftRecords, commonRecords), record(rightRecords, commonRecords));
        return at(Kind.NONE, -1, "", -1, "", "", "");
    }

    public boolean diverged() { return kind != Kind.NONE; }
    public Kind kind() { return kind; }
    public int recordIndex() { return recordIndex; }
    public String recordLabel() { return label; }
    public int fieldIndex() { return fieldIndex; }
    public String field() { return field; }
    public String left() { return left; }
    public String right() { return right; }

    public String render() {
        StringBuilder result = new StringBuilder("kind=").append(kind).append('\n');
        if (diverged()) result.append("record.index=").append(recordIndex).append('\n')
                .append("record.label=").append(label).append('\n')
                .append("field.index=").append(fieldIndex).append('\n')
                .append("field=").append(field).append('\n')
                .append("left=").append(left).append('\n').append("right=").append(right).append('\n');
        return result.toString();
    }

    private static TraceDiff at(Kind kind, int record, String label, int field, String name,
            String left, String right) { return new TraceDiff(kind, record, label, field, name, left, right); }
    private static String item(List<String> values, int index) {
        return index < values.size() ? values.get(index) : "<missing>";
    }
    private static String record(List<Record> values, int index) {
        return index < values.size() ? values.get(index).label() : "<missing>";
    }
}
