import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Downloads public mapping artifacts into ignored local/mappings with exact pins. */
public final class AcquireMappings {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path destination = root.resolve("local/mappings").normalize();

    public static void main(String[] arguments) {
        if (arguments.length == 0) {
            System.err.println("usage: java tools/mappings/AcquireMappings.java <source.properties>...");
            System.exit(2);
        }
        try {
            AcquireMappings tool = new AcquireMappings();
            for (String argument : arguments) tool.acquire(Paths.get(argument));
        } catch (Exception error) {
            System.err.println("mapping acquisition failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void acquire(Path descriptor) throws Exception {
        Properties source = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(descriptor)) { source.load(reader); }
        URI uri = URI.create(required(source, "url"));
        require("https".equalsIgnoreCase(uri.getScheme()), "mapping URL must use HTTPS");
        String outputName = required(source, "output");
        require(Paths.get(outputName).getNameCount() == 1, "output must be a file name");
        Path output = destination.resolve(outputName).normalize();
        require(output.startsWith(destination) && !output.equals(destination), "unsafe output path");
        long bytes = Long.parseLong(required(source, "expected.bytes"));
        String sha256 = required(source, "expected.sha256");
        require(bytes > 0 && sha256.matches("[0-9a-f]{64}"), "invalid artifact pin");
        Files.createDirectories(destination);
        if (Files.isRegularFile(output) && verify(output, bytes, sha256)) {
            System.out.println(required(source, "id") + " already verified: " + output);
            return;
        }
        Path temporary = Files.createTempFile(destination, outputName + ".", ".part");
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(60_000);
            connection.setInstanceFollowRedirects(false);
            int status = connection.getResponseCode();
            require(status == 200, "download returned HTTP " + status);
            try (InputStream input = connection.getInputStream();
                    OutputStream sink = Files.newOutputStream(temporary)) {
                input.transferTo(sink);
            } finally {
                connection.disconnect();
            }
            require(verify(temporary, bytes, sha256), "downloaded artifact did not match pin");
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(required(source, "id") + " acquired: " + output);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static boolean verify(Path path, long bytes, String sha256) throws Exception {
        if (Files.size(path) != bytes) return false;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        return sha256.equals(HexFormat.of().formatHex(digest.digest()));
    }

    private static String required(Properties source, String key) {
        String value = source.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
