package worldline.symbolgraph;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class MappingArchive {
    private MappingArchive() {}

    public static TinyMapping read(Path archive, String entryName) throws IOException {
        if (archive == null || entryName == null || entryName.isEmpty()) {
            throw new IllegalArgumentException("archive and entry are required");
        }
        try (JarFile jar = new JarFile(archive.toFile())) {
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry == null || entry.isDirectory()) {
                throw new IllegalArgumentException("missing mapping entry: " + entryName);
            }
            try (InputStreamReader reader = new InputStreamReader(
                    jar.getInputStream(entry), StandardCharsets.UTF_8)) {
                return new TinyV2Reader().read(reader);
            }
        }
    }
}
