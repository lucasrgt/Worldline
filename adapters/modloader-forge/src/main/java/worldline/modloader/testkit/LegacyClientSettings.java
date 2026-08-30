package worldline.modloader.testkit;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/** Validated prepared-client and Java 8 inputs for a legacy TestKit provider. */
final class LegacyClientSettings {
    final String loader;
    final Path workspace, java, natives, probe;
    final String probeTarget;
    final List<Path> classpath;
    final int timeoutSeconds;

    private LegacyClientSettings(String loader, Path workspace, Path java, Path natives,
            Path probe, String probeTarget, List<Path> classpath, int timeoutSeconds) {
        this.loader = loader; this.workspace = workspace; this.java = java;
        this.natives = natives; this.probe = probe; this.probeTarget = probeTarget;
        this.classpath = classpath; this.timeoutSeconds = timeoutSeconds;
    }

    static LegacyClientSettings load(String loader) throws Exception {
        require("modloader".equals(loader) || "forge".equals(loader), "unknown legacy loader");
        Path workspace = path("worldline.legacy." + loader + ".workspace");
        Path java8 = path("worldline.legacy.java8Home");
        Path java = java8.resolve("bin").resolve(windows() ? "java.exe" : "java");
        require(Files.isRegularFile(java), "Java 8 executable is absent");
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(workspace.resolve("worldline-testkit.properties"),
                StandardCharsets.UTF_8)) { values.load(reader); }
        require("worldline.legacy-testkit-client.v1".equals(values.getProperty("schema"))
                && loader.equals(values.getProperty("loader")), "prepared legacy client drifted");
        int count = Integer.parseInt(values.getProperty("classpath.count", "0"));
        require(count >= 3 && count <= 128, "legacy classpath census drifted");
        List<Path> classpath = new ArrayList<Path>();
        for (int index = 1; index <= count; index++)
            classpath.add(child(workspace, required(values, "classpath." + index), true));
        Path natives = child(workspace, required(values, "natives"), false);
        Path probe = child(workspace, required(values, "probe.source"), true);
        String probeTarget = required(values, "probe.target");
        require(!Path.of(probeTarget).isAbsolute() && !probeTarget.contains(".."),
                "unsafe legacy probe target");
        int timeout = Integer.parseInt(System.getProperty("worldline.legacy.timeoutSeconds", "180"));
        require(timeout >= 30 && timeout <= 900, "legacy timeout must be 30..900 seconds");
        return new LegacyClientSettings(loader, workspace, java, natives, probe, probeTarget,
                Collections.unmodifiableList(classpath), timeout);
    }

    private static Path child(Path root, String relative, boolean regularOrDirectory) {
        Path path = root.resolve(relative).normalize();
        require(path.startsWith(root) && (regularOrDirectory ? Files.exists(path) : Files.isDirectory(path)),
                "missing prepared client path " + path); return path;
    }
    private static Path path(String key) {
        String value = System.getProperty(key); require(value != null && !value.trim().isEmpty(), "missing " + key);
        Path path = Path.of(value.trim()).toAbsolutePath().normalize();
        require(Files.isDirectory(path), "missing directory " + path); return path;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static boolean windows() { return System.getProperty("os.name", "").startsWith("Windows"); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
