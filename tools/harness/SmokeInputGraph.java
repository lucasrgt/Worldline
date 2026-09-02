import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

/** Authoritative qualification graph with a retained legacy-equivalence audit. */
final class SmokeInputGraph {
    private static final String ALGORITHM = "worldline-smoke-input-v6-tokens";
    private final Path root;
    private final SmokeInputFingerprint inputs;

    SmokeInputGraph(Path root, SmokeInputFingerprint inputs) {
        this.root = root.toAbsolutePath().normalize(); this.inputs = inputs;
    }

    SmokeInputGraphResult qualification(SmokeDiscovery.Entry smoke) throws Exception {
        Collector graph = new Collector(smoke.id);
        graph.scalar("identity", "algorithm", ALGORITHM);
        graph.scalar("identity", "smoke", smoke.id);
        graph.scalar("identity", "runner", smoke.runner);
        Path runner = root.resolve(smoke.runner);
        String source = SmokeInputFingerprint.source(runner);
        processConfiguration(graph, source);
        add(graph, root.resolve("smokes").resolve(smoke.id));
        sharedInputs(graph, smoke.id);
        add(graph, runner);
        add(graph, root.resolve("tools/harness/SmokeProcess.java"));
        conditionalInputs(graph, source);
        boolean dataDriven = Set.of("tools/smoke/DataDrivenCycle.java",
                "tools/smoke/CompositeCycle.java").contains(smoke.runner);
        if (dataDriven) dataDrivenInputs(graph, smoke.id);
        else for (String input : SmokeInputFingerprint.REPOSITORY_INPUTS)
            if (source.contains(input)) add(graph, root.resolve(input));
        Set<String> products = dataDriven ? inputs.dataDrivenProducts(smoke.id)
                : inputs.products(source);
        for (String product : products) graph.module(product, inputs.module(product, true));
        require(!source.contains(".worldline/build/classes") || !products.isEmpty(),
                "cannot identify product inputs for smoke " + smoke.id);
        return graph.finish();
    }

    private void conditionalInputs(Collector graph, String source) throws Exception {
        if (source.contains("SmokeSupport")) add(graph, root.resolve("tools/harness/SmokeSupport.java"));
        if (source.contains("SmokeRetry")) add(graph, root.resolve("tools/harness/SmokeRetry.java"));
        if (source.contains("DataDrivenCyclePlan"))
            add(graph, root.resolve("tools/harness/DataDrivenCyclePlan.java"));
        if (source.contains("CompositeCyclePlan"))
            add(graph, root.resolve("tools/harness/CompositeCyclePlan.java"));
        if (source.contains("DataDrivenSupport")) {
            add(graph, root.resolve("tools/harness/DataDrivenSupport.java"));
            add(graph, root.resolve("tools/harness/SmokeSupport.java"));
        }
        if (source.contains("SmokeRetryBoundary")) {
            add(graph, root.resolve("tools/harness/SmokeRetryBoundary.java"));
            add(graph, root.resolve("tools/harness/SmokeRetry.java"));
            add(graph, root.resolve("tools/harness/SmokeSupport.java"));
        }
        if (source.contains("ExceptionalSmokeSupport")) {
            add(graph, root.resolve("tools/harness/ExceptionalSmokeSupport.java"));
            add(graph, root.resolve("tools/harness/SmokeSupport.java"));
        }
        if (source.contains("modules/smoketest")) add(graph, root.resolve("modules/smoketest"));
    }

    private void dataDrivenInputs(Collector graph, String id) throws Exception {
        Properties descriptor = inputs.descriptor(id);
        String artifact = descriptor.getProperty("cycle.artifact", "").trim();
        require(artifact.matches("artifacts/[a-z0-9.-]+\\.properties"),
                "unsafe data-driven artifact: " + id);
        add(graph, root.resolve(artifact));
        for (String input : list(descriptor.getProperty("cycle.inputs"))) {
            require(input.matches("(?:adapters|modules)/[a-z0-9-]+/src/(?:main|testkit)/java"),
                    "unsafe data-driven input: " + input);
            add(graph, root.resolve(input));
        }
    }

