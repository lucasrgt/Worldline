import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/** Pins and builds external open-source toolchains under the ignored local root. */
public final class Bootstrap {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {"retromcp"})) {
            System.err.println("usage: java tools/toolchains/Bootstrap.java retromcp");
            System.exit(2);
        }
        try {
            new Bootstrap().execute();
        } catch (Exception error) {
            System.err.println("bootstrap failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        load();
        String repository = required("repository");
        String revision = required("revision");
        Path checkout = root.resolve(required("local.path")).normalize();
        Path local = root.resolve("local").normalize();
        if (!checkout.startsWith(local) || checkout.equals(local)) {
            throw new IllegalStateException("toolchain checkout must be inside local/: " + checkout);
        }

        if (Files.exists(checkout)) {
            requireCleanCheckout(checkout, repository);
            String actual = capture(root, "git", "-C", checkout.toString(), "rev-parse", "HEAD").trim();
            Path artifact = checkout.resolve(required("artifact.path")).normalize();
            if (revision.equals(actual) && Files.isRegularFile(artifact) && Files.size(artifact) > 0) {
                ready(revision, artifact);
                return;
            }
        } else {
            Files.createDirectories(checkout.getParent());
            run(root, "git", "clone", "--no-checkout", repository, checkout.toString());
        }

        run(root, "git", "-C", checkout.toString(), "fetch", "--depth", "1", "origin", revision);
        run(root, "git", "-C", checkout.toString(), "checkout", "--detach", revision);
        String actual = capture(root, "git", "-C", checkout.toString(), "rev-parse", "HEAD").trim();
        if (!revision.equals(actual)) {
            throw new IllegalStateException("RetroMCP revision mismatch: " + actual);
        }

        String wrapper = System.getProperty("os.name").toLowerCase().contains("win")
                ? checkout.resolve("gradlew.bat").toString()
                : checkout.resolve("gradlew").toString();
        run(checkout, wrapper, "--no-daemon", required("build.task"));
        Path artifact = checkout.resolve(required("artifact.path")).normalize();
        if (!artifact.startsWith(checkout) || !Files.isRegularFile(artifact) || Files.size(artifact) == 0) {
            throw new IllegalStateException("expected a non-empty RetroMCP CLI artifact: " + artifact);
        }
        ready(revision, artifact);
    }

    private void ready(String revision, Path artifact) {
        System.out.println("RetroMCP ready");
        System.out.println("  revision: " + revision);
        System.out.println("  artifact: " + root.relativize(artifact));
    }

    private void load() throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("toolchains/retromcp.properties"), StandardCharsets.UTF_8)) {
            config.load(reader);
        }
    }

    private String required(String key) {
        String value = config.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("missing toolchain property: " + key);
        }
        return value.trim();
    }

    private void requireCleanCheckout(Path checkout, String repository) throws Exception {
        if (!Files.isDirectory(checkout.resolve(".git"))) {
            throw new IllegalStateException("existing toolchain path is not a Git checkout: " + checkout);
        }
        String origin = capture(root, "git", "-C", checkout.toString(), "remote", "get-url", "origin").trim();
        if (!repository.equals(origin)) {
            throw new IllegalStateException("unexpected RetroMCP origin: " + origin);
        }
        String status = capture(root, "git", "-C", checkout.toString(), "status", "--porcelain").trim();
        if (!status.isEmpty()) {
            throw new IllegalStateException("refusing to replace a modified RetroMCP checkout:\n" + status);
        }
    }

    private void run(Path directory, String... command) throws Exception {
        String output = capture(directory, command);
        if (!output.trim().isEmpty()) {
            System.out.print(output);
        }
    }

    private String capture(Path directory, String... command) throws Exception {
        Process process;
        try {
            process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        } catch (IOException error) {
            throw new IllegalStateException("could not start " + command[0] + ": " + error.getMessage(), error);
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IllegalStateException(command[0] + " exited " + exit + "\n" + output);
        }
        return output;
    }
}
