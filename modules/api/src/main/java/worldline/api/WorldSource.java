package worldline.api;

import java.nio.file.Path;
import java.util.Objects;

/** Location of a caller-owned local world used by a controlled scenario. */
public final class WorldSource {
    private final Path path;

    private WorldSource(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public static WorldSource at(Path path) {
        return new WorldSource(path);
    }

    public Path path() {
        return path;
    }
}