    private void sharedInputs(Collector graph, String id) throws Exception {
        Properties descriptor = inputs.descriptor(id);
        for (String path : list(descriptor.getProperty("shared.inputs"))) {
            require(path.matches("smokes/shared/[a-z0-9/-]+"), "unsafe shared smoke input: " + path);
            add(graph, root.resolve(path));
        }
        String budget = descriptor.getProperty("performance.budget", "").trim();
        if (!budget.isEmpty()) {
            require(budget.matches("quality/[a-z0-9-]+\\.properties"),
                    "unsafe smoke performance budget: " + budget);
            add(graph, root.resolve(budget));
        }
    }

    private void processConfiguration(Collector graph, String source) throws Exception {
        for (String name : List.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS"))
            graph.configuration("environment", name, System.getenv().getOrDefault(name, ""));
        for (String name : System.getProperties().stringPropertyNames().stream()
                .filter(value -> value.startsWith("worldline.")).sorted().toList())
            graph.configuration("system-property", name, System.getProperty(name));
        Matcher matcher = SmokeInputFingerprint.ENVIRONMENT.matcher(source);
        Set<String> names = new HashSet<>(); while (matcher.find()) names.add(matcher.group(1));
        for (String name : names.stream().sorted().toList()) {
            String value = System.getenv().getOrDefault(name, "");
            graph.configuration("environment", name, value);
            if (name.equals("WORLDLINE_AERO_PREBUILT") && !value.isBlank()) {
                Path file = Path.of(value).toAbsolutePath().normalize();
                require(Files.isRegularFile(file), "missing smoke environment input: " + file);
                String digest = inputs.pathDigest(file, false);
                graph.raw("environment-file", name, digest, digest);
            }
        }
    }

    private void add(Collector graph, Path path) throws Exception {
        path = path.toAbsolutePath().normalize();
        require(path.startsWith(root), "smoke input graph escaped repository: " + path);
        graph.path(root.relativize(path).toString().replace('\\', '/'), inputs.pathDigest(path, true));
    }

    static void selfTest() throws Exception {
        Collector graph = new Collector("fixture");
        graph.scalar("identity", "algorithm", ALGORITHM);
        graph.configuration("environment", "SECRET", "must-not-leak");
        SmokeInputGraphResult result = graph.finish();
        require(result.rootSha256().matches("[0-9a-f]{64}") && result.nodes().size() == 2,
                "input graph did not seal its nodes");
        require(!result.json().contains("must-not-leak")
                && result.json().contains("worldline.smoke-input-graph.v1"),
                "input graph JSON leaked a value or lost its schema");
        System.out.println("  smoke input graph self-test: passed");
    }

    private static List<String> list(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        List<String> values = new ArrayList<>();
        for (String value : raw.split(",")) if (!value.isBlank()) values.add(value.trim());
        return values;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class Collector {
        private final String smoke;
        private final MessageDigest root;
        private final List<SmokeInputGraphResult.Node> nodes = new ArrayList<>();
        Collector(String smoke) throws Exception {
            this.smoke = smoke; root = HexDigest.sha256();
        }
        void scalar(String kind, String key, String value) throws Exception {
            raw(kind, key, value, sha256(value));
        }
        void configuration(String kind, String key, String value) throws Exception {
            raw(kind, key, key + "=" + value, sha256(value));
        }
        void path(String path, String digest) { update(path); update(digest); node("path", path, digest); }
        void module(String name, String digest) {
            update("module:" + name + ":" + digest); node("module", name, digest);
        }
        void raw(String kind, String key, String rootValue, String nodeDigest) {
            update(rootValue); node(kind, key, nodeDigest);
        }
        private void node(String kind, String key, String digest) {
            nodes.add(new SmokeInputGraphResult.Node(nodes.size(), kind, key, digest));
        }
        private void update(String value) {
            root.update(value.getBytes(StandardCharsets.UTF_8)); root.update((byte) 0);
        }
        SmokeInputGraphResult finish() {
            return new SmokeInputGraphResult(smoke, HexFormat.of().formatHex(root.digest()), nodes);
        }
        private static String sha256(String value) throws Exception {
            return HexFormat.of().formatHex(HexDigest.sha256()
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
