import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Validates boundary, non-claim, and frozen-trace fields on every behavior map. */
final class BehaviorMapSchemaCheck {
    private static final String SCHEMA = "<!-- worldline-map-schema=1 -->";
    private BehaviorMapSchemaCheck() { }
    static void execute(Path root) throws Exception {
        int maps = 0;
        try (var paths = Files.walk(root.resolve("smokes"))) {
            for (Path map : paths.filter(path -> path.getFileName().toString().equals("MAP.md"))
                    .sorted().toList()) {
                maps++; String text = Files.readString(map, StandardCharsets.UTF_8);
                String[] lines = text.split("\\R", 6); require(lines.length >= 5 && lines[0].equals(SCHEMA),
                        "missing behavior map schema: " + root.relativize(map));
                require(field(lines[1], "boundary").matches("[A-Za-z0-9_.#/-]+"),
                        "invalid map boundary: " + root.relativize(map));
                String nonclaims = field(lines[2], "nonclaims");
                require(nonclaims.equals("bounded-to-qualified-evidence")
                                || nonclaims.matches("[a-z0-9]+(?:-[a-z0-9]+)*(?:,[a-z0-9]+(?:-[a-z0-9]+)*)+"),
                        "invalid map nonclaims: " + root.relativize(map));
                String trace = field(lines[3], "frozen-trace");
                Path descriptor = map.getParent().resolve("smoke.properties");
                if (Files.isRegularFile(descriptor)) require(trace.equals(load(descriptor)
                        .getProperty("expected.signature")), "map frozen trace drift: " + root.relativize(map));
                else require(trace.equals("aggregate:redstone-runtime-oracles"),
                        "aggregate map trace drift");
            }
        }
        int expected = SmokeDiscovery.discover(root).size() + 1;
        require(maps == expected, "behavior map schema census drift: " + maps);
        System.out.println("  behavior map schema: " + maps + " boundaries and frozen traces");
    }
    private static String field(String line, String key) {
        String prefix = "<!-- " + key + "=";
        require(line.startsWith(prefix) && line.endsWith(" -->"), "invalid map field " + key);
        return line.substring(prefix.length(), line.length() - 4).trim();
    }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
