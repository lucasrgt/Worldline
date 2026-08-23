import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Loads the versioned working-tree inputs used by portable smoke fingerprints. */
final class SmokeTrackedFiles {
    private SmokeTrackedFiles() { }

    static Set<Path> read(Path root) throws IOException {
        String manifest = System.getenv("WORLDLINE_TRACKED_FILES");
        if (manifest != null && !manifest.isBlank()) return manifest(root, Path.of(manifest));
        Process process = new ProcessBuilder("git", "ls-files", "-z")
                .directory(root.toFile()).redirectErrorStream(true).start();
        byte[] output = process.getInputStream().readAllBytes();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0)
                throw new IOException("git ls-files failed: "
                        + new String(output, StandardCharsets.UTF_8));
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt(); throw new IOException("git ls-files interrupted", error);
        }
        Set<Path> result = new HashSet<>();
        for (String value : new String(output, StandardCharsets.UTF_8).split("\\x00"))
            if (!value.isEmpty()) result.add(root.resolve(value).toAbsolutePath().normalize());
        return Set.copyOf(result);
    }

    private static Set<Path> manifest(Path root, Path requested) throws IOException {
        require("1".equals(System.getenv("WORLDLINE_CONTAINER_ISOLATED")),
                "tracked-file manifest is reserved for isolated containers");
        Path directory = root.resolve(".worldline/runtime-fabric").normalize();
        Path file = requested.toAbsolutePath().normalize();
        require(file.startsWith(directory) && Files.isRegularFile(file),
                "invalid container tracked-file manifest");
        Set<Path> result = new HashSet<>();
        for (String value : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            require(value.matches("[A-Za-z0-9._/+-]+") && !value.startsWith("/")
                    && !value.contains("../"), "unsafe tracked path: " + value);
            Path path = root.resolve(value).toAbsolutePath().normalize();
            require(path.startsWith(root), "tracked path escaped container root");
            result.add(path);
        }
        require(!result.isEmpty(), "empty container tracked-file manifest");
        return Set.copyOf(result);
    }

    private static void require(boolean value, String message) throws IOException {
        if (!value) throw new IOException(message);
    }
}
