package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class InstallVerifierTest {
    private InstallVerifierTest() { }
    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-install-verifier-");
        String priorVersion = System.getProperty("worldline.install.version");
        String priorPins = System.getProperty("worldline.install.pins");
        try {
            String version = "9.8.7";
            Path api = root.resolve("worldline-test-api-" + version + ".jar");
            Path runner = root.resolve("worldline-test-runner-" + version + ".jar");
            Files.writeString(api, "api\n", StandardCharsets.UTF_8);
            Files.writeString(runner, "runner\n", StandardCharsets.UTF_8);
            Path pins = root.resolve("checksums.properties");
            Files.writeString(pins, "format=1\n" + api.getFileName() + "=" + digest(api) + "\n"
                    + runner.getFileName() + "=" + digest(runner) + "\n", StandardCharsets.UTF_8);
            System.setProperty("worldline.install.version", version);
            System.setProperty("worldline.install.pins", pins.toUri().toString());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            require(InstallVerifier.run(new String[] {root.toString()}, new PrintStream(bytes)) == 0
                            && bytes.toString(StandardCharsets.UTF_8).contains(
                                    "WORLDLINE_VERIFY_INSTALL=PASS"),
                    "valid TestKit installation was rejected");
            Files.writeString(runner, "drift\n", StandardCharsets.UTF_8);
            boolean rejected = false;
            try { InstallVerifier.run(new String[] {root.toString()}, System.out); }
            catch (IllegalStateException expected) { rejected = true; }
            require(rejected, "altered TestKit installation was accepted");
        } finally {
            restore("worldline.install.version", priorVersion);
            restore("worldline.install.pins", priorPins);
            Files.deleteIfExists(root.resolve("worldline-test-api-9.8.7.jar"));
            Files.deleteIfExists(root.resolve("worldline-test-runner-9.8.7.jar"));
            Files.deleteIfExists(root.resolve("checksums.properties")); Files.deleteIfExists(root);
        }
        System.out.println("InstallVerifierTest passed");
    }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key); else System.setProperty(key, value);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
