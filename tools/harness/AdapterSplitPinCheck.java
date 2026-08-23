import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Validates adapter decomposition sources and transported runtime observations. */
final class AdapterSplitPinCheck {
    private AdapterSplitPinCheck() { }

    static void execute(Path root) throws Exception {
        Properties lock = manifest(root);
        require("1".equals(lock.getProperty("schema"))
                        && integer(lock, "existing.count") == 3
                        && integer(lock, "added.count") == 4,
                "invalid adapter-split migration");
        sources(root, lock, "existing", 3, true);
        sources(root, lock, "added", 4, false);
        require(source(root, "adapters/b173-server/src/main/java/worldline/b173server/"
                        + "B173DedicatedServer.java").contains("process.boot(properties())"),
                "dedicated-server process delegation drift");
        require(source(root, "adapters/b173-server/src/main/java/worldline/b173server/"
                        + "B173WireClient.java").contains("B173WireLogin.connect("),
                "wire-client login delegation drift");
        require(source(root, "adapters/aero-model-lib/runtime-src/worldline/aero/mixin/"
                        + "WorldlineCaptureMixin.java").contains("WorldlineCaptureScene.placePlayer("),
                "capture-scene delegation drift");
        SmokePins pins = new SmokePins(root); pins.validateEvidence();
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root); int checked = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            checked++; String current = fingerprints.compute(smoke);
            SmokePins.Entry pin = pins.match(smoke.id, current);
            require(pin != null && carries(lock, smoke.id, pin, current),
                    "adapter-split proof drift: " + smoke.id);
        }
        require(checked == 525 && checked == integer(lock, "smoke.count")
                        && integer(lock, "smoke.changed") > 0,
                "adapter-split proof census drift");
        System.out.println("  adapter-split proof transport: 7 sources, 525 smoke inputs");
    }

    static Properties manifest(Path root) throws Exception {
        Path path = root.resolve("smokes/adapter-split.lock");
        if (!Files.isRegularFile(path)) return new Properties();
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    static boolean carries(Properties lock, String id, SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return hash(lock.getProperty(stem + "prior_fingerprint"))
                && current.equals(lock.getProperty(stem + "current_fingerprint"))
                && pin.evidence().equals(lock.getProperty(stem + "evidence_sha256"));
    }

    static boolean follows(Properties lock, String id, String prior, String evidence,
            SmokePins.Entry pin, String current) {
        String stem = "smoke." + id + ".";
        return carries(lock, id, pin, current)
                && prior.equals(lock.getProperty(stem + "prior_fingerprint"))
                && evidence.equals(lock.getProperty(stem + "evidence_sha256"));
    }

    private static void sources(Path root, Properties lock, String group, int count,
            boolean prior) throws Exception {
        for (int index = 0; index < count; index++) {
            String stem = group + "." + index + ".";
            String relative = required(lock, stem + "path");
            require(relative.startsWith("adapters/")
                            && digest(root.resolve(relative)).equals(
                                    required(lock, stem + "current_sha256"))
                            && (!prior || hash(lock.getProperty(stem + "prior_sha256"))),
                    "adapter decomposition source drift: " + relative);
        }
    }

    private static String source(Path root, String relative) throws Exception {
        return Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
    }

    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
                source(path.getParent(), path.getFileName().toString()).replace("\r\n", "\n")
                        .getBytes(StandardCharsets.UTF_8)));
    }

    private static boolean hash(String value) {
        return value != null && value.matches("[0-9a-f]{64}");
    }
    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
