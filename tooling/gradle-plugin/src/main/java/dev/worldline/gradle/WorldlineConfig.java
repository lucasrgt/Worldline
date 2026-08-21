package dev.worldline.gradle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small strict TOML subset for generated Worldline configuration. */
final class WorldlineConfig {
    private final Map<String, String> values;
    private WorldlineConfig(Map<String, String> values) { this.values = values; }

    static WorldlineConfig read(Path path) {
        if (!Files.isRegularFile(path)) return new WorldlineConfig(new LinkedHashMap<>());
        try { return parse(Files.readAllLines(path, StandardCharsets.UTF_8)); }
        catch (IOException error) { throw new IllegalStateException("cannot read " + path, error); }
    }
    static WorldlineConfig parse(List<String> lines) {
        Map<String, String> values = new LinkedHashMap<>(); String section = "";
        for (int number = 0; number < lines.size(); number++) {
            String line = lines.get(number).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
                require(section.matches("[A-Za-z0-9_.-]+"), "invalid section at line " + (number + 1));
                continue;
            }
            int split = line.indexOf('='); require(split > 0, "invalid assignment at line " + (number + 1));
            String key = line.substring(0, split).trim(), raw = line.substring(split + 1).trim();
            require(key.matches("[A-Za-z0-9_.-]+") && raw.length() >= 2
                    && raw.startsWith("\"") && raw.endsWith("\""), "invalid value at line " + (number + 1));
            String value = raw.substring(1, raw.length() - 1);
            require(!value.contains("\"") && !value.contains("\0"), "invalid string at line " + (number + 1));
            String full = section.isEmpty() ? key : section + "." + key;
            require(values.put(full, value) == null, "duplicate key " + full);
        }
        return new WorldlineConfig(values);
    }
    String value(String key) { return values.get(key); }
    String value(String key, String fallback) { String value = values.get(key); return value == null ? fallback : value; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
