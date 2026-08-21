package worldline.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.List;
import java.util.stream.Collectors;

/** Bounded latest-write fingerprint for watch-mode JARs and class directories. */
final class TestSourceFingerprint {
    private TestSourceFingerprint() {}
    static long read(Path source) throws IOException {
        Path real = source.toRealPath();
        if (Files.isRegularFile(real)) return Files.getLastModifiedTime(real).toMillis();
        try (Stream<Path> paths = Files.walk(real)) {
            List<Path> files = paths.filter(Files::isRegularFile).limit(10_001)
                    .sorted().collect(Collectors.toList());
            if (files.size() > 10_000) throw new IllegalArgumentException(
                    "watch source contains more than 10000 files");
            long fingerprint = Files.getLastModifiedTime(real).toMillis();
            for (Path path : files) fingerprint = 31 * fingerprint + modified(path)
                    + Files.size(path) + path.toString().hashCode();
            return fingerprint;
        }
    }
    private static long modified(Path path) {
        try { return Files.getLastModifiedTime(path).toMillis(); }
        catch (IOException error) { throw new IllegalStateException("watch fingerprint failed", error); }
    }
}
