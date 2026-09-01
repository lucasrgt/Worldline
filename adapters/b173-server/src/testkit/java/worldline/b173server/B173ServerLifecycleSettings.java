package worldline.b173server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;

/** Validates caller-owned inputs for the optional official lifecycle provider. */
final class B173ServerLifecycleSettings {
    static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    static final String SERVER_SHA256 =
            "033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d";
    static final long SERVER_BYTES = 503_100L;

    final Path serverJar;
    final Duration timeout;

    private B173ServerLifecycleSettings(Path serverJar, Duration timeout) {
        this.serverJar = serverJar;
        this.timeout = timeout;
    }

    static B173ServerLifecycleSettings load() {
        String value = System.getProperty(SERVER_PROPERTY, "").trim();
        require(!value.isEmpty(), "missing system property " + SERVER_PROPERTY);
        Path jar = Paths.get(value).toAbsolutePath().normalize();
        require(Files.isRegularFile(jar), "official server JAR is absent");
        try {
            require(Files.size(jar) == SERVER_BYTES, "official server JAR size mismatch");
            byte[] bytes = Files.readAllBytes(jar);
            require(hex(MessageDigest.getInstance("SHA-256").digest(bytes)).equals(SERVER_SHA256),
                    "official server JAR SHA-256 mismatch");
        } catch (java.io.IOException error) {
            throw new IllegalStateException("could not read official server JAR", error);
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
        int seconds;
        try {
            seconds = Integer.parseInt(System.getProperty(
                    "worldline.b173.lifecycle.timeoutSeconds", "180"));
        } catch (NumberFormatException error) {
            throw new IllegalStateException("invalid lifecycle timeout", error);
        }
        require(seconds >= 30 && seconds <= 900,
                "lifecycle timeout must be 30..900 seconds");
        return new B173ServerLifecycleSettings(jar, Duration.ofSeconds(seconds));
    }

    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder();
        for (byte item : bytes) value.append(String.format("%02x", item & 255));
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
