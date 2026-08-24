import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Snapshots live worktree filesystem links into the shared cache. */
final class CacheReferences {
    private CacheReferences() { }

    static Set<Path> snapshot(Path repository, Path cache) throws Exception {
        return inspect(repository, cache).paths;
    }

    static Snapshot inspect(Path repository, Path cache) throws Exception {
        if (!Files.isDirectory(cache)) return new Snapshot(Set.of(), 0);
        Path realCache = cache.toRealPath(); Set<Path> result = new HashSet<>(); int dangling = 0;
        String listing = ProcessCapture.require(repository,
                List.of("git", "worktree", "list", "--porcelain"), 60);
        for (String line : listing.lines().filter(value -> value.startsWith("worktree ")).toList()) {
            Path privateRoot = Path.of(line.substring(9)).resolve(".worldline");
            if (!Files.isDirectory(privateRoot)) continue;
            for (Path candidate : SafeTreeDelete.paths(privateRoot, 4)) {
                if (!SafeTreeDelete.linkLike(candidate)) continue;
                Path target;
                try { target = candidate.toRealPath(); }
                catch (NoSuchFileException missing) { dangling++; continue; }
                if (target.startsWith(realCache)) result.add(target);
            }
        }
        return new Snapshot(Set.copyOf(result), dangling);
    }

    static boolean protects(Path entry, Set<Path> references) throws Exception {
        Path real = entry.toRealPath();
        return references.stream().anyMatch(target -> target.startsWith(real));
    }

    @FunctionalInterface
    interface Source { Set<Path> snapshot() throws Exception; }

    record Snapshot(Set<Path> paths, int dangling) { }
}
