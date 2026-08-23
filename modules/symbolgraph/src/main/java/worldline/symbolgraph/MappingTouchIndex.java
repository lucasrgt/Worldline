package worldline.symbolgraph;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/** Exact token inventory used only to prioritize behavior-touched mapping identities. */
public final class MappingTouchIndex {
    private MappingTouchIndex() {}

    public static Set<String> read(Path root) throws Exception {
        if (root == null || !Files.isDirectory(root))
            throw new IllegalArgumentException("mapping touch root is not a directory");
        TreeSet<String> tokens = new TreeSet<String>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)
                    .filter(MappingTouchIndex::maintained).sorted()::iterator) {
                String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                for (String token : text.split("[^A-Za-z0-9_$/<>.-]+"))
                    if (!token.isEmpty()) tokens.add(token);
            }
        }
        return Collections.unmodifiableSet(tokens);
    }

    private static boolean maintained(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".java") || name.endsWith(".properties") || name.endsWith(".md");
    }
}
