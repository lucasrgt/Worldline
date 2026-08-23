import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Rejects silently unregistered remote tests and unowned tracked snapshots. */
final class TestSurfaceCheck {
    static void execute(Path root) throws Exception {
        List<Path> tracked = SmokeTrackedFiles.read(root).stream().sorted().toList();
        remoteTests(root, tracked); snapshots(root, tracked);
    }

    private static void remoteTests(Path root, List<Path> tracked) throws Exception {
        Path suite = root.resolve("modules/api/src/test/java/worldline/api/DomainApiTest.java");
        String registry = Files.readString(suite, StandardCharsets.UTF_8); int count = 0;
        for (Path path : tracked) {
            String name = path.getFileName().toString();
            if (!path.startsWith(suite.getParent()) || !name.matches("Remote[A-Za-z0-9]+Test[.]java"))
                continue;
            String type = name.substring(0, name.length() - 5);
            require(occurrences(registry, type + ".run();") == 1,
                    "Remote test is not registered exactly once: " + type);
            String source = Files.readString(path, StandardCharsets.UTF_8);
            require(source.contains("static void run("), "Remote test lacks run entry point: " + type);
            count++;
        }
        require(count > 0, "Remote test discovery found no tests");
        System.out.println("  remote test registry: " + count + " discovered and registered");
    }

    private static void snapshots(Path root, List<Path> tracked) throws Exception {
        int count = 0;
        for (Path snapshot : tracked) {
            if (!snapshot.getFileName().toString().endsWith(".wlsnap")) continue;
            require(snapshot.toString().replace('\\', '/').contains("/__snapshots__/"),
                    "tracked snapshot is outside __snapshots__: " + root.relativize(snapshot));
            Path owner = Path.of(snapshot + ".owner.properties");
            require(tracked.contains(owner), "snapshot lacks tracked owner: " + root.relativize(snapshot));
            Properties values = new Properties();
            try (var reader = Files.newBufferedReader(owner, StandardCharsets.UTF_8)) {
                values.load(reader);
            }
            String sourceName = required(values, "test.source");
            require(sourceName.matches("modules/[a-z0-9-]+/src/test/java/[A-Za-z0-9_./-]+Test[.]java"),
                    "unsafe snapshot owner source: " + sourceName);
            Path source = root.resolve(sourceName).normalize();
            require(tracked.contains(source), "snapshot owner source is not tracked: " + sourceName);
            String name = required(values, "snapshot.name");
            require(name.matches("[A-Za-z0-9_.-]+"), "invalid snapshot name: " + name);
            require(Files.readString(source, StandardCharsets.UTF_8)
                    .contains("toMatchSnapshot(\"" + name + "\")"),
                    "snapshot owner no longer declares literal: " + name);
            count++;
        }
        System.out.println("  tracked snapshots: " + count + " owned, 0 orphaned");
    }

    private static int occurrences(String text, String value) {
        int count = 0, from = 0;
        while ((from = text.indexOf(value, from)) >= 0) { count++; from += value.length(); }
        return count;
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isEmpty(), "missing snapshot owner field: " + key); return value;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
