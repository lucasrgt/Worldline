import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Authenticated bounded in-process S3 protocol fixture. */
final class ModuleCacheS3Fixture implements AutoCloseable {
    private static final long MAXIMUM = 8L * 1024L * 1024L;
    private final String region, accessKey, secretKey;
    private final Map<String, Stored> objects = new HashMap<>();
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    ModuleCacheS3Fixture(String region, String accessKey, String secretKey) throws Exception {
        this.region = region; this.accessKey = accessKey; this.secretKey = secretKey;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle); server.setExecutor(executor); server.start();
    }

    Map<String, String> environment() {
        Map<String, String> values = new HashMap<>();
        values.put("WORLDLINE_MODULE_S3_ENDPOINT", "http://127.0.0.1:" + server.getAddress().getPort());
        values.put("WORLDLINE_MODULE_S3_ALLOW_LOOPBACK_HTTP", "1");
        values.put("WORLDLINE_MODULE_S3_BUCKET", "worldline-cache");
        values.put("WORLDLINE_MODULE_S3_PREFIX", "modules/v1");
        values.put("WORLDLINE_MODULE_S3_REGION", region);
        values.put("WORLDLINE_MODULE_S3_ACCESS_KEY", accessKey);
        values.put("WORLDLINE_MODULE_S3_SECRET_KEY", secretKey); return values;
    }

    void corrupt(String digest) {
        String suffix = "/" + digest.substring(0, 2) + "/" + digest.substring(2);
        synchronized (objects) {
            String key = objects.keySet().stream().filter(value -> value.endsWith(suffix))
                    .findFirst().orElseThrow(); Stored stored = objects.get(key);
            byte[] bytes = stored.bytes().clone(); bytes[0] ^= 1;
            objects.put(key, new Stored(bytes, stored.digest(), stored.size()));
        }
    }

    int objectCount() { synchronized (objects) { return objects.size(); } }

    private void handle(HttpExchange exchange) {
        try {
            require(authenticated(exchange), "request authentication failed");
            if ("PUT".equals(exchange.getRequestMethod())) put(exchange);
            else if ("GET".equals(exchange.getRequestMethod())) get(exchange);
            else reply(exchange, 405, new byte[0]);
        } catch (Exception error) {
            try { reply(exchange, 403, new byte[0]); } catch (Exception ignored) { }
        } finally { exchange.close(); }
    }

    private boolean authenticated(HttpExchange exchange) throws Exception {
        String authorization = header(exchange, "Authorization");
        String timestamp = header(exchange, "x-amz-date");
        String payload = header(exchange, "x-amz-content-sha256");
        if (authorization.isEmpty() || timestamp.isEmpty() || payload.isEmpty()) return false;
        String marker = "SignedHeaders=", end = ",Signature=";
        int start = authorization.indexOf(marker), finish = authorization.indexOf(end);
        if (start < 0 || finish <= start) return false;
        String names = authorization.substring(start + marker.length(), finish);
        Map<String, String> additional = new HashMap<>();
        for (String name : names.split(";")) if (!List.of("host", "x-amz-content-sha256",
                "x-amz-date", "x-amz-security-token").contains(name))
            additional.put(name, header(exchange, name));
        URI request = new URI("http", null, "127.0.0.1", server.getAddress().getPort(),
                exchange.getRequestURI().getRawPath(), null, null);
        Instant now = Instant.from(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC).parse(timestamp));
        S3SigV4.Signed expected = S3SigV4.sign(exchange.getRequestMethod(), request, region,
                accessKey, secretKey, "", now, payload, additional);
        return expected.authorization().equals(authorization);
    }

    private void put(HttpExchange exchange) throws Exception {
        require("*".equals(header(exchange, "If-None-Match")), "conditional create is absent");
        String digest = header(exchange, "x-amz-meta-worldline-sha256");
        long size = Long.parseLong(header(exchange, "x-amz-meta-worldline-size"));
        byte[] bytes = bounded(exchange.getRequestBody(), size);
        require(digest.matches("[0-9a-f]{64}") && size > 0L && size <= MAXIMUM
                && Arrays.equals(HexDigest.sha256().digest(bytes), java.util.HexFormat.of().parseHex(digest))
                && digest.equals(header(exchange, "x-amz-content-sha256")),
                "PUT object identity drifted");
        synchronized (objects) {
            if (objects.containsKey(exchange.getRequestURI().getRawPath())) {
                reply(exchange, 412, new byte[0]); return;
            }
            objects.put(exchange.getRequestURI().getRawPath(), new Stored(bytes, digest, size));
        }
        reply(exchange, 200, new byte[0]);
    }

    private void get(HttpExchange exchange) throws Exception {
        Stored stored; synchronized (objects) {
            stored = objects.get(exchange.getRequestURI().getRawPath());
        }
        if (stored == null) { reply(exchange, 404, new byte[0]); return; }
        exchange.getResponseHeaders().set("x-amz-meta-worldline-sha256", stored.digest());
        exchange.getResponseHeaders().set("x-amz-meta-worldline-size", Long.toString(stored.size()));
        reply(exchange, 200, stored.bytes());
    }

    private static byte[] bounded(InputStream input, long expected) throws Exception {
        require(expected > 0L && expected <= MAXIMUM, "invalid fixture body size");
        byte[] bytes = input.readNBytes((int) expected + 1);
        require(bytes.length == expected, "fixture body length drifted"); return bytes;
    }
    private static String header(HttpExchange exchange, String name) {
        return exchange.getRequestHeaders().getFirst(name) == null ? ""
                : exchange.getRequestHeaders().getFirst(name);
    }
    private static void reply(HttpExchange exchange, int status, byte[] body) throws Exception {
        exchange.sendResponseHeaders(status, body.length); exchange.getResponseBody().write(body);
    }
    @Override public void close() {
        server.stop(0); executor.shutdownNow();
        try { require(executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                    "S3 fixture executor did not terminate"); }
        catch (InterruptedException error) { Thread.currentThread().interrupt();
            throw new IllegalStateException("S3 fixture shutdown interrupted", error); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Stored(byte[] bytes, String digest, long size) { }
}
