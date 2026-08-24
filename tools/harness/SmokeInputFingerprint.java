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
            "adapters/b173-client", "adapters/b173-server", "adapters/stationapi",
            "adapters/aero-model-lib",
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
        return compute(smoke, true);
    }

    String computeExecution(SmokeDiscovery.Entry smoke) throws Exception {
        String qualification = compute(smoke);
        if (LaneDifferential.portableQualification(root, smoke)
                || LaneDifferential.platform().equals("windows")) return qualification;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-smoke-lane-bound-execution-v1");
        update(digest, qualification);
        update(digest, SmokeLane.classify(root, smoke));
        update(digest, LaneDifferential.platform());
        update(digest, System.getProperty("os.arch", "unknown"));
        update(digest, Integer.toString(Runtime.version().feature()));
        return HexFormat.of().formatHex(digest.digest());
    }

    String computeRuntime(SmokeDiscovery.Entry smoke) throws Exception {
        return compute(smoke, false);
    }

    private String compute(SmokeDiscovery.Entry smoke, boolean qualification) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        boolean portable = qualification;
        update(digest, portable ? "worldline-smoke-input-v5-portable"
                : "worldline-smoke-observation-v1");
        update(digest, smoke.id); update(digest, smoke.runner);
        if (!portable) {
            update(digest, System.getProperty("java.runtime.version", System.getProperty("java.version")));
            update(digest, System.getProperty("os.name")); update(digest, System.getProperty("os.arch"));
        }
        addProcessConfiguration(digest, source(root.resolve(smoke.runner)));
        if (qualification) add(digest, root.resolve("smokes").resolve(smoke.id));
        else addRuntimeInputs(digest, smoke.id);
        addSharedInputs(digest, smoke.id, qualification);
        Path runner = root.resolve(smoke.runner); add(digest, runner);
        String source = source(runner);
        add(digest, root.resolve("tools/harness/SmokeProcess.java"));
        if (source.contains("SmokeSupport")) add(digest, root.resolve("tools/harness/SmokeSupport.java"));
        if (source.contains("SmokeRetry")) add(digest, root.resolve("tools/harness/SmokeRetry.java"));
        if (source.contains("DataDrivenCyclePlan"))
            add(digest, root.resolve("tools/harness/DataDrivenCyclePlan.java"));
        if (source.contains("CompositeCyclePlan"))
            add(digest, root.resolve("tools/harness/CompositeCyclePlan.java"));
        if (source.contains("DataDrivenSupport")) {
            add(digest, root.resolve("tools/harness/DataDrivenSupport.java"));
            add(digest, root.resolve("tools/harness/SmokeSupport.java"));
        }
        if (source.contains("SmokeRetryBoundary")) {
            add(digest, root.resolve("tools/harness/SmokeRetryBoundary.java"));
            add(digest, root.resolve("tools/harness/SmokeRetry.java"));
            add(digest, root.resolve("tools/harness/SmokeSupport.java"));
        }
        if (source.contains("ExceptionalSmokeSupport")) {
            add(digest, root.resolve("tools/harness/ExceptionalSmokeSupport.java"));
            add(digest, root.resolve("tools/harness/SmokeSupport.java"));
        }
        if (source.contains("modules/smoketest")) add(digest, root.resolve("modules/smoketest"));
        boolean dataDriven = Set.of("tools/smoke/DataDrivenCycle.java",
                "tools/smoke/CompositeCycle.java").contains(smoke.runner);
        if (dataDriven) addDataDrivenInputs(digest, smoke.id);
        else for (String input : REPOSITORY_INPUTS)
            if (source.contains(input)) add(digest, root.resolve(input));
        Set<String> products = dataDriven ? dataDrivenProducts(smoke.id) : products(source);
        for (String product : products) update(digest, "module:" + product + ":" + module(product));
        if (source.contains(".worldline/build/classes") && products.isEmpty())
            throw new IllegalStateException("cannot identify product inputs for smoke " + smoke.id);
        return HexFormat.of().formatHex(digest.digest());
    }

    private void addRuntimeInputs(MessageDigest digest, String id) throws Exception {
        Path directory = root.resolve("smokes").resolve(id);
        Properties descriptor = descriptor(id);
        for (String key : descriptor.stringPropertyNames().stream().sorted().toList())
            if (!qualificationOnly(key)) update(digest, "descriptor:" + key + "="
                    + descriptor.getProperty(key).trim());
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                String name = path.getFileName().toString();
                if (!name.equals("smoke.properties") && !name.endsWith(".md")) add(digest, path);
            }
        }
    }

    private void addDataDrivenInputs(MessageDigest digest, String id) throws Exception {
        Properties descriptor = descriptor(id);
        String artifact = descriptor.getProperty("cycle.artifact", "").trim();
        require(artifact.matches("artifacts/[a-z0-9.-]+\\.properties"),
                "unsafe data-driven artifact: " + id);
        add(digest, root.resolve(artifact));
        for (String input : list(descriptor.getProperty("cycle.inputs"))) {
            require(input.matches("(?:adapters|modules)/[a-z0-9-]+/src/main/java"),
                    "unsafe data-driven input: " + input);
            add(digest, root.resolve(input));
        }
    }

    private Set<String> dataDrivenProducts(String id) throws IOException {
        Properties descriptor = descriptor(id); Set<String> result = new HashSet<>();
        result.addAll(list(descriptor.getProperty("cycle.compile.products")));
        result.addAll(list(descriptor.getProperty("cycle.runtime.products"))); return result;
    }

    private static boolean qualificationOnly(String key) {
        return key.equals("behavior") || key.startsWith("expected.") || key.startsWith("atlas.")
                || key.startsWith("testkit.") || key.startsWith("qualification.")
                || key.equals("cycle.migration") || key.startsWith("performance.")
                || key.endsWith("mapping.sha256");
    }

    private void addSharedInputs(MessageDigest digest, String id, boolean qualification)
            throws Exception {
        Properties descriptor = descriptor(id);
        String raw = descriptor.getProperty("shared.inputs", "").trim();
        if (!raw.isEmpty()) for (String value : raw.split(",")) {
            String path = value.trim();
            require(path.matches("smokes/shared/[a-z0-9/-]+"), "unsafe shared smoke input: " + path);
            add(digest, root.resolve(path));
        }
        if (!qualification) return;
        String budget = descriptor.getProperty("performance.budget", "").trim();
        if (!budget.isEmpty()) {
            require(budget.matches("quality/[a-z0-9-]+\\.properties"),
                    "unsafe smoke performance budget: " + budget);
            add(digest, root.resolve(budget));
        }
    }

    private Properties descriptor(String id) throws IOException {
        return StrictProperties.load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
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
            digest.update(PortableText.normalize(Files.readAllBytes(path)));
        } else {
            List<Path> files = tracked.stream().filter(item -> item.startsWith(path))
                    .filter(Files::isRegularFile).sorted(Comparator.comparing(
                            item -> path.relativize(item).toString())).collect(Collectors.toList());
            require(!files.isEmpty(), "smoke input has no tracked files: " + root.relativize(path));
            for (Path file : files) {
                update(digest, path.relativize(file).toString().replace('\\', '/'));
                digest.update(PortableText.normalize(Files.readAllBytes(file)));
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
