package worldline.testkit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Deterministic bounded snapshot rendering for stable Java values. */
final class SnapshotValue {
    private SnapshotValue() {}

    static String render(Object value) {
        StringBuilder result = new StringBuilder(); append(result, value, 0);
        if (result.length() > 1_048_576) throw new IllegalArgumentException("snapshot value is too large");
        return result.toString();
    }

    private static void append(StringBuilder target, Object value, int depth) {
        if (depth > 32) throw new IllegalArgumentException("snapshot nesting exceeds 32");
        if (value == null) { target.append("null"); return; }
        if (value instanceof String || value instanceof Character) {
            quote(target, String.valueOf(value)); return;
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            target.append(value); return;
        }
        Class<?> type = value.getClass();
        if (type.isArray()) {
            target.append('['); int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                if (index > 0) target.append(','); append(target, Array.get(value, index), depth + 1);
            }
            target.append(']'); return;
        }
        if (value instanceof Iterable<?>) {
            target.append('['); int index = 0;
            for (Object item : (Iterable<?>) value) {
                if (index++ > 0) target.append(','); append(target, item, depth + 1);
            }
            target.append(']'); return;
        }
        if (value instanceof Map<?, ?>) {
            List<String> entries = new ArrayList<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                StringBuilder item = new StringBuilder(); append(item, entry.getKey(), depth + 1);
                item.append(':'); append(item, entry.getValue(), depth + 1); entries.add(item.toString());
            }
            Collections.sort(entries); target.append('{');
            for (int index = 0; index < entries.size(); index++) {
                if (index > 0) target.append(','); target.append(entries.get(index));
            }
            target.append('}'); return;
        }
        throw new IllegalArgumentException("unsupported snapshot type: " + type.getName());
    }

    private static void quote(StringBuilder target, String value) {
        target.append('"');
        for (int index = 0; index < value.length(); index++) {
            char item = value.charAt(index);
            if (item == '"' || item == '\\') target.append('\\').append(item);
            else if (item == '\n') target.append("\\n");
            else if (item == '\r') target.append("\\r");
            else if (item == '\t') target.append("\\t");
            else if (item < 0x20) throw new IllegalArgumentException("snapshot string has control character");
            else target.append(item);
        }
        target.append('"');
    }
}
