import java.net.URI;
import java.util.Map;

/** Closed S3-compatible endpoint and credential configuration. */
final class ModuleCacheS3Config {
    private final URI endpoint;
    private final String bucket, prefix, region, accessKey, secretKey, sessionToken;

    private ModuleCacheS3Config(URI endpoint, String bucket, String prefix, String region,
            String accessKey, String secretKey, String sessionToken) {
        this.endpoint = endpoint; this.bucket = bucket; this.prefix = prefix;
        this.region = region; this.accessKey = accessKey; this.secretKey = secretKey;
        this.sessionToken = sessionToken;
    }

    static ModuleCacheS3Config from(Map<String, String> environment) {
        URI endpoint;
        try { endpoint = URI.create(required(environment, "WORLDLINE_MODULE_S3_ENDPOINT")); }
        catch (IllegalArgumentException error) { throw new IllegalStateException(
                "invalid WORLDLINE_MODULE_S3_ENDPOINT", error); }
        require(endpoint.isAbsolute() && endpoint.getHost() != null
                        && endpoint.getRawQuery() == null && endpoint.getRawFragment() == null
                        && endpoint.getUserInfo() == null,
                "S3 endpoint must be an absolute origin without credentials or query");
        boolean loopback = endpoint.getHost().equals("127.0.0.1")
                || endpoint.getHost().equals("localhost") || endpoint.getHost().equals("::1");
        boolean testHttp = "1".equals(environment.get("WORLDLINE_MODULE_S3_ALLOW_LOOPBACK_HTTP"));
        require("https".equals(endpoint.getScheme())
                        || "http".equals(endpoint.getScheme()) && loopback && testHttp,
                "S3 endpoint requires HTTPS outside an explicit loopback fixture");
        String rawPath = endpoint.getRawPath();
        require(rawPath == null || !rawPath.contains("..") && !rawPath.contains("//"),
                "S3 endpoint base path is not normalized");
        String bucket = required(environment, "WORLDLINE_MODULE_S3_BUCKET");
        require(bucket.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]")
                        && !bucket.contains(".."), "invalid S3 bucket");
        String prefix = environment.getOrDefault("WORLDLINE_MODULE_S3_PREFIX", "");
        require(prefix.isEmpty() || prefix.matches("[a-z0-9][a-z0-9/_-]{0,254}")
                        && !prefix.contains("//") && !prefix.endsWith("/"),
                "invalid S3 object prefix");
        String region = required(environment, "WORLDLINE_MODULE_S3_REGION");
        require(region.matches("[a-z0-9][a-z0-9-]{1,30}[a-z0-9]"), "invalid S3 region");
        String access = required(environment, "WORLDLINE_MODULE_S3_ACCESS_KEY");
        require(access.matches("[A-Za-z0-9/+_=,.@-]{3,128}"), "invalid S3 access key");
        String secret = required(environment, "WORLDLINE_MODULE_S3_SECRET_KEY");
        require(secret.length() <= 256 && secret.chars().allMatch(value -> value >= 33 && value <= 126),
                "invalid S3 secret key");
        String token = environment.getOrDefault("WORLDLINE_MODULE_S3_SESSION_TOKEN", "");
        require(token.length() <= 2048 && token.chars().allMatch(value -> value >= 33 && value <= 126),
                "invalid S3 session token");
        return new ModuleCacheS3Config(endpoint, bucket, prefix, region, access, secret, token);
    }

    URI objectUri(String digest) {
        require(digest.matches("[0-9a-f]{64}"), "invalid S3 object digest");
        String base = endpoint.getRawPath() == null ? "" : endpoint.getRawPath();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String namespace = prefix.isEmpty() ? "" : "/" + prefix;
        String path = base + "/" + bucket + namespace + "/objects/sha256/"
                + digest.substring(0, 2) + "/" + digest.substring(2);
        try { return new URI(endpoint.getScheme(), null, endpoint.getHost(), endpoint.getPort(),
                path, null, null); }
        catch (java.net.URISyntaxException error) {
            throw new IllegalStateException("S3 object URI construction failed", error);
        }
    }

    String region() { return region; }
    String accessKey() { return accessKey; }
    String secretKey() { return secretKey; }
    String sessionToken() { return sessionToken; }

    static void selfTest() {
        Map<String, String> values = new java.util.HashMap<>();
        values.put("WORLDLINE_MODULE_S3_ENDPOINT", "http://127.0.0.1:9000/base");
        values.put("WORLDLINE_MODULE_S3_ALLOW_LOOPBACK_HTTP", "1");
        values.put("WORLDLINE_MODULE_S3_BUCKET", "worldline-cache");
        values.put("WORLDLINE_MODULE_S3_PREFIX", "modules/v1");
        values.put("WORLDLINE_MODULE_S3_REGION", "us-east-1");
        values.put("WORLDLINE_MODULE_S3_ACCESS_KEY", "TESTACCESS");
        values.put("WORLDLINE_MODULE_S3_SECRET_KEY", "test-secret");
        ModuleCacheS3Config config = from(values); String digest = "a".repeat(64);
        require(config.objectUri(digest).toString().equals("http://127.0.0.1:9000/base/"
                + "worldline-cache/modules/v1/objects/sha256/aa/" + "a".repeat(62)),
                "S3 object layout drifted");
        values.remove("WORLDLINE_MODULE_S3_ALLOW_LOOPBACK_HTTP");
        rejected(() -> from(values), "unapproved HTTP S3 endpoint was accepted");
    }

    private static String required(Map<String, String> environment, String key) {
        String value = environment.get(key);
        require(value != null && !value.isBlank(), key + " is required in S3 mode"); return value;
    }
    private static void rejected(Action action, String message) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    @FunctionalInterface private interface Action { void run(); }
}
