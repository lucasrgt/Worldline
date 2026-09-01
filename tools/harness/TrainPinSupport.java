import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Shared deterministic I/O primitives for integration-train migrations. */
abstract class TrainPinSupport {
    protected static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    protected static String capture(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        require(process.waitFor() == 0, "git command failed: " + String.join(" ", command));
        return output.toString(StandardCharsets.UTF_8);
    }

    protected static int status(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return new ProcessBuilder(command).directory(root.toFile()).start().waitFor();
    }

    protected static String digest(String text) throws Exception {
        return digest(text.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8));
    }

    protected static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    protected static void store(Path path, Properties values) throws Exception {
        StringBuilder output = new StringBuilder("# Worldline integration-train proof v1\n");
        for (String key : values.stringPropertyNames().stream()
                .sorted(Comparator.naturalOrder()).toList()) {
            output.append(key).append('=').append(values.getProperty(key)).append('\n');
        }
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    protected static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    protected static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
}
