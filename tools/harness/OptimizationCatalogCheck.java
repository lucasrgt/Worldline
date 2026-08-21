import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Fails closed when optimization records, source references, or defaults drift. */
public final class OptimizationCatalogCheck {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)+");
    private static final Pattern REF = Pattern.compile(
            "^[\\t ]*@(?:[A-Za-z0-9_.]+\\.)?OptimizationRef[\\t ]*\\((.*?)\\)",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern STRING = Pattern.compile("\"([^\"]+)\"");
    private static final List<String> REQUIRED = Arrays.asList("schema", "id", "summary",
            "subsystem", "status", "default.enabled", "behavior.delta", "risks",
            "rollback", "tracking", "source.symbols", "evidence");
    private static final List<String> STATUSES =
            Arrays.asList("active", "candidate", "rejected", "retired", "unknown");
    private final Path root;

    private OptimizationCatalogCheck(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public static void main(String[] arguments) {
        if (arguments.length > 1) {
            System.err.println("usage: java tools/harness/OptimizationCatalogCheck.java [repository-root]");
            System.exit(2);
        }
        Path root = arguments.length == 0 ? Paths.get("") : Paths.get(arguments[0]);
        try { new OptimizationCatalogCheck(root).execute(); }
        catch (Exception error) {
            System.err.println("optimization catalog check failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Map<String, Record> catalog = loadCatalog();
        Map<String, List<String>> references = scanReferences();
        for (Map.Entry<String, List<String>> entry : references.entrySet())
            require(catalog.containsKey(entry.getKey()), "unknown optimization reference "
                    + entry.getKey() + " at " + String.join(",", entry.getValue()));
        for (Record record : catalog.values()) {
            boolean annotated = references.containsKey(record.id);
            require(!record.tracking.equals("annotation") || annotated,
                    "annotation-tracked optimization has no source reference " + record.id);
            require(!annotated || record.tracking.equals("annotation"),
                    "symbol-tracked optimization is annotated " + record.id);
        }
        int sites = references.values().stream().mapToInt(List::size).sum();
        System.out.println("  optimization catalog: " + catalog.size() + " records, "
                + sites + " annotated sites");
    }

    private Map<String, Record> loadCatalog() throws IOException {
        Path directory = root.resolve("worldline/optimizations/catalog");
        require(Files.isDirectory(directory), "missing worldline/optimizations/catalog");
        Map<String, Record> result = new LinkedHashMap<String, Record>();
        for (Path path : files(directory, ".properties")) {
            Properties fields = new Properties();
            try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                fields.load(reader);
            }
            for (String key : REQUIRED) required(fields, key, path);
            String id = fields.getProperty("id").trim();
            require(ID.matcher(id).matches(), "invalid optimization id " + id);
            require(path.getFileName().toString().equals(id + ".properties"),
                    "catalog filename must match id " + id);
            require("worldline.optimization.v1".equals(fields.getProperty("schema").trim()),
                    "unsupported optimization schema " + id);
            String status = fields.getProperty("status").trim();
            require(STATUSES.contains(status), "invalid optimization status " + id);
            String enabled = fields.getProperty("default.enabled").trim();
            require(enabled.equals("true") || enabled.equals("false"), "invalid default " + id);
            require(status.equals("active") || enabled.equals("false"),
                    "non-active optimization defaults on " + id);
            String tracking = fields.getProperty("tracking").trim();
            require(tracking.equals("annotation") || tracking.equals("symbol"),
                    "invalid tracking mode " + id);
            String evidence = fields.getProperty("evidence").trim();
            require(status.equals("unknown") || !evidence.equals("none"),
                    "decided optimization lacks evidence " + id);
            require(result.put(id, new Record(id, tracking)) == null,
                    "duplicate optimization id " + id);
        }
        return result;
    }

    private Map<String, List<String>> scanReferences() throws IOException {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        for (String name : Arrays.asList("modules", "adapters", "smokes", "tools")) {
            Path directory = root.resolve(name);
            if (!Files.isDirectory(directory)) continue;
            for (Path path : files(directory, ".java")) {
                String normalized = root.relativize(path).toString().replace('\\', '/');
                if (normalized.contains("/src/test/")) continue;
                String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                Matcher annotation = REF.matcher(source);
                while (annotation.find()) {
                    Matcher strings = STRING.matcher(annotation.group(1)); int count = 0;
                    while (strings.find()) {
                        String id = strings.group(1); count++;
                        require(ID.matcher(id).matches(), "invalid optimization reference " + id);
                        result.computeIfAbsent(id, ignored -> new ArrayList<String>()).add(normalized);
                    }
                    require(count > 0, "OptimizationRef has no literal id at " + normalized);
                }
            }
        }
        return result;
    }

    private List<Path> files(Path directory, String suffix) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> result = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .collect(Collectors.toList());
            Collections.sort(result); return result;
        }
    }

    private String required(Properties fields, String key, Path path) {
        String value = fields.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key + " in " + path);
        return value.trim();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Record {
        final String id, tracking;
        Record(String id, String tracking) { this.id = id; this.tracking = tracking; }
    }
}
