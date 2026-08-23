import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Loads the versioned working-tree inputs used by portable smoke fingerprints. */
final class SmokeTrackedFiles {
    private SmokeTrackedFiles() { }

    static Set<Path> read(Path root) throws IOException {
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
}
