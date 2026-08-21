package dev.worldline.gradle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gradle.api.Project;

/** Deterministic CLI, environment, global-profile, then local-drop-zone resolution. */
final class OraclePaths {
    static final String CLIENT_SHA256 = "af1fa04b8006d3ef78c7e24f8de4aa56f439a74d7f314827529062d5bab6db4c";
    static final String SERVER_SHA256 = "033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d";
    static final long CLIENT_BYTES = 1_465_375L, SERVER_BYTES = 503_100L;
    final Path client, server, retroMcp;

    private OraclePaths(Path client, Path server, Path retroMcp) {
        this.client = client; this.server = server; this.retroMcp = retroMcp;
    }
    static OraclePaths resolve(Project project, WorldlineExtension extension) {
        Path root = project.getProjectDir().toPath().toAbsolutePath().normalize();
        WorldlineConfig projectConfig = WorldlineConfig.read(root.resolve("worldline.toml"));
        String profile = extension.getOracleProfile().getOrElse(
                projectConfig.value("profile", "b173-local"));
        Path globalPath = Paths.get(System.getProperty("user.home"), ".worldline", "config.toml");
        WorldlineConfig global = WorldlineConfig.read(globalPath);
        Path local = root.resolve(".local/oracles/b1.7.3");
        Path client = path(project, "worldline.clientJar", "WORLDLINE_CLIENT_JAR",
                global.value("profiles." + profile + ".clientJar"), local.resolve("minecraft.jar"));
        Path server = path(project, "worldline.serverJar", "WORLDLINE_SERVER_JAR",
                global.value("profiles." + profile + ".serverJar"), local.resolve("minecraft_server.jar"));
        Path toolchain = path(project, "worldline.retroMcpRoot", "WORLDLINE_RETROMCP_ROOT",
                global.value("profiles." + profile + ".retroMcp"), local.resolve("retromcp-java"));
        String artifactRoot = environment("WORLDLINE_ARTIFACT_ROOT");
        if (artifactRoot != null) {
            Path shared = Paths.get(artifactRoot).toAbsolutePath().normalize().resolve("b1.7.3");
            if (!explicit(project, "worldline.clientJar", "WORLDLINE_CLIENT_JAR", global,
                    "profiles." + profile + ".clientJar")) client = shared.resolve("minecraft.jar");
            if (!explicit(project, "worldline.serverJar", "WORLDLINE_SERVER_JAR", global,
                    "profiles." + profile + ".serverJar")) server = shared.resolve("minecraft_server.jar");
        }
        return new OraclePaths(client, server, toolchain);
    }
    private static Path path(Project project, String property, String env, String global, Path fallback) {
        Object configured = project.findProperty(property);
        String value = configured == null ? environment(env) : configured.toString();
        if (value == null) value = global;
        return value == null ? fallback.toAbsolutePath().normalize()
                : Paths.get(value).toAbsolutePath().normalize();
    }
    private static boolean explicit(Project project, String property, String env,
            WorldlineConfig global, String key) {
        return project.findProperty(property) != null || environment(env) != null || global.value(key) != null;
    }
    private static String environment(String key) {
        String value = System.getenv(key); return value == null || value.trim().isEmpty() ? null : value.trim();
    }
    boolean clientPresent() { return Files.isRegularFile(client); }
    boolean serverPresent() { return Files.isRegularFile(server); }
}
