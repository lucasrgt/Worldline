import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Properties;

/** Acquires hash-pinned proprietary artifacts into the ignored local root. */
public final class Acquire {
    private final Path root = Paths.get("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        if (arguments.length != 1 || !Arrays.asList("client", "server", "all").contains(arguments[0])) {
            System.err.println("usage: java tools/artifacts/Acquire.java client|server|all");
            System.exit(2);
        }
        try { new Acquire().execute(arguments[0]); }
        catch (Exception error) {
            System.err.println("artifact acquisition failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String selection) throws Exception {
        if (!selection.equals("server")) acquire("client");
        if (!selection.equals("client")) acquire("server");
    }

    private void acquire(String name) throws Exception {
        Properties descriptor = load("artifacts/minecraft-b1.7.3-" + name + ".properties");
        Path destination = localPath(descriptor);
        if (Files.isRegularFile(destination) && valid(descriptor, destination)) {
            ready(name, destination, "already present");
            return;
        }
        URI source = URI.create(required(descriptor, "source.url"));
        if (!"https".equalsIgnoreCase(source.getScheme())) {
            throw new IllegalStateException("artifact source must use HTTPS");
        }
        Files.createDirectories(destination.getParent());
        Path partial = destination.resolveSibling(destination.getFileName() + ".part");
        Files.deleteIfExists(partial);
        HttpRequest request = HttpRequest.newBuilder(source).GET().build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(partial));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(partial);
            throw new IllegalStateException("artifact server returned HTTP " + response.statusCode());
        }
        if (!valid(descriptor, partial)) {
            Files.deleteIfExists(partial);
            throw new IllegalStateException("downloaded " + name + " artifact failed frozen hashes");
        }
        Files.move(partial, destination, StandardCopyOption.REPLACE_EXISTING);
        ready(name, destination, "downloaded and verified");
    }

    private boolean valid(Properties descriptor, Path path) throws Exception {
        return Files.size(path) == Long.parseLong(required(descriptor, "expected.bytes"))
                && digest(path, "SHA-1").equals(required(descriptor, "expected.sha1"))
                && digest(path, "SHA-256").equals(required(descriptor, "expected.sha256"));
    }

    private Path localPath(Properties descriptor) {
        Path local = root.resolve("local").normalize();
        Path result = root.resolve(required(descriptor, "local.path")).normalize();
        if (!result.startsWith(local) || result.equals(local)) {
            throw new IllegalStateException("artifact destination must be inside local/");
        }
        return result;
    }

    private Properties load(String relative) throws IOException {
        Properties result = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(root.resolve(relative), StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private String required(Properties descriptor, String key) {
        String result = descriptor.getProperty(key);
        if (result == null || result.trim().isEmpty()) {
            throw new IllegalStateException("missing descriptor property: " + key);
        }
        return result.trim();
    }

    private String digest(Path path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void ready(String name, Path path, String state) {
        System.out.println("b1.7.3 " + name + " artifact " + state);
        System.out.println("  path: " + root.relativize(path));
    }
}
