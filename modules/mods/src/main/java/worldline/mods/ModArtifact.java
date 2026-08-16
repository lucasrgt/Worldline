package worldline.mods;

import java.nio.file.Path;

/** Inspected mod package, including provenance and compatibility. */
public final class ModArtifact {
    private final Path path;
    private final ModDescriptor descriptor;
    private final String sha256;
    private final ModCompatibility compatibility;

    ModArtifact(Path path, ModDescriptor descriptor, String sha256,
            ModCompatibility compatibility) {
        this.path = path;
        this.descriptor = descriptor;
        this.sha256 = sha256;
        this.compatibility = compatibility;
    }

    public Path path() { return path; }
    public ModDescriptor descriptor() { return descriptor; }
    public String sha256() { return sha256; }
    public ModCompatibility compatibility() { return compatibility; }
    public boolean compatible() { return compatibility == ModCompatibility.COMPATIBLE; }
}
