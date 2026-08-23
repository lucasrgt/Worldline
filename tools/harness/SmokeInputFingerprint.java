import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Computes the behavior-input identity of one smoke without binding it to a Git commit. */
final class SmokeInputFingerprint {
    private static final Pattern PRODUCT = Pattern.compile(
            "(?:product|productClasses)\\(\\\"([a-z0-9-]+)\\\"\\)"
            + "|\\.worldline/build/classes/([a-z0-9-]+)");
    private static final Pattern RESOLVE = Pattern.compile("\\.resolve\\(\\\"([a-z0-9-]+)\\\"\\)");
    private static final Pattern ENVIRONMENT = Pattern.compile("System\\.getenv\\(\\\"([A-Z0-9_]+)\\\"\\)");
    private static final List<String> REPOSITORY_INPUTS = List.of(
            "adapters/b173-client", "adapters/b173-server", "adapters/aero-model-lib",
            "artifacts", "mappings", "patches", "tools/toolchains");

    private final Path root;
    private final Set<Path> tracked;
    private final Properties modules = new Properties();
    private final Map<Path, String> pathDigests = new HashMap<>();
    private final Map<String, String> moduleDigests = new HashMap<>();

    SmokeInputFingerprint(Path root) throws IOException {
        this.root = root.toAbsolutePath().normalize();
        this.tracked = SmokeTrackedFiles.read(this.root);
        try (java.io.Reader reader = Files.newBufferedReader(
                this.root.resolve("harness.properties"), StandardCharsets.UTF_8)) {
            modules.load(reader);
        }
    }

    String compute(SmokeDiscovery.Entry smoke) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-smoke-input-v2");
        update(digest, smoke.id); update(digest, smoke.runner);
        update(digest, System.getProperty("java.runtime.version", System.getProperty("java.version")));
        update(digest, System.getProperty("os.name")); update(digest, System.getProperty("os.arch"));
        addProcessConfiguration(digest, source(root.resolve(smoke.runner)));
        add(digest, root.resolve("smokes").resolve(smoke.id));
        Path runner = root.resolve(smoke.runner); add(digest, runner);
        String source = source(runner);
        add(digest, root.resolve("tools/harness/SmokeProcess.java"));
        if (source.contains("SmokeSupport")) add(digest, root.resolve("tools/harness/SmokeSupport.java"));
        if (source.contains("SmokeRetry")) add(digest, root.resolve("tools/harness/SmokeRetry.java"));
        if (source.contains("modules/smoketest")) add(digest, root.resolve("modules/smoketest"));
        for (String input : REPOSITORY_INPUTS) if (source.contains(input)) add(digest, root.resolve(input));
        Set<String> products = products(source);
        for (String product : products) update(digest, "module:" + product + ":" + module(product));
        if (source.contains(".worldline/build/classes") && products.isEmpty())
            throw new IllegalStateException("cannot identify product inputs for smoke " + smoke.id);
        return HexFormat.of().formatHex(digest.digest());
    }

    private void addProcessConfiguration(MessageDigest digest, String source) throws Exception {
        for (String name : List.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS"))
            update(digest, name + "=" + System.getenv().getOrDefault(name, ""));
        List<String> properties = System.getProperties().stringPropertyNames().stream()
                .filter(name -> name.startsWith("worldline.")).sorted().collect(Collectors.toList());
        for (String name : properties) update(digest, name + "=" + System.getProperty(name));
        Matcher matcher = ENVIRONMENT.matcher(source); Set<String> names = new HashSet<>();
        while (matcher.find()) names.add(matcher.group(1));
        for (String name : names.stream().sorted().toList()) {
            String value = System.getenv().getOrDefault(name, ""); update(digest, name + "=" + value);
            if (name.equals("WORLDLINE_AERO_PREBUILT") && !value.isBlank()) {
                Path file = Path.of(value).toAbsolutePath().normalize();
                require(Files.isRegularFile(file), "missing smoke environment input: " + file);
                update(digest, pathDigest(file));
            }
        }
    }

    private static String source(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private Set<String> products(String source) {
        Set<String> result = new HashSet<>(); Matcher matcher = PRODUCT.matcher(source);
        while (matcher.find()) result.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        if (source.contains(".worldline/build/classes")) {
            matcher = RESOLVE.matcher(source);
            while (matcher.find()) if (modules.getProperty(
                    "module." + matcher.group(1) + ".dependencies") != null) result.add(matcher.group(1));
        }
        return result;
    }

    private String module(String name) throws Exception {
        String cached = moduleDigests.get(name); if (cached != null) return cached;
        require(modules.getProperty("module." + name + ".dependencies") != null,
                "unknown smoke product module: " + name);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, name);
        update(digest, modules.getProperty("module." + name + ".release",
                modules.getProperty("java.release", "")));
        for (String dependency : list(modules.getProperty("module." + name + ".dependencies")))
            update(digest, dependency + ":" + module(dependency));
        add(digest, root.resolve("modules").resolve(name).resolve("src/main/java"));
        String value = HexFormat.of().formatHex(digest.digest()); moduleDigests.put(name, value); return value;
    }

    private void add(MessageDigest digest, Path path) throws Exception {
        path = path.toAbsolutePath().normalize();
        require(path.startsWith(root), "smoke fingerprint escaped repository: " + path);
        update(digest, root.relativize(path).toString().replace('\\', '/'));
        update(digest, pathDigest(path));
    }

    private String pathDigest(Path path) throws Exception {
        String cached = pathDigests.get(path); if (cached != null) return cached;
        require(Files.exists(path), "missing smoke input: " + root.relativize(path));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        if (Files.isRegularFile(path)) {
            require(!path.startsWith(root) || tracked.contains(path),
                    "untracked smoke input: " + root.relativize(path));
            digest.update(Files.readAllBytes(path));
        } else {
            List<Path> files = tracked.stream().filter(item -> item.startsWith(path))
                    .filter(Files::isRegularFile).sorted(Comparator.comparing(
                            item -> path.relativize(item).toString())).collect(Collectors.toList());
            require(!files.isEmpty(), "smoke input has no tracked files: " + root.relativize(path));
            for (Path file : files) {
                update(digest, path.relativize(file).toString().replace('\\', '/'));
                digest.update(Files.readAllBytes(file));
            }
        }
        String value = HexFormat.of().formatHex(digest.digest()); pathDigests.put(path, value); return value;
    }

    private static List<String> list(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        List<String> values = new ArrayList<>();
        for (String value : raw.split(",")) if (!value.isBlank()) values.add(value.trim());
        return values;
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
