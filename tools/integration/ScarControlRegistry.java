import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Verifies that every applicable rejection scar routes to a versioned control. */
final class ScarControlRegistry {
    private ScarControlRegistry() { }

    static Map<String, Control> load(Path root, List<RejectionRegistry.Entry> rejections)
            throws Exception {
        Path path = root.resolve("coordination/swarm/scar-checks.properties");
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("1".equals(values.getProperty("schema")), "invalid scar control schema");
        int count = Integer.parseInt(required(values, "check.count"));
        Map<String, Control> result = new HashMap<>();
        for (int index = 0; index < count; index++) {
            String prefix = "check." + index + ".";
            String scar = required(values, prefix + "scar");
            Control control = new Control(required(values, prefix + "id"),
                    Integer.parseInt(required(values, prefix + "version")),
                    required(values, prefix + "type"), required(values, prefix + "handler"),
                    required(values, prefix + "evidence"));
            require(control.version > 0 && List.of("executable", "exception").contains(control.type),
                    "invalid scar control: " + scar);
            require(Files.isRegularFile(root.resolve(control.evidence)),
                    "missing scar control evidence: " + control.evidence);
            result.put(scar, control);
        }
        for (RejectionRegistry.Entry rejection : rejections) {
            require(result.containsKey(rejection.scar()),
                    "rejection scar lacks a control: " + rejection.scar());
        }
        return Map.copyOf(result);
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Control(String id, int version, String type, String handler, String evidence) { }
}
