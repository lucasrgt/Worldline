import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Incrementally classifies bytes from the selected OpenCode provider's private log. */
final class OxAlphaProviderLogMonitor {
    private static final long MAX_LOG_BYTES = 8L * 1024 * 1024;
    private final String selectedModel;
    private final long maxLogBytes;
    private long bytes;
    private final StringBuilder partial = new StringBuilder();
    private volatile String classification;

    OxAlphaProviderLogMonitor(String selectedModel) {
        this(selectedModel, MAX_LOG_BYTES);
    }

    OxAlphaProviderLogMonitor(String selectedModel, long maxLogBytes) {
        this.selectedModel = selectedModel;
        this.maxLogBytes = maxLogBytes;
    }

    String accept(byte[] value, int length) {
        bytes += length;
        if (bytes > maxLogBytes) {
            classification = "supervisor-evidence-overflow";
            return classification;
        }
        partial.append(new String(value, 0, length, StandardCharsets.UTF_8));
        int newline;
        while ((newline = partial.indexOf("\n")) >= 0) {
            acceptLine(stripCarriageReturn(partial.substring(0, newline)));
            partial.delete(0, newline + 1);
        }
        return classification;
    }

    String finish() {
        if (!partial.isEmpty() && !"supervisor-evidence-overflow".equals(classification)) {
            acceptLine(stripCarriageReturn(partial.toString()));
            partial.setLength(0);
        }
        return classification;
    }

    String classification() {
        return classification;
    }

    private void acceptLine(String line) {
        if (!providerFailureLine(line, selectedModel)) {
            return;
        }
        classification = line.toLowerCase(Locale.ROOT).contains("usage limit")
                ? "provider-usage-limit" : "provider-stream-error";
    }

    static boolean providerFailureLine(String line, String selectedModel) {
        return "ERROR".equals(field(line, "level"))
                && "stream error".equals(field(line, "message"))
                && selectedModel.equals(field(line, "providerID") + "/" + field(line, "modelID"));
    }

    static String field(String line, String key) {
        int index = 0;
        while (index < line.length()) {
            while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                index++;
            }
            int equals = line.indexOf('=', index);
            if (equals < 0) {
                return null;
            }
            int space = index;
            while (space < equals && !Character.isWhitespace(line.charAt(space))) {
                space++;
            }
            if (space < equals) {
                index = space + 1;
                continue;
            }
            String name = line.substring(index, equals);
            index = equals + 1;
            String value;
            if (index < line.length() && line.charAt(index) == '"') {
                StringBuilder quoted = new StringBuilder();
                boolean escaped = false;
                index++;
                while (index < line.length()) {
                    char character = line.charAt(index++);
                    if (escaped) {
                        quoted.append(character);
                        escaped = false;
                    } else if (character == '\\') {
                        escaped = true;
                    } else if (character == '"') {
                        break;
                    } else {
                        quoted.append(character);
                    }
                }
                value = quoted.toString();
            } else {
                int end = index;
                while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
                    end++;
                }
                value = line.substring(index, end);
                index = end;
            }
            if (name.equals(key)) {
                return value;
            }
        }
        return null;
    }

    private static String stripCarriageReturn(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }
}
