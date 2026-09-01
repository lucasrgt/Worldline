import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Applies only host-matched, lower-only telemetry memory floors. */
final class TelemetryAdmissionFloor {
    private static final List<String> KEYS = List.of("schema", "os", "architecture",
            "runtime.capability", "sample.count", "observed.peak.memory.bytes",
            "minimum.worker.memory.bytes", "direction");
    private TelemetryAdmissionFloor() { }

    static long workerBytes(Path root, long configured) throws Exception {
        Path path = root.resolve(".worldline/executor-telemetry/admission.properties");
        if (!Files.exists(path)) return configured;
        Map<String, String> values = parse(Files.readString(path, StandardCharsets.UTF_8));
        require(values.get("schema").equals("worldline.telemetry-admission.v1"), "admission schema drift");
        require(values.get("runtime.capability").equals("candidate-worker")
                && values.get("direction").equals("lower-only"), "telemetry admission direction drift");
        require(values.get("os").equals(host(System.getProperty("os.name")))
                && values.get("architecture").equals(host(System.getProperty("os.arch"))),
                "telemetry admission host drift");
        long samples = number(values, "sample.count"), peak = number(values, "observed.peak.memory.bytes");
        long minimum = number(values, "minimum.worker.memory.bytes");
        require(samples > 0L && minimum >= peak, "invalid telemetry admission floor");
        return Math.max(configured, minimum);
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-admission-").toAbsolutePath().normalize();
        try {
            Path path = root.resolve(".worldline/executor-telemetry/admission.properties");
            Files.createDirectories(path.getParent());
            String text = "schema=worldline.telemetry-admission.v1\nos=" + host(System.getProperty("os.name"))
                    + "\narchitecture=" + host(System.getProperty("os.arch"))
                    + "\nruntime.capability=candidate-worker\nsample.count=2\n"
                    + "observed.peak.memory.bytes=3221225472\nminimum.worker.memory.bytes=4026531840\n"
                    + "direction=lower-only\n";
            Files.writeString(path, text, StandardCharsets.UTF_8);
            require(workerBytes(root, 2L << 30) == 4026531840L, "telemetry did not lower admission");
            require(workerBytes(root, 5L << 30) == (5L << 30), "telemetry raised admission");
            Files.writeString(path, text.replace("lower-only", "raise"), StandardCharsets.UTF_8);
            expectFailure(() -> workerBytes(root, 2L << 30));
        } finally {
            Files.deleteIfExists(root.resolve(".worldline/executor-telemetry/admission.properties"));
            Files.deleteIfExists(root.resolve(".worldline/executor-telemetry"));
            Files.deleteIfExists(root.resolve(".worldline")); Files.deleteIfExists(root);
        }
    }

    private static Map<String, String> parse(String text) {
        require(text.endsWith("\n") && !text.contains("\r"), "admission encoding drift");
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : text.substring(0, text.length() - 1).split("\n", -1)) {
            int split = line.indexOf('='); require(split > 0, "invalid admission row");
            require(values.put(line.substring(0, split), line.substring(split + 1)) == null,
                    "duplicate admission field");
        }
        require(values.keySet().equals(new java.util.LinkedHashSet<>(KEYS)), "admission fields drifted");
        return values;
    }
    private static long number(Map<String, String> values, String key) { try {
        long value = Long.parseLong(values.get(key)); require(value >= 0L, key + " is negative"); return value;
    } catch (NumberFormatException error) { throw new IllegalArgumentException(key + " is invalid", error); } }
    private static String host(String value) { return value.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", ""); }
    private static void expectFailure(Checked action) throws Exception {
        boolean failed = false; try { action.run(); } catch (IllegalStateException expected) { failed = true; }
        require(failed, "invalid telemetry admission was accepted");
    }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
