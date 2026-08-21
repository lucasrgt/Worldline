package worldline.cli;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared local, environment, and global-profile oracle resolution for bootstrap commands. */
final class WorldlineOracleSettings {
    final Path client, server; final boolean hostOnly;
    private WorldlineOracleSettings(Path client, Path server, boolean hostOnly) {
        this.client = client; this.server = server; this.hostOnly = hostOnly;
    }
    static WorldlineOracleSettings resolve(Path project) throws Exception {
        Map<String, String> localConfig = read(project.resolve("worldline.toml"));
        String profile = localConfig.getOrDefault("profile", "b173-local");
        Map<String, String> global = read(Paths.get(System.getProperty("user.home"), ".worldline", "config.toml"));
        Path local = project.resolve(".local/oracles/b1.7.3");
        Path client = path("WORLDLINE_CLIENT_JAR", global.get("profiles." + profile + ".clientJar"),
                local.resolve("minecraft.jar"));
        Path server = path("WORLDLINE_SERVER_JAR", global.get("profiles." + profile + ".serverJar"),
                local.resolve("minecraft_server.jar"));
        String shared = environment("WORLDLINE_ARTIFACT_ROOT");
        if (shared != null && environment("WORLDLINE_CLIENT_JAR") == null
                && global.get("profiles." + profile + ".clientJar") == null)
            client = Paths.get(shared).resolve("b1.7.3/minecraft.jar").toAbsolutePath().normalize();
        if (shared != null && environment("WORLDLINE_SERVER_JAR") == null
                && global.get("profiles." + profile + ".serverJar") == null)
            server = Paths.get(shared).resolve("b1.7.3/minecraft_server.jar").toAbsolutePath().normalize();
        return new WorldlineOracleSettings(client, server,
                Boolean.parseBoolean(localConfig.getOrDefault("noRuntime", "false")));
    }
    private static Map<String, String> read(Path path) throws Exception {
        Map<String, String> result = new LinkedHashMap<>(); if (!Files.isRegularFile(path)) return result;
        String section = "";
        for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = raw.trim(); if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) { section = line.substring(1, line.length() - 1); continue; }
            int split = line.indexOf('='); if (split <= 0) throw new IllegalArgumentException("invalid config: " + path);
            String value = line.substring(split + 1).trim();
            if (value.length() < 2 || !value.startsWith("\"") || !value.endsWith("\""))
                throw new IllegalArgumentException("invalid config: " + path);
            String key = line.substring(0, split).trim();
            result.put(section.isEmpty() ? key : section + "." + key, value.substring(1, value.length() - 1));
        }
        return result;
    }
    private static Path path(String env, String global, Path fallback) {
        String value = environment(env); if (value == null) value = global;
        return value == null ? fallback.toAbsolutePath().normalize() : Paths.get(value).toAbsolutePath().normalize();
    }
    private static String environment(String key) {
        String value = System.getenv(key); return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
