import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;

/** Bounded authenticated transport for immutable S3-compatible objects. */
final class S3ObjectTransport {
    private static final long MAXIMUM = 8L * 1024L * 1024L;
    private final ModuleCacheS3Config config;
    private final HttpClient client;
    private final Clock clock;
    private final Path downloads;

    S3ObjectTransport(ModuleCacheS3Config config, Clock clock, Path downloads) throws Exception {
        this.config = config; this.clock = clock; this.downloads = absolute(downloads);
        Files.createDirectories(this.downloads);
        require(Files.isDirectory(this.downloads, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(this.downloads), "S3 download directory is linked");
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }

    boolean get(String digest, long size, Path target) throws Exception {
        require(size > 0L && size <= MAXIMUM, "invalid S3 object size");
        require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS),
                "S3 download target already exists");
        HttpRequest request = request("GET", digest, S3SigV4.emptyHash(), Map.of(), null);
        HttpResponse<InputStream> response = client.send(request,
                HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            if (response.statusCode() == 404) return false;
            require(response.statusCode() == 200, "S3 GET returned status " + response.statusCode());
            require(header(response, "x-amz-meta-worldline-sha256").equals(digest)
                    && header(response, "x-amz-meta-worldline-size").equals(Long.toString(size)),
                    "S3 object metadata drifted");
            copyExact(body, target, size); verify(target, digest, size); return true;
        } catch (Exception error) {
            Files.deleteIfExists(target); throw error;
        }
    }

    void put(ArtifactTransportPolicy.Candidate candidate) throws Exception {
        verify(candidate.path(), candidate.sha256(), candidate.size());
        Map<String, String> metadata = Map.of(
                "if-none-match", "*",
                "x-amz-meta-worldline-sha256", candidate.sha256(),
                "x-amz-meta-worldline-size", Long.toString(candidate.size()));
        int status = 409;
        for (int attempt = 0; attempt < 2 && status == 409; attempt++) {
            HttpRequest request = request("PUT", candidate.sha256(), candidate.sha256(), metadata,
                    HttpRequest.BodyPublishers.ofFile(candidate.path()));
            HttpResponse<InputStream> response = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());
            status = response.statusCode(); try (InputStream responseBody = response.body()) {
                responseBody.readNBytes(1);
            }
        }
        require(status == 200 || status == 412, "S3 conditional PUT returned status " + status);
        Path proof = temporary("publish-proof");
        try { require(get(candidate.sha256(), candidate.size(), proof),
                    "published S3 object disappeared"); }
        finally { Files.deleteIfExists(proof); }
    }

    private HttpRequest request(String method, String digest, String payloadHash,
            Map<String, String> additional, HttpRequest.BodyPublisher body) {
        S3SigV4.Signed signed = S3SigV4.sign(method, config.objectUri(digest), config.region(),
                config.accessKey(), config.secretKey(), config.sessionToken(), clock.instant(),
                payloadHash, additional);
        HttpRequest.Builder builder = HttpRequest.newBuilder(config.objectUri(digest))
                .timeout(Duration.ofSeconds(30));
        signed.headers().forEach(builder::header);
        return body == null ? builder.GET().build() : builder.PUT(body).build();
    }

    Path temporary(String label) {
        return downloads.resolve(label + "-" + ProcessHandle.current().pid() + "-"
                + System.nanoTime() + ".object");
    }

    private static void copyExact(InputStream input, Path target, long size) throws Exception {
        try (java.io.OutputStream output = Files.newOutputStream(target,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[16 * 1024]; long total = 0L; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) {
                total += count; require(total <= size, "S3 object exceeded declared size");
                output.write(buffer, 0, count);
            }
            require(total == size, "S3 object was truncated");
        }
    }

    static void verify(Path path, String digest, long size) throws Exception {
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)
                && Files.size(path) == size && sha256(path).equals(digest),
                "S3 object identity drifted");
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = HexDigest.sha256();
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static String header(HttpResponse<?> response, String name) {
        return response.headers().firstValue(name).orElse("");
    }
    private static Path absolute(Path value) {
        require(value.isAbsolute() && value.normalize().equals(value),
                "S3 download directory is not absolute and normalized"); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
