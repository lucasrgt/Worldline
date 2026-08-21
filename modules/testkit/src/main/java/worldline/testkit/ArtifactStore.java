package worldline.testkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded per-test artifact writer that cannot escape its configured root. */
final class ArtifactStore {
    private static final int MAX_NAME = 96;
    private final Path directory;
    private final List<Path> files = new ArrayList<>();

    ArtifactStore(Path root, String id) throws IOException {
        Path absolute = OutputGuard.safe(root, "artifact root");
        directory = absolute.resolve(safe(id)).normalize();
        require(directory.startsWith(absolute) && !directory.equals(absolute), "unsafe artifact directory");
        Files.createDirectories(directory);
    }

    Path directory() { return directory; }
    synchronized List<Path> files() { return Collections.unmodifiableList(new ArrayList<>(files)); }

    synchronized Path write(String name, byte[] bytes) throws IOException {
        if (bytes == null) throw new NullPointerException("artifact bytes");
        require(bytes.length <= 8_388_608, "artifact exceeds 8 MiB");
        require(name != null && name.length() > 0 && name.length() <= MAX_NAME
                && name.matches("[A-Za-z0-9][A-Za-z0-9._-]*"), "invalid artifact name");
        Path target = directory.resolve(name).normalize();
        require(target.getParent().equals(directory), "artifact path escaped test directory");
        Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        if (!files.contains(target)) files.add(target); return target;
    }

    static String safe(String value) {
        String clean = value.replaceAll("[^A-Za-z0-9._-]+", "-").replaceAll("^-+|-+$", "");
        if (clean.isEmpty()) clean = "test";
        return clean.length() <= MAX_NAME ? clean : clean.substring(0, MAX_NAME);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
