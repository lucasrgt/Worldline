import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Properties;

/** Verifies proprietary client/server inputs and the pinned open-source toolchain. */
public final class RuntimeCheck {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final boolean required;

    private RuntimeCheck(boolean required) {
        this.required = required;
    }

    public static void main(String[] arguments) {
        boolean required = Arrays.equals(arguments, new String[] {"--required"});
        if (arguments.length > 0 && !required) {
            System.err.println("usage: java tools/harness/RuntimeCheck.java [--required]");
            System.exit(2);
        }
        try {
            new RuntimeCheck(required).execute();
        } catch (Exception error) {
            System.err.println("runtime check failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Properties client = load("artifacts/minecraft-b1.7.3-client.properties");
        Properties server = load("artifacts/minecraft-b1.7.3-server.properties");
        Properties toolchain = load("toolchains/retromcp.properties");
        Path clientJar = localPath(client, "local.path");
        Path serverJar = localPath(server, "local.path");
        Path checkout = localPath(toolchain, "local.path");
        boolean clientPresent = Files.isRegularFile(clientJar);
        boolean serverPresent = Files.isRegularFile(serverJar);
        boolean toolchainPresent = Files.isDirectory(checkout.resolve(".git"));
        if (!clientPresent && !serverPresent && !toolchainPresent && !required) {
            System.out.println("  runtime inputs: absent (runtime checks not requested)");
            return;
        }
        if (required && (!clientPresent || !serverPresent || !toolchainPresent)) {
            throw new IllegalStateException(
                    "runtime inputs are incomplete; run Acquire.java all and the toolchain setup");
        }
        if (clientPresent) verifyArtifact(client, clientJar);
        if (serverPresent) verifyArtifact(server, serverJar);
        if (toolchainPresent) verifyToolchain(toolchain, checkout);
        System.out.println(clientPresent && serverPresent && toolchainPresent
                ? "  runtime inputs: verified b1.7.3 client/server and pinned RetroMCP"
                : "  runtime inputs: available inputs verified (runtime profile not requested)");
    }

    private void verifyArtifact(Properties descriptor, Path jar) throws Exception {
        long expectedBytes = Long.parseLong(required(descriptor, "expected.bytes"));
        String sha1 = required(descriptor, "expected.sha1");
        String sha256 = required(descriptor, "expected.sha256");
        if (!sha1.matches("[0-9a-f]{40}") || !sha256.matches("[0-9a-f]{64}")) {
            throw new IllegalStateException("artifact descriptor contains an invalid digest");
        }
        if (Files.size(jar) != expectedBytes
                || !digest(jar, "SHA-1").equals(sha1)
                || !digest(jar, "SHA-256").equals(sha256)) {
            throw new IllegalStateException("local artifact does not match frozen descriptor "
                    + required(descriptor, "id"));
        }
    }

    private void verifyToolchain(Properties descriptor, Path checkout) throws Exception {
        String repository = required(descriptor, "repository");
        String revision = required(descriptor, "revision");
        String origin = git(checkout, "remote", "get-url", "origin").trim();
        String actual = git(checkout, "rev-parse", "HEAD").trim();
        String status = git(checkout, "status", "--porcelain").trim();
        if (!repository.equals(origin) || !revision.equals(actual) || !status.isEmpty()) {
            throw new IllegalStateException("local RetroMCP checkout does not match its pinned clean revision");
        }
        Path cli = checkout.resolve(required(descriptor, "artifact.path")).normalize();
        if (!cli.startsWith(checkout) || !Files.isRegularFile(cli) || Files.size(cli) == 0) {
            throw new IllegalStateException("pinned RetroMCP CLI has not been built");
        }
    }

    private Properties load(String relative) throws IOException {
        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(root.resolve(relative), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private Path localPath(Properties descriptor, String key) {
        Path local = root.resolve("local").normalize();
        Path path = root.resolve(required(descriptor, key)).normalize();
        if (!path.startsWith(local) || path.equals(local)) {
            throw new IllegalStateException(key + " must be inside local/");
        }
        return path;
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("missing descriptor property: " + key);
        }
        return value.trim();
    }

    private String digest(Path path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, count);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private String git(Path checkout, String... arguments) throws Exception {
        String[] command = new String[arguments.length + 3];
        command[0] = "git";
        command[1] = "-C";
        command[2] = checkout.toString();
        System.arraycopy(arguments, 0, command, 3, arguments.length);
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(root.toFile())
                .redirectErrorStream(true);
        // Hooks export GIT_DIR for this repository; -C cannot override it.
        builder.environment().keySet().removeIf(key -> key.startsWith("GIT_"));
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) {
            throw new IllegalStateException("git could not inspect the RetroMCP checkout\n" + output);
        }
        return output;
    }
}
