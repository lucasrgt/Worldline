import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Validated execution plan for legacy cycles with composite observations. */
public final class CompositeCyclePlan {
    public final String id, mainClass, artifact, tracePrefix, signaturePrefix;
    public final List<String> signalPrefixes, arguments, inputs, nestedSources;
    public final List<String> compileProducts, runtimeProducts;
    public final List<String> outputContains, signalContains, signalExcludes;
    public final List<String> traceContains, traceExcludes;
    public final boolean compareSignal, requireExpectedSignal;
    private final Properties values;

    private CompositeCyclePlan(Properties values) {
        this.values = values; id = required("id"); mainClass = required("cycle.main");
        artifact = required("cycle.artifact"); tracePrefix = required("cycle.trace.prefix");
        signaturePrefix = required("cycle.signature.prefix");
        signalPrefixes = numbered("cycle.signal.prefix"); arguments = list("cycle.args");
        inputs = list("cycle.inputs"); nestedSources = list("cycle.nested.sources");
        compileProducts = list("cycle.compile.products");
        runtimeProducts = list("cycle.runtime.products");
        outputContains = numbered("cycle.output.contains");
        signalContains = numbered("cycle.signal.contains");
        signalExcludes = numbered("cycle.signal.excludes");
        traceContains = numbered("cycle.trace.contains");
        traceExcludes = numbered("cycle.trace.excludes");
        compareSignal = flag("cycle.compare.signal");
        requireExpectedSignal = flag("cycle.require.expected.signal"); validate();
    }

    public static CompositeCyclePlan load(Path root, String id) throws Exception {
        Properties values = new Properties(); Path descriptor = root.resolve("smokes").resolve(id)
                .resolve("smoke.properties");
        try (Reader reader = Files.newBufferedReader(descriptor, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require(id.equals(values.getProperty("id", "").trim()), "cycle id drift: " + id);
        return new CompositeCyclePlan(values);
    }

    public String fingerprint() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-composite-cycle-plan-v1");
        for (String key : values.stringPropertyNames().stream().filter(name -> name.startsWith("cycle."))
                .filter(name -> !name.equals("cycle.migration")).sorted().toList())
            update(digest, key + "=" + values.getProperty(key).trim());
        return HexFormat.of().formatHex(digest.digest());
    }

    private void validate() {
        require("1".equals(required("cycle.composite.schema")), "unsupported composite schema: " + id);
        require(mainClass.matches("worldline\\.(?:smoke|b173server)\\.[A-Za-z0-9_.]+"),
                "unsafe cycle main: " + id);
        require(artifact.matches("artifacts/[a-z0-9.-]+\\.properties"), "unsafe artifact: " + id);
        require(!arguments.isEmpty() && arguments.stream().allMatch(CompositeCyclePlan::propertyName),
                "invalid cycle arguments: " + id);
        require(!inputs.isEmpty() && inputs.stream().allMatch(value ->
                value.matches("(?:adapters|modules)/[a-z0-9-]+/src/main/java")),
                "invalid cycle inputs: " + id);
        require(nestedSources.stream().allMatch(value -> value.matches(
                        "smokes/m[0-9]+-[a-z0-9-]+/src/[A-Za-z0-9_./-]+[.]java")),
                "invalid nested cycle source: " + id);
        require(!signalPrefixes.isEmpty(), "missing signal prefixes: " + id);
        List<String> prefixes = new ArrayList<>(signalPrefixes);
        prefixes.add(tracePrefix); prefixes.add(signaturePrefix);
        for (String prefix : prefixes)
            require(prefix.matches("WORLDLINE_[A-Z0-9_]+="), "invalid output prefix: " + id);
    }

    private boolean flag(String key) {
        String value = required(key); require(value.equals("true") || value.equals("false"),
                "invalid boolean " + key); return Boolean.parseBoolean(value);
    }
    private List<String> list(String key) {
        String raw = values.getProperty(key, "").trim(); if (raw.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        for (String value : raw.split(",")) if (!value.isBlank()) result.add(value.trim());
        return List.copyOf(result);
    }
    private List<String> numbered(String stem) {
        int count; try { count = Integer.parseInt(values.getProperty(stem + ".count", "")); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + stem); }
        require(count >= 0 && count <= 64, "invalid " + stem + " count");
        List<String> result = new ArrayList<>();
        for (int index = 1; index <= count; index++) result.add(required(stem + "." + index));
        return List.copyOf(result);
    }
    private String required(String key) {
        String value = values.getProperty(key); require(value != null && !value.trim().isEmpty(),
                "missing " + key + " for " + values.getProperty("id", "cycle")); return value.trim();
    }
    private static boolean propertyName(String value) { return value.matches("[a-z][a-z0-9.-]*"); }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
