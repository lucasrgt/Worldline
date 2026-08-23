import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates that a pool manifest case is owned by its declared current runner. */
final class SmokeManifestSource {
    private SmokeManifestSource() { }

    static void validate(Path root, String sourceName, String smokeId) throws Exception {
        Path source = root.resolve(sourceName).normalize();
        require(source.startsWith(root.resolve("tools/smoke")) && Files.isRegularFile(source),
                "missing or unsafe smoke source: " + sourceName);
        Path manifest = root.resolve("smokes").resolve(smokeId).resolve("smoke.properties");
        require(Files.isRegularFile(manifest), "missing smoke manifest: " + smokeId);
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(manifest, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require(smokeId.equals(values.getProperty("id")), "smoke manifest identity drift: " + smokeId);
        String declared = values.getProperty("runner.source");
        if (declared == null) require(Files.readString(source).contains("\"" + smokeId + "\""),
                "legacy runner does not declare smoke: " + smokeId);
        else require(sourceName.equals(declared), "pool runner does not match smoke manifest: " + smokeId);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
