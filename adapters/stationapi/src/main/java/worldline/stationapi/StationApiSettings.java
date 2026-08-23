package worldline.stationapi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Validated local inputs for one external StationAPI runtime session. */
final class StationApiSettings {
    final Path checkout, initScript, serverJar;
    final String clientSha256;
    final int timeoutSeconds;

    private StationApiSettings(Path checkout, Path initScript, Path serverJar, String clientSha256,
            int timeoutSeconds) {
        this.checkout = checkout; this.initScript = initScript;
        this.serverJar = serverJar; this.clientSha256 = clientSha256;
        this.timeoutSeconds = timeoutSeconds;
    }

    static StationApiSettings load() {
        Path checkout = path("worldline.stationapi.checkout");
        Path init = path("worldline.stationapi.init");
        Path server = path("worldline.stationapi.serverJar");
        String clientSha = System.getProperty("worldline.stationapi.clientSha256", "").trim();
        require(Files.isDirectory(checkout.resolve(".git")), "StationAPI checkout is absent");
        require(Files.isRegularFile(checkout.resolve("stationapi/test-bare/build.gradle")),
                "StationAPI test-bare project is absent");
        require(Files.isRegularFile(init), "StationAPI driver init script is absent");
        require(Files.isRegularFile(server), "official server JAR is absent");
        require(clientSha.matches("[0-9a-f]{64}"), "official client SHA-256 is absent");
        int timeout = Integer.parseInt(System.getProperty("worldline.stationapi.timeoutSeconds", "180"));
        require(timeout >= 30 && timeout <= 900, "StationAPI timeout must be 30..900 seconds");
        return new StationApiSettings(checkout, init, server, clientSha, timeout);
    }

    Path project() { return checkout.resolve("stationapi/test-bare"); }
    Path wrapper() {
        return project().resolve(System.getProperty("os.name", "").startsWith("Windows")
                ? "gradlew.bat" : "gradlew");
    }
    private static Path path(String key) {
        String value = System.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing system property " + key);
        return Paths.get(value.trim()).toAbsolutePath().normalize();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
