import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Strict parser for the versioned execution contract's small JSON documents. */
final class ExecutionJsonParser {
    private final String input;
    private int index;

    private ExecutionJsonParser(String input) { this.input = input; }

    static Map<String, Object> object(String input) {
        Object value = new ExecutionJsonParser(input).document();
        check(value instanceof Map<?, ?>, "JSON root is not an object");
        return cast((Map<?, ?>) value);
    }

    private Object document() {
        Object value = value(0); whitespace(); require(index == input.length(), "trailing JSON data"); return value;
    }

    private Object value(int depth) {
        require(depth <= 64, "JSON nesting is too deep"); whitespace(); require(index < input.length(), "unexpected end of JSON");
        return switch (input.charAt(index)) {
            case '{' -> objectValue(depth + 1); case '[' -> arrayValue(depth + 1); case '"' -> stringValue();
            case 't' -> literal("true", Boolean.TRUE); case 'f' -> literal("false", Boolean.FALSE);
            case 'n' -> literal("null", null); default -> numberValue();
        };
    }

    private Map<String, Object> objectValue(int depth) {
        index++; whitespace(); Map<String, Object> result = new LinkedHashMap<>();
        if (take('}')) return result;
        while (true) {
            whitespace(); require(index < input.length() && input.charAt(index) == '"', "JSON key is not a string");
            String key = stringValue(); whitespace(); expect(':');
            require(!result.containsKey(key), "duplicate JSON key " + key); result.put(key, value(depth));
            whitespace(); if (take('}')) return result; expect(',');
        }
    }

    private List<Object> arrayValue(int depth) {
        index++; whitespace(); List<Object> result = new ArrayList<>(); if (take(']')) return result;
        while (true) { result.add(value(depth)); whitespace(); if (take(']')) return result; expect(','); }
    }

    private String stringValue() {
        expect('"'); StringBuilder result = new StringBuilder();
        while (index < input.length()) {
            char value = input.charAt(index++); if (value == '"') return result.toString();
            require(value >= 0x20, "unescaped JSON control character");
            if (value != '\\') { result.append(value); continue; }
            require(index < input.length(), "unterminated JSON escape"); char escape = input.charAt(index++);
            switch (escape) {
                case '"', '\\', '/' -> result.append(escape); case 'b' -> result.append('\b');
                case 'f' -> result.append('\f'); case 'n' -> result.append('\n'); case 'r' -> result.append('\r');
                case 't' -> result.append('\t'); case 'u' -> result.append(unicode());
                default -> throw failure("invalid JSON escape " + escape);
            }
        }
        throw failure("unterminated JSON string");
    }

    private char unicode() {
        require(index + 4 <= input.length(), "short JSON unicode escape");
        try { char value = (char) Integer.parseInt(input.substring(index, index + 4), 16); index += 4; return value; }
        catch (NumberFormatException error) { throw failure("invalid JSON unicode escape"); }
    }

    private Object numberValue() {
        int start = index; if (take('-')) require(index < input.length(), "incomplete JSON number");
        if (!take('0')) { digitRequired(); while (digit()) index++; }
        boolean decimal = false;
        if (take('.')) { decimal = true; digitRequired(); while (digit()) index++; }
        if (index < input.length() && (input.charAt(index) == 'e' || input.charAt(index) == 'E')) {
            decimal = true; index++; if (index < input.length() && "+-".indexOf(input.charAt(index)) >= 0) index++;
            digitRequired(); while (digit()) index++;
        }
        String value = input.substring(start, index);
        try { if (decimal) return Double.valueOf(value); return Long.valueOf(value); }
        catch (NumberFormatException error) { throw failure("invalid JSON number"); }
    }

    private Object literal(String text, Object value) {
        require(input.startsWith(text, index), "invalid JSON literal"); index += text.length(); return value;
    }
    private void digitRequired() { require(digit(), "expected JSON digit"); }
    private boolean digit() { return index < input.length() && Character.isDigit(input.charAt(index)); }
    private boolean take(char value) { if (index < input.length() && input.charAt(index) == value) { index++; return true; } return false; }
    private void expect(char value) { whitespace(); require(take(value), "expected JSON " + value); }
    private void whitespace() { while (index < input.length() && " \t\r\n".indexOf(input.charAt(index)) >= 0) index++; }
    private IllegalArgumentException failure(String message) { return new IllegalArgumentException(message + " at JSON offset " + index); }
    private void require(boolean value, String message) { if (!value) throw failure(message); }
    private static void check(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    @SuppressWarnings("unchecked") private static Map<String, Object> cast(Map<?, ?> value) { return (Map<String, Object>) value; }
}
