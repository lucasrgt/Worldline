import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/** Fails closed when adapter kinds drift from the driver/extension allowlists. */
public final class AdapterKindCheck {
    private static final Set<String> DRIVERS = new LinkedHashSet<String>(
            Arrays.asList("b173-client", "b173-server", "modloader-forge", "stationapi"));
    private final Path root;

    private AdapterKindCheck(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public static void main(String[] arguments) {
        if (arguments.length > 1) {
            System.err.println("usage: java tools/harness/AdapterKindCheck.java [repository-root]");
            System.exit(2);
        }
        Path root = arguments.length == 0 ? Paths.get("") : Paths.get(arguments[0]);
        try { new AdapterKindCheck(root).execute(); }
        catch (Exception error) {
            System.err.println("adapter kind check failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Set<String> drivers = new LinkedHashSet<String>();
        Set<String> extensions = new LinkedHashSet<String>();
        scan(root.resolve("adapters"), "semantics", drivers, extensions);
        scan(root.resolve("worldline").resolve("extensions"), null, drivers, extensions);
        Path harness = root.resolve("harness.properties");
        if (Files.isRegularFile(harness)) {
            Properties fields = load(harness);
            if (fields.containsKey("adapter.drivers"))
                same("adapter.drivers", csv(fields, "adapter.drivers"), drivers);
            if (fields.containsKey("adapter.extensions"))
                same("adapter.extensions", csv(fields, "adapter.extensions"), extensions);
        }
        System.out.println("  adapter kinds: " + drivers.size() + " drivers, "
                + extensions.size() + " extensions");
    }

    private void scan(Path directory, String nested, Set<String> drivers, Set<String> extensions)
            throws IOException {
        if (!Files.isDirectory(directory)) return;
        try (DirectoryStream<Path> children = Files.newDirectoryStream(directory)) {
            for (Path child : children) {
                if (!Files.isDirectory(child)) continue;
                Path manifest = nested == null ? child.resolve("manifest.properties")
                        : child.resolve(nested).resolve("manifest.properties");
                if (!Files.isRegularFile(manifest)) continue;
                classify(manifest, child.getFileName().toString(), drivers, extensions);
            }
        }
    }

    private void classify(Path manifest, String adapter, Set<String> drivers,
            Set<String> extensions) throws IOException {
        Properties fields = load(manifest);
        String name = required(fields, "adapter");
        require(adapter.equals(name), "adapter directory mismatch " + adapter);
        String kind = required(fields, "kind");
        require(kind.equals("driver") || kind.equals("extension"), "invalid kind " + adapter);
        require(kind.equals("driver") == DRIVERS.contains(adapter), "adapter kind " + adapter);
        require((kind.equals("driver") ? drivers : extensions).add(adapter),
                "duplicate adapter " + adapter);
    }

    private Properties load(Path path) throws IOException {
        Properties fields = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            fields.load(reader);
        }
        return fields;
    }

    private Set<String> csv(Properties fields, String key) {
        Set<String> result = new LinkedHashSet<String>();
        for (String item : required(fields, key).split(",")) {
            String value = item.trim();
            require(!value.isEmpty(), "empty " + key);
            require(result.add(value), "duplicate " + key + " " + value);
        }
        return result;
    }

    private void same(String key, Set<String> expected, Set<String> actual) {
        require(expected.equals(actual), key + " is " + actual + "; expected " + expected);
    }

    private String required(Properties fields, String key) {
        String value = fields.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
