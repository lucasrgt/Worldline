import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Loads Java properties while rejecting duplicate logical keys. */
final class StrictProperties {
    private StrictProperties() { }

    static Properties load(Path path) throws IOException {
        rejectDuplicates(path);
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    static void load(Path path, Properties target) throws IOException {
        Properties values = load(path); target.putAll(values);
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-strict-properties-");
        try {
            Path valid = root.resolve("valid.properties");
            Files.writeString(valid, "plain=one\nescaped\\ key=two\n", StandardCharsets.UTF_8);
            require(load(valid).size() == 2, "valid properties were rejected");
            Path duplicate = root.resolve("duplicate.properties");
            Files.writeString(duplicate, "same=one\nsame:two\n", StandardCharsets.UTF_8);
            boolean rejected = false;
            try { load(duplicate); } catch (IOException expected) { rejected = true; }
            require(rejected, "duplicate properties key was accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  strict properties self-test: passed");
    }

    private static void rejectDuplicates(Path path) throws IOException {
        Set<String> keys = new HashSet<>();
        for (String logical : logicalLines(Files.readAllLines(path, StandardCharsets.UTF_8))) {
            String trimmed = logical.stripLeading();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) continue;
            String key = decodeKey(rawKey(trimmed));
            if (!keys.add(key)) throw new IOException("duplicate properties key " + key + " in " + path);
        }
    }

    private static List<String> logicalLines(List<String> physical) {
        List<String> result = new ArrayList<>(); StringBuilder current = new StringBuilder();
        for (String line : physical) {
            if (current.length() == 0) current.append(line); else current.append(line.stripLeading());
            if (continued(current)) current.setLength(current.length() - 1);
            else { result.add(current.toString()); current.setLength(0); }
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private static boolean continued(CharSequence value) {
        int slashes = 0;
        for (int index = value.length() - 1; index >= 0 && value.charAt(index) == '\\'; index--) slashes++;
        return slashes % 2 == 1;
    }

    private static String rawKey(String line) {
        boolean escaped = false;
        for (int index = 0; index < line.length(); index++) {
            char value = line.charAt(index);
            if (!escaped && (value == '=' || value == ':' || Character.isWhitespace(value)))
                return line.substring(0, index);
            if (value == '\\' && !escaped) escaped = true; else escaped = false;
        }
        return line;
    }

    private static String decodeKey(String raw) throws IOException {
        Properties value = new Properties(); value.load(new StringReader(raw + "=present"));
        if (value.size() != 1) throw new IOException("invalid properties key " + raw);
        return value.stringPropertyNames().iterator().next();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
