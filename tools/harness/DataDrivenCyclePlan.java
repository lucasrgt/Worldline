import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Validated execution plan shared by ordinary official-server smoke cycles. */
public final class DataDrivenCyclePlan {
    public final String id, mainClass, artifact, tracePrefix, signaturePrefix, signalPrefix;
    public final List<String> arguments, inputs, compileProducts, runtimeProducts;
    public final List<String> outputContains, signalContains, signalExcludes;
    public final List<String> traceContains, traceExcludes;
    private final Properties values;

    private DataDrivenCyclePlan(Properties values) {
        this.values = values; id = required("id"); mainClass = required("cycle.main");
        artifact = required("cycle.artifact"); tracePrefix = required("cycle.trace.prefix");
        signaturePrefix = required("cycle.signature.prefix");
        signalPrefix = required("cycle.signal.prefix"); arguments = list("cycle.args");
        inputs = list("cycle.inputs"); compileProducts = list("cycle.compile.products");
        runtimeProducts = list("cycle.runtime.products");
        outputContains = numbered("cycle.output.contains");
        signalContains = numbered("cycle.signal.contains");
        signalExcludes = numbered("cycle.signal.excludes");
        traceContains = numbered("cycle.trace.contains");
        traceExcludes = numbered("cycle.trace.excludes");
        validate();
    }

    public static DataDrivenCyclePlan load(Path root, String id) throws Exception {
        Properties values = new Properties(); Path descriptor = root.resolve("smokes").resolve(id)
                .resolve("smoke.properties");
        try (Reader reader = Files.newBufferedReader(descriptor, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require(id.equals(values.getProperty("id", "").trim()), "cycle id drift: " + id);
        return new DataDrivenCyclePlan(values);
    }

    public String value(String key) { return required(key); }

    public String fingerprint() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, "worldline-data-driven-cycle-plan-v1");
        for (String key : values.stringPropertyNames().stream().filter(name -> name.startsWith("cycle."))
                .filter(name -> !name.equals("cycle.migration")).sorted().toList())
            update(digest, key + "=" + values.getProperty(key).trim());
        return HexFormat.of().formatHex(digest.digest());
    }

    private void validate() {
        require("1".equals(required("cycle.schema")), "unsupported cycle schema: " + id);
        require(mainClass.matches("worldline\\.smoke\\.[A-Za-z0-9_.]+"), "unsafe cycle main: " + id);
        require(artifact.matches("artifacts/[a-z0-9.-]+\\.properties"), "unsafe artifact: " + id);
        require(!arguments.isEmpty() && arguments.stream().allMatch(DataDrivenCyclePlan::propertyName),
                "invalid cycle arguments: " + id);
        require(!inputs.isEmpty() && inputs.stream().allMatch(value ->
                value.matches("(?:adapters|modules)/[a-z0-9-]+/src/(?:main|testkit)/java")),
                "invalid cycle inputs: " + id);
        require(compileProducts.stream().allMatch(DataDrivenCyclePlan::moduleName)
                        && runtimeProducts.stream().allMatch(DataDrivenCyclePlan::moduleName),
                "invalid cycle products: " + id);
        for (String prefix : List.of(tracePrefix, signaturePrefix, signalPrefix))
            require(prefix.matches("WORLDLINE_[A-Z0-9_]+="), "invalid output prefix: " + id);
        for (String fragment : outputContains) if (fragment.contains("_SIGNATURE="))
            require(fragment.matches("WORLDLINE_[A-Z0-9_]+_SIGNATURE=[0-9a-f]{64}"),
                    "invalid frozen component signature: " + id);
    }

    private List<String> list(String key) {
        String raw = values.getProperty(key, "").trim(); if (raw.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        for (String value : raw.split(",")) if (!value.isBlank()) result.add(value.trim());
        return List.copyOf(result);
    }

    private List<String> numbered(String stem) {
        String raw = values.getProperty(stem + ".count", "0").trim(); int count;
        try { count = Integer.parseInt(raw); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + stem); }
        require(count >= 0 && count <= 64, "invalid " + stem + " count");
        List<String> result = new ArrayList<>();
        for (int index = 1; index <= count; index++) result.add(required(stem + "." + index));
        return List.copyOf(result);
    }

    private String required(String key) {
        String value = values.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key + " for "
                + values.getProperty("id", "cycle")); return value.trim();
    }

    private static boolean propertyName(String value) { return value.matches("[a-z][a-z0-9.-]*"); }
    private static boolean moduleName(String value) { return value.matches("[a-z0-9-]+"); }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
