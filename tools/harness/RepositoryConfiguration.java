import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/** Owns validated repository module configuration independently from stage orchestration. */
final class RepositoryConfiguration {
    private final Path root;
    private final Properties values = new Properties();
    RepositoryConfiguration(Path root) { this.root = root; }

    void load() throws Exception {
        try (Reader reader = Files.newBufferedReader(root.resolve("harness.properties"),
                StandardCharsets.UTF_8)) { values.load(reader); }
    }
    List<String> modules() {
        List<String> modules = list("modules"); require(!modules.isEmpty(), "at least one module is required");
        List<String> seen = new ArrayList<>();
        for (String module : modules) {
            Path main = moduleRoot(module).resolve("src/main/java");
            require(Files.isDirectory(main), "missing production source root: " + root.relativize(main));
            for (String dependency : list("module." + module + ".dependencies"))
                require(seen.contains(dependency), "module " + module
                        + " depends on undeclared, unknown, or later module " + dependency);
            seen.add(module);
        }
        return modules;
    }
    Properties values() { return values; }
    String required(String key) {
        String value = values.getProperty(key); require(value != null,
                "missing harness property: " + key); return value;
    }
    List<Path> productionRoots(List<String> modules) {
        return modules.stream().map(module -> moduleRoot(module).resolve("src/main/java")).toList();
    }
    private List<String> list(String key) {
        String raw = required(key).trim(); if (raw.isEmpty()) return Collections.emptyList();
        return Arrays.stream(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList();
    }
    private Path moduleRoot(String module) { return root.resolve("modules").resolve(module); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
