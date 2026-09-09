import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** AWS Signature Version 4 header signer for bounded S3 object requests. */
final class S3SigV4 {
    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DAY = DateTimeFormatter
            .ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
    private S3SigV4() { }

    static Signed sign(String method, URI uri, String region, String accessKey, String secretKey,
            String sessionToken, Instant now, String payloadHash, Map<String, String> additional) {
        require(method.matches("GET|PUT"), "unsupported S3 signing method");
        require(uri.isAbsolute() && uri.getHost() != null && uri.getRawQuery() == null,
                "S3 signing URI must be an absolute query-free origin path");
        require(region.matches("[a-z0-9-]+") && !accessKey.isBlank() && !secretKey.isBlank(),
                "invalid S3 signing identity");
        require(payloadHash.matches("[0-9a-f]{64}"), "invalid S3 payload hash");
        String timestamp = TIMESTAMP.format(now), day = DAY.format(now);
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("host", host(uri));
        for (Map.Entry<String, String> entry : additional.entrySet()) {
            String name = entry.getKey().toLowerCase(java.util.Locale.ROOT);
            require(name.matches("[a-z0-9-]+") && !name.equals("authorization")
                            && !headers.containsKey(name), "invalid duplicate S3 signed header");
            headers.put(name, canonical(entry.getValue()));
        }
        headers.put("x-amz-content-sha256", payloadHash); headers.put("x-amz-date", timestamp);
        if (sessionToken != null && !sessionToken.isEmpty())
            headers.put("x-amz-security-token", canonical(sessionToken));
        StringBuilder canonicalHeaders = new StringBuilder(), signedNames = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (signedNames.length() > 0) signedNames.append(';');
            signedNames.append(entry.getKey());
            canonicalHeaders.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
        }
        String rawPath = uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String canonicalRequest = method + "\n" + rawPath + "\n\n" + canonicalHeaders + "\n"
                + signedNames + "\n" + payloadHash;
        String scope = day + "/" + region + "/s3/aws4_request";
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + scope + "\n"
                + sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8));
        byte[] key = hmac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), day);
        key = hmac(key, region); key = hmac(key, "s3"); key = hmac(key, "aws4_request");
        String signature = HexFormat.of().formatHex(hmac(key, stringToSign));
        String authorization = ALGORITHM + " Credential=" + accessKey + "/" + scope
                + ",SignedHeaders=" + signedNames + ",Signature=" + signature;
        TreeMap<String, String> transport = new TreeMap<>(headers); transport.remove("host");
        transport.put("authorization", authorization);
        return new Signed(Map.copyOf(transport), authorization, canonicalRequest);
    }

    static void selfTest() {
        Signed official = sign("GET", URI.create("https://examplebucket.s3.amazonaws.com/test.txt"),
                "us-east-1", "AKIAIOSFODNN7EXAMPLE",
                "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", "",
                Instant.parse("2013-05-24T00:00:00Z"), emptyHash(), Map.of("range", "bytes=0-9"));
        require(official.authorization().endsWith(
                "Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41"),
                "AWS S3 SigV4 official GET vector drifted");
        require(official.authorization().contains(
                "SignedHeaders=host;range;x-amz-content-sha256;x-amz-date"),
                "AWS S3 signed-header vector drifted");
    }

    static String emptyHash() { return sha256(new byte[0]); }
    private static String host(URI uri) {
        int port = uri.getPort(); boolean defaultPort = port < 0
                || "https".equals(uri.getScheme()) && port == 443
                || "http".equals(uri.getScheme()) && port == 80;
        return uri.getHost() + (defaultPort ? "" : ":" + port);
    }
    private static String canonical(String value) {
        require(value != null && !value.contains("\r") && !value.contains("\n"),
                "invalid S3 header value"); return value.trim().replaceAll("[ \\t]+", " ");
    }
    private static String sha256(byte[] value) {
        return HexFormat.of().formatHex(HexDigest.sha256().digest(value));
    }
    private static byte[] hmac(byte[] key, String value) {
        try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); }
        catch (java.security.GeneralSecurityException error) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", error);
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Signed(Map<String, String> headers, String authorization, String canonicalRequest) { }
}
