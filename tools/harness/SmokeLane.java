import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/** One fail-closed lane classifier shared by portability and runtime planning. */
final class SmokeLane {
    static final String SERVER = "server-headless";
    static final String GUI = "windows-client-gui";
    static final String TOOLING = "tooling";
    private SmokeLane() { }

    static String classify(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        Properties descriptor = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve("smokes").resolve(smoke.id)
                .resolve("smoke.properties"), StandardCharsets.UTF_8)) {
            descriptor.load(reader);
        }
        if ("tooling".equals(descriptor.getProperty("candidate.kind"))
                || "tooling-cycle".equals(descriptor.getProperty("qualification.proof")))
            return TOOLING;
        String source = Files.readString(root.resolve(smoke.runner), StandardCharsets.UTF_8);
        boolean explicitServer = "server".equals(descriptor.getProperty("side"))
                || descriptor.containsKey("server.jar.sha256")
                && !descriptor.containsKey("client.jar.sha256");
        boolean gui = source.contains("minecraft-b1.7.3-client.properties")
                || source.contains("aero-model-lib") || source.contains("runClient")
                || source.contains("WORLDLINE_AERO");
        if (smoke.runner.equals("tools/smoke/Run.java") && explicitServer) return SERVER;
        if (gui) return GUI;
        return SERVER;
    }

    static void validate(Path root) throws Exception {
        Properties expected = new Properties();
        try (Reader reader = Files.newBufferedReader(root.resolve("quality/smoke-lanes.properties"),
                StandardCharsets.UTF_8)) { expected.load(reader); }
        Map<String, Integer> counts = new HashMap<>();
        for (String lane : new String[] {SERVER, GUI, TOOLING}) counts.put(lane, 0);
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root))
            counts.compute(classify(root, smoke), (key, value) -> value == null ? 1 : value + 1);
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        require("1".equals(expected.getProperty("schema"))
                        && total == integer(expected, "total")
                        && counts.get(SERVER) == integer(expected, SERVER)
                        && counts.get(GUI) == integer(expected, GUI)
                        && counts.get(TOOLING) == integer(expected, TOOLING),
                "smoke lane census drift: " + counts);
        System.out.println("  smoke lanes: " + counts.get(SERVER) + " portable headless, "
                + counts.get(GUI) + " GUI-bound, " + counts.get(TOOLING) + " tooling-bound");
    }

    private static int integer(Properties values, String key) {
        try { return Integer.parseInt(values.getProperty(key, "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
