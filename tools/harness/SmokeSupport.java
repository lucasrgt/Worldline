import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Shared fail-closed primitives for source-launched smoke coordinators. */
public final class SmokeSupport {
    private SmokeSupport() {}

    public static String capture(Path directory, List<String> command) throws Exception {
        int timeout = environmentInteger("WORLDLINE_SMOKE_CHILD_TIMEOUT_SECONDS", 300, 1, 3600);
        Path log = Files.createTempFile("worldline-smoke-child-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                destroy(process);
                throw new IllegalStateException(command.get(0) + " timed out after " + timeout + "s");
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            if (process.exitValue() != 0)
                throw new IllegalStateException(command.get(0) + " failed\n" + output);
            return output;
        } finally { Files.deleteIfExists(log); }
    }

    public static void recreate(Path root, Path target) throws IOException {
        Path privateRoot = root.resolve(".worldline").toAbsolutePath().normalize();
        Path normalized = target.toAbsolutePath().normalize();
        require(normalized.startsWith(privateRoot) && !normalized.equals(privateRoot),
                "unsafe build path: " + target);
        if (Files.exists(normalized)) try (Stream<Path> paths = Files.walk(normalized)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                Files.deleteIfExists(path);
        }
        Files.createDirectories(normalized);
    }

    public static void verifyArtifact(Path path, Properties artifact) throws Exception {
        require(Files.isRegularFile(path), "official artifact absent: " + path);
        require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes")),
                "official artifact size drift: " + path);
        require(digest(path, "SHA-1").equals(value(artifact, "expected.sha1")),
                "official artifact SHA-1 drift: " + path);
        require(digest(path, "SHA-256").equals(value(artifact, "expected.sha256")),
                "official artifact SHA-256 drift: " + path);
    }

    public static List<String> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .map(Path::toString).collect(Collectors.toList());
        }
    }

    public static void load(Path path, Properties target) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }

    public static String value(Properties properties, String key) {
        String value = properties.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }

    public static String digest(Path path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    public static Path product(Path root, String module) {
        return root.resolve(".worldline/build/classes").resolve(module);
    }

    public static String line(String output, String prefix) {
        return output.lines().filter(value -> value.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing " + prefix + "\n" + output))
                .substring(prefix.length());
    }

    public static boolean eof(Exception error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof EOFException) return true;
            String message = cause.getMessage();
            if (message != null && message.toUpperCase(Locale.ROOT).contains("EOF")) return true;
        }
        return false;
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static int environmentInteger(String name, int fallback, int minimum, int maximum) {
        String raw = System.getenv(name); int value = fallback;
        if (raw != null && !raw.isBlank()) try { value = Integer.parseInt(raw); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
        require(value >= minimum && value <= maximum,
                name + " must be between " + minimum + " and " + maximum);
        return value;
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }
}
