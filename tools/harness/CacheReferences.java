import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Snapshots live worktree filesystem links into the shared cache. */
final class CacheReferences {
    private CacheReferences() { }

    static Set<Path> snapshot(Path repository, Path cache) throws Exception {
        if (!Files.isDirectory(cache)) return Set.of();
        Path realCache = cache.toRealPath(); Set<Path> result = new HashSet<>();
        String listing = ProcessCapture.require(repository,
                List.of("git", "worktree", "list", "--porcelain"), 60);
        for (String line : listing.lines().filter(value -> value.startsWith("worktree ")).toList()) {
            Path privateRoot = Path.of(line.substring(9)).resolve(".worldline");
            if (!Files.isDirectory(privateRoot)) continue;
            for (Path candidate : SafeTreeDelete.paths(privateRoot, 4)) {
                if (!SafeTreeDelete.linkLike(candidate)) continue;
                Path target = candidate.toRealPath();
                if (target.startsWith(realCache)) result.add(target);
            }
        }
        return Set.copyOf(result);
    }

    static boolean protects(Path entry, Set<Path> references) throws Exception {
        Path real = entry.toRealPath();
        return references.stream().anyMatch(target -> target.startsWith(real));
    }

    @FunctionalInterface
    interface Source { Set<Path> snapshot() throws Exception; }
}
