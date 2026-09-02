import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** Canonical JSON primitives shared by the Java side of the native executor contract. */
final class ExecutionJson {
    private ExecutionJson() { }

    static String quote(String value) {
        require(value != null, "JSON string is null");
        StringBuilder result = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char item = value.charAt(index);
            if (Character.isHighSurrogate(item)) {
                require(index + 1 < value.length() && Character.isLowSurrogate(value.charAt(index + 1)),
                        "JSON string contains an unpaired surrogate");
                result.append(item).append(value.charAt(++index)); continue;
            }
            require(!Character.isLowSurrogate(item), "JSON string contains an unpaired surrogate");
            switch (item) {
                case '"' -> result.append("\\\""); case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b"); case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n"); case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> { if (item < 0x20) result.append(String.format("\\u%04x", (int) item));
                    else result.append(item); }
            }
        }
        return result.append('"').toString();
    }

    static String strings(List<String> values) {
        StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) result.append(','); result.append(quote(values.get(index)));
        }
        return result.append(']').toString();
    }

    static String stringMap(Map<String, String> values) {
        StringBuilder result = new StringBuilder("{"); int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (index++ > 0) result.append(','); result.append(quote(entry.getKey())).append(':')
                    .append(quote(entry.getValue()));
        }
        return result.append('}').toString();
    }

    static String nullable(Object value) {
        if (value == null) return "null";
        if (value instanceof String text) return quote(text);
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        throw new IllegalArgumentException("unsupported canonical JSON value");
    }

    static byte[] bytes(String json) { return (json + "\n").getBytes(StandardCharsets.UTF_8); }

    static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(ContainerHexDigest.sha256().digest(bytes));
    }

    static String sha256(Path path) throws Exception { return sha256(Files.readAllBytes(path)); }

    static void writeAtomic(Path path, byte[] bytes) throws IOException {
        Path parent = path.toAbsolutePath().normalize().getParent();
        require(parent != null, "output has no parent"); Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, path.getFileName().toString(), ".tmp");
        try {
            Files.write(temporary, bytes);
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, path); }
        } finally { Files.deleteIfExists(temporary); }
    }

    static void exactKeys(Map<String, Object> value, List<String> expected, String label) {
        require(value.size() == expected.size() && new java.util.ArrayList<>(value.keySet()).equals(expected),
                label + " fields or order drifted: " + value.keySet());
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
