import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Ratchets duplicated SHA-256, lift-loop, Thread.sleep, and server-bootstrap copies. */
public final class DuplicatePatternCheck {
    private static final Pattern SHA = Pattern.compile(
            "MessageDigest\\s*[.]\\s*getInstance\\s*[(]\\s*\"SHA-256\"\\s*[)]");
    private static final Pattern LIFT = Pattern.compile(
            "for\\s*[(]\\s*int\\s+lift\\s*=\\s*0;\\s*lift\\s*<\\s*8;\\s*lift\\+\\+\\s*[)]");
    private static final Pattern SLEEP = Pattern.compile("Thread\\s*[.]\\s*sleep\\s*[(]");
    private static final Pattern SERVER = Pattern.compile(
            "new\\s+B173DedicatedServer\\s*[(]");

    private DuplicatePatternCheck() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length == 2 && "--plant-scan".equals(arguments[1])) {
            Census census = walk(Path.of(arguments[0]));
            if (census.sha256 + census.lift + census.sleep + census.server > 0)
                throw new IllegalStateException("planted duplication survived: " + census);
            System.out.println("duplicate pattern plant-scan: clean");
            return;
        }
        if (arguments.length > 1)
            throw new IllegalArgumentException("usage: DuplicatePatternCheck [root]");
        Path root = arguments.length == 0 ? Path.of("") : Path.of(arguments[0]);
        execute(root.toAbsolutePath().normalize());
    }

    static void execute(Path root) throws Exception {
        Properties policy = StrictProperties.load(
                root.resolve("quality/duplication-ratchet.properties"));
        require("1".equals(policy.getProperty("schema")), "invalid duplication ratchet schema");
        Census census = census(root);
        check(policy, "sha256.direct.sites", census.sha256);
        check(policy, "lift.loop.sites", census.lift);
        check(policy, "thread.sleep.sites", census.sleep);
        check(policy, "dedicated.server.manual.sites", census.server);
        System.out.println("  duplication ratchet: sha256=" + census.sha256
                + " lift=" + census.lift + " sleep=" + census.sleep
                + " server=" + census.server);
    }

    static Census scan(Map<String, String> sources) {
        Census census = new Census();
        for (Map.Entry<String, String> entry : sources.entrySet())
            add(census, entry.getKey(), entry.getValue());
        return census;
    }

    static void selfTest() throws Exception {
        Map<String, String> clean = new LinkedHashMap<>();
        clean.put("tools/harness/Ok.java", "final class Ok {}\n");
        require(scan(clean).total() == 0, "clean fixture reported copies");
        Map<String, String> planted = new LinkedHashMap<>();
        planted.put("smokes/new/Bad.java", "class Bad { void x() throws Exception {\n"
                + "MessageDigest.getInstance(\"SHA-" + "256\");\n"
                + "for (int lift = 0; lift < " + "8; lift++) {}\n"
                + "Thread." + "sleep(1L);\n"
                + "new B173Dedicated" + "Server(jar, dir, 1, 1L, t, 3, true);\n"
                + "}}\n");
        Census bad = scan(planted);
        require(bad.sha256 == 1 && bad.lift == 1 && bad.sleep == 1 && bad.server == 1,
                "planted duplication was not fail-closed: " + bad);
        execute(Path.of("").toAbsolutePath().normalize());
        System.out.println("  duplication ratchet self-test: passed");
    }

    private static Census census(Path root) throws Exception {
        if (Files.isDirectory(root.resolve(".git"))) return gitCensus(root);
        return walk(root);
    }

    private static Census gitCensus(Path root) throws Exception {
        String tracked = ProcessCapture.require(root, List.of("git", "ls-files", "*.java"), 120);
        Census census = new Census();
        for (String relative : tracked.lines().filter(value -> !value.isBlank()).toList()) {
            Path path = root.resolve(relative);
            if (!Files.isRegularFile(path)) continue;
            add(census, relative.replace('\\', '/'),
                    Files.readString(path, StandardCharsets.UTF_8));
        }
        return census;
    }

    private static Census walk(Path root) throws Exception {
        Census census = new Census();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                String relative = root.relativize(path).toString().replace('\\', '/');
                add(census, relative, Files.readString(path, StandardCharsets.UTF_8));
            }
        }
        return census;
    }

    private static void add(Census census, String relative, String source) {
        if (relative.endsWith("HexDigest.java")) {
            census.lift += count(LIFT, source);
            census.sleep += sleepCount(relative, source);
            census.server += count(SERVER, source);
            return;
        }
        if (relative.endsWith("OfficialServerBootstrap.java")) {
            census.sha256 += count(SHA, source);
            census.lift += count(LIFT, source);
            census.sleep += sleepCount(relative, source);
            return;
        }
        census.sha256 += count(SHA, source);
        census.lift += count(LIFT, source);
        census.sleep += sleepCount(relative, source);
        census.server += count(SERVER, source);
    }

    private static int sleepCount(String relative, String source) {
        if (!relative.startsWith("tools/") && !relative.startsWith("smokes/")) return 0;
        return count(SLEEP, source);
    }

    private static int count(Pattern pattern, String source) {
        Matcher matcher = pattern.matcher(source);
        int total = 0;
        while (matcher.find()) total++;
        return total;
    }

    private static void check(Properties policy, String key, int actual) {
        int baseline = Integer.parseInt(required(policy, key));
        require(actual <= baseline, key + " grew: " + actual + " > " + baseline
                + "; use the shared helper and lower the ratchet");
        require(actual == baseline, key + " changed: " + actual + " != " + baseline
                + "; update quality/duplication-ratchet.properties in the same change");
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    static final class Census {
        int sha256, lift, sleep, server;
        int total() { return sha256 + lift + sleep + server; }
        @Override public String toString() {
            return "sha256=" + sha256 + ",lift=" + lift + ",sleep=" + sleep
                    + ",server=" + server;
        }
    }
}
