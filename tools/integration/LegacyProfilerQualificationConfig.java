import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Immutable pins for the controlled ModLoader and Forge runtime qualification. */
record LegacyProfilerQualificationConfig(String runtimeVersion, String clientHash,
        String modLoaderFile, String modLoaderHash, String modLoaderVersion,
        String forgeFile, String forgeHash, String forgeVersion, int frames,
        List<String> requiredMetrics, int timeoutSeconds) {
    static final String FILE = "adapters/modloader-forge/qualification.properties";

    static LegacyProfilerQualificationConfig load(Path repository) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(repository.resolve(FILE),
                StandardCharsets.UTF_8)) { values.load(reader); }
        return from(values);
    }

    static LegacyProfilerQualificationConfig from(Properties values) {
        require("worldline.legacy-profiler-qualification.v1".equals(required(values, "schema")),
                "legacy profiler qualification schema drifted");
        int frames = number(values, "capture.frames", 1, 10_000);
        int timeout = number(values, "timeout.seconds", 10, 600);
        List<String> metrics = Arrays.stream(required(values, "required.metrics").split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
        require(metrics.size() >= 6, "legacy profiler required metric census drifted");
        return new LegacyProfilerQualificationConfig(required(values, "runtime.version"),
                hash(values, "client.sha256"), required(values, "modloader.file"),
                hash(values, "modloader.sha256"), required(values, "modloader.version"),
                required(values, "forge.file"), hash(values, "forge.sha256"),
                required(values, "forge.version"), frames, metrics, timeout);
    }

    Path artifact(Path directory, String loader) {
        require("modloader".equals(loader) || "forge".equals(loader), "unknown legacy loader");
        return directory.resolve("modloader".equals(loader) ? modLoaderFile : forgeFile);
    }

    String artifactHash(String loader) {
        return "modloader".equals(loader) ? modLoaderHash : forgeHash;
    }

    String loaderVersion(String loader) {
        return "modloader".equals(loader) ? modLoaderVersion : forgeVersion;
    }

    private static int number(Properties values, String key, int minimum, int maximum) {
        int value;
        try { value = Integer.parseInt(required(values, key)); }
        catch (NumberFormatException invalid) { throw new IllegalStateException("invalid " + key); }
        require(value >= minimum && value <= maximum, "out-of-range " + key); return value;
    }

    private static String hash(Properties values, String key) {
        String value = required(values, key).toLowerCase();
        require(value.matches("[0-9a-f]{64}"), "invalid qualification hash " + key); return value;
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing qualification property " + key);
        return value.trim();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
