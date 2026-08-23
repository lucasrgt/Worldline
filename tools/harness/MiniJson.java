import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Strict parser for the small JSON receipts and plans owned by the harness. */
final class MiniJson {
    private final String input;
    private int index;

    private MiniJson(String input) { this.input = input; }

    static Map<String, Object> object(String input) {
        Object value = new MiniJson(input).document();
        if (!(value instanceof Map<?, ?> raw)) throw new IllegalArgumentException("JSON root is not an object");
        return cast(raw);
    }

    static String string(Map<String, Object> object, String name) {
        Object value = required(object, name);
        if (!(value instanceof String text)) throw new IllegalArgumentException(name + " is not a string");
        return text;
    }

    static boolean bool(Map<String, Object> object, String name) {
        Object value = required(object, name);
        if (!(value instanceof Boolean flag)) throw new IllegalArgumentException(name + " is not a boolean");
        return flag;
    }

    static long integer(Map<String, Object> object, String name) {
        Object value = required(object, name);
        if (!(value instanceof Long number)) throw new IllegalArgumentException(name + " is not an integer");
        return number;
    }

    static List<Object> array(Map<String, Object> object, String name) {
        Object value = required(object, name);
        if (!(value instanceof List<?> raw)) throw new IllegalArgumentException(name + " is not an array");
        return new ArrayList<>(raw);
    }

    static Map<String, Object> asObject(Object value, String name) {
        if (!(value instanceof Map<?, ?> raw)) throw new IllegalArgumentException(name + " is not an object");
        return cast(raw);
    }

    private Object document() {
        Object value = value(0); whitespace();
        if (index != input.length()) fail("trailing data");
        return value;
    }

    private Object value(int depth) {
        if (depth > 128) fail("nesting is too deep");
        whitespace();
        if (index >= input.length()) fail("unexpected end of input");
        return switch (input.charAt(index)) {
            case '{' -> objectValue(depth + 1);
            case '[' -> arrayValue(depth + 1);
            case '"' -> stringValue();
            case 't' -> literal("true", Boolean.TRUE);
            case 'f' -> literal("false", Boolean.FALSE);
            case 'n' -> literal("null", null);
            default -> numberValue();
        };
    }

    private Map<String, Object> objectValue(int depth) {
        index++; whitespace();
        Map<String, Object> result = new LinkedHashMap<>();
        if (take('}')) return result;
        while (true) {
            whitespace();
            if (index >= input.length() || input.charAt(index) != '"') fail("object key must be a string");
            String key = stringValue(); whitespace(); expect(':');
            if (result.containsKey(key)) fail("duplicate object key " + key);
            result.put(key, value(depth));
            whitespace();
            if (take('}')) return result;
            expect(',');
        }
    }

    private List<Object> arrayValue(int depth) {
        index++; whitespace();
        List<Object> result = new ArrayList<>();
        if (take(']')) return result;
        while (true) {
            result.add(value(depth)); whitespace();
            if (take(']')) return result;
            expect(',');
        }
    }

    private String stringValue() {
        expect('"'); StringBuilder result = new StringBuilder();
        while (index < input.length()) {
            char value = input.charAt(index++);
            if (value == '"') return result.toString();
            if (value < 0x20) fail("unescaped control character");
            if (value != '\\') { result.append(value); continue; }
            if (index >= input.length()) fail("unterminated escape");
            char escape = input.charAt(index++);
            switch (escape) {
                case '"', '\\', '/' -> result.append(escape);
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> result.append(unicode());
                default -> fail("invalid escape " + escape);
            }
        }
        fail("unterminated string"); return "";
    }

    private char unicode() {
        if (index + 4 > input.length()) fail("short unicode escape");
        try {
            char value = (char) Integer.parseInt(input.substring(index, index + 4), 16);
            index += 4; return value;
        } catch (NumberFormatException error) {
            fail("invalid unicode escape"); return 0;
        }
    }

    private Object numberValue() {
        int start = index;
        if (take('-') && index >= input.length()) fail("incomplete number");
        if (take('0')) { }
        else { requireDigit(); while (index < input.length() && Character.isDigit(input.charAt(index))) index++; }
        boolean decimal = false;
        if (take('.')) { decimal = true; requireDigit(); while (digit()) index++; }
        if (index < input.length() && (input.charAt(index) == 'e' || input.charAt(index) == 'E')) {
            decimal = true; index++; if (index < input.length()
                    && (input.charAt(index) == '+' || input.charAt(index) == '-')) index++;
            requireDigit(); while (digit()) index++;
        }
        String value = input.substring(start, index);
        try {
            if (decimal) return Double.valueOf(value);
            return Long.valueOf(value);
        }
        catch (NumberFormatException error) { fail("invalid number"); return 0L; }
    }

    private Object literal(String text, Object value) {
        if (!input.startsWith(text, index)) fail("invalid literal");
        index += text.length(); return value;
    }

    private void requireDigit() {
        if (!digit()) fail("expected digit");
    }

    private boolean digit() {
        return index < input.length() && Character.isDigit(input.charAt(index));
    }

    private boolean take(char value) {
        if (index < input.length() && input.charAt(index) == value) { index++; return true; }
        return false;
    }

    private void expect(char value) {
        whitespace(); if (!take(value)) fail("expected " + value);
    }

    private void whitespace() {
        while (index < input.length() && " \t\r\n".indexOf(input.charAt(index)) >= 0) index++;
    }

    private void fail(String message) {
        throw new IllegalArgumentException(message + " at JSON offset " + index);
    }

    private static Object required(Map<String, Object> object, String name) {
        if (!object.containsKey(name)) throw new IllegalArgumentException("missing JSON field " + name);
        return object.get(name);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Map<?, ?> value) {
        return (Map<String, Object>) value;
    }
}
