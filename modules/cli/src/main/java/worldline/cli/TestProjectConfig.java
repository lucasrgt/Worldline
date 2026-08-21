package worldline.cli;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** Strict convention file that makes plain {@code worldline test} deterministic. */
final class TestProjectConfig {
    private static final Set<String> KEYS = new TreeSet<>(Arrays.asList("format", "source", "spec",
            "classpath", "mod", "world", "provider", "reporter", "artifacts", "snapshots", "runtime.lock"));
    private TestProjectConfig() {}

    static String[] expand(String[] arguments) throws IOException {
        Path path = Paths.get("worldline-test.properties");
        if (!Files.isRegularFile(path)) throw new IllegalArgumentException(
                "worldline-test.properties is required for shorthand 'worldline test'");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        for (String key : properties.stringPropertyNames())
            if (!KEYS.contains(key)) throw new IllegalArgumentException("unknown test project property: " + key);
        require("1".equals(properties.getProperty("format")), "test project format must be 1");
        String source = required(properties, "source");
        String spec = arguments.length == 2 ? arguments[1] : properties.getProperty("spec", "*").trim();
        require(!spec.isEmpty(), "blank test project property: spec");
        List<String> expanded = new ArrayList<>(Arrays.asList("test", "run", source, spec));
        option(properties, expanded, "classpath", "--classpath=");
        option(properties, expanded, "mod", "--mod="); option(properties, expanded, "world", "--world=");
        option(properties, expanded, "provider", "--provider=");
        option(properties, expanded, "reporter", "--reporter=");
        option(properties, expanded, "artifacts", "--artifacts=");
        option(properties, expanded, "snapshots", "--snapshots=");
        option(properties, expanded, "runtime.lock", "--runtime-lock=");
        return expanded.toArray(new String[0]);
    }
    private static void option(Properties source, List<String> target, String key, String prefix) {
        String value = source.getProperty(key);
        if (value != null) { require(!value.trim().isEmpty(), "blank test project property: " + key);
            target.add(prefix + value.trim()); }
    }
    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key); require(value != null && !value.trim().isEmpty(),
                "missing test project property: " + key); return value.trim();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
