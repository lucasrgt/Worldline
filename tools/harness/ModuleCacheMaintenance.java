import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/** Reports and bounds the cross-worktree immutable module cache without breaking live links. */
public final class ModuleCacheMaintenance {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path cache = cacheRoot().resolve("modules");

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1 && List.of("doctor", "gc", "--self-test")
                    .contains(arguments[0]), "usage: ModuleCacheMaintenance doctor|gc|--self-test");
            if (arguments[0].equals("--self-test")) selfTest();
            else new ModuleCacheMaintenance().execute(arguments[0].equals("gc"));
        } catch (Exception error) {
            System.err.println("module cache maintenance failed: " + error.getMessage());
            System.exit(1);
        }
    }

    void execute(boolean delete) throws Exception {
        Files.createDirectories(cache);
        CachePolicy policy = new CachePolicy(root);
        CacheReferences.Source references = () -> CacheReferences.snapshot(root, cache);
        Result result = maintain(cache, references, policy.maximumBytes(),
                policy.minimumAgeMillis(), System.currentTimeMillis(), delete);
        System.out.println("module-cache.entries=" + result.entries + ";bytes=" + result.bytes
                + ";referenced=" + result.referenced + ";removed=" + result.removed);
        System.out.println("WORLDLINE_MODULE_CACHE_" + (delete ? "GC" : "DOCTOR") + "=PASS");
    }

    static Result maintain(Path cache, Set<Path> references, long maximum, long minimumAge,
            long now, boolean delete) throws Exception {
        return maintain(cache, () -> references, maximum, minimumAge, now, delete);
    }

    static Result maintain(Path cache, CacheReferences.Source source, long maximum, long minimumAge,
            long now, boolean delete) throws Exception {
        require(maximum > 0L, "module cache maximum must be positive");
        Set<Path> references = source.snapshot();
        List<Entry> entries = entries(cache); long total = entries.stream().mapToLong(Entry::bytes).sum();
        int referenced = 0, removed = 0;
        for (Entry entry : entries) {
            if (CacheReferences.protects(entry.path, references)) { referenced++; continue; }
            if (!delete || now - entry.used < minimumAge) continue;
            Path lockPath = entry.path.resolveSibling(entry.digest + ".lock");
            Files.createDirectories(lockPath.getParent());
            try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE); FileLock lock = tryLock(channel)) {
                if (lock == null) continue;
                Set<Path> current = source.snapshot();
                if (CacheReferences.protects(entry.path, current)) { referenced++; continue; }
                SafeTreeDelete.delete(entry.path); Files.deleteIfExists(entry.usedPath);
                total -= entry.bytes; removed++;
            }
        }
        return new Result(entries.size(), total, referenced, removed);
    }

    private static List<Entry> entries(Path cache) throws Exception {
        List<Entry> result = new ArrayList<>();
        if (!Files.isDirectory(cache)) return result;
        try (Stream<Path> modules = Files.list(cache)) {
            for (Path module : modules.filter(Files::isDirectory).toList()) {
                require(!SafeTreeDelete.linkLike(module), "module cache contains a filesystem link: " + module);
                    try (Stream<Path> paths = Files.list(module)) {
                        for (Path path : paths.filter(Files::isDirectory).toList()) {
                    require(!SafeTreeDelete.linkLike(path),
                            "module cache contains a filesystem link: " + path);
                    String digest = path.getFileName().toString();
                    if (!digest.matches("[0-9a-f]{64}")) continue;
                    Path complete = path.resolve(".complete");
                    require(Files.isRegularFile(complete)
                            && Files.readString(complete).trim().equals(digest),
                            "corrupt immutable module cache entry " + path);
                    Path used = path.resolveSibling(digest + ".used");
                    long timestamp = Files.isRegularFile(used) ? parseUsed(used)
                            : Files.getLastModifiedTime(complete).toMillis();
                    result.add(new Entry(path, used, digest, timestamp, size(path)));
                }
            }
            }
        }
        result.sort(Comparator.comparingLong(Entry::used)); return result;
    }

    private static long size(Path root) throws IOException {
        return SafeTreeDelete.size(root);
    }

    private static long parseUsed(Path path) throws IOException {
        try { return Long.parseLong(Files.readString(path, StandardCharsets.UTF_8).trim()); }
        catch (NumberFormatException error) { throw new IOException("invalid module usage marker " + path, error); }
    }

    private static FileLock tryLock(FileChannel channel) throws IOException {
        try { return channel.tryLock(); }
        catch (java.nio.channels.OverlappingFileLockException ignored) { return null; }
    }

    static Path cacheRoot() {
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        if (control != null && !control.isBlank())
            return Path.of(control).toAbsolutePath().normalize().resolve("cache");
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        String base = windows ? System.getenv("LOCALAPPDATA") : System.getenv("XDG_RUNTIME_DIR");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
        return Path.of(base).toAbsolutePath().normalize().resolve("worldline/locks/cache");
    }

    private static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-module-cache-");
        try {
            Path cache = root.resolve("modules"), old = entry(cache, "api", "a".repeat(64), 1L);
            Path live = entry(cache, "api", "b".repeat(64), 1L);
            Result result = maintain(cache, Set.of(live.toRealPath()), 1L, 1L, 10L, true);
            require(result.removed == 1 && !Files.exists(old) && Files.isDirectory(live),
                    "module cache GC removed a referenced entry or retained an expired entry");
            SafeTreeDelete.delete(live); Files.deleteIfExists(live.resolveSibling("b".repeat(64) + ".used"));
            Path late = entry(cache, "api", "c".repeat(64), 1L);
            java.util.concurrent.atomic.AtomicInteger snapshots = new java.util.concurrent.atomic.AtomicInteger();
            Result raced = maintain(cache, () -> snapshots.getAndIncrement() == 0
                    ? Set.of() : Set.of(late.toRealPath()), 1L, 1L, 10L, true);
            require(raced.removed == 0 && Files.isDirectory(late) && snapshots.get() >= 2,
                    "module cache GC did not re-snapshot live links under the entry lock");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  module cache maintenance self-test: passed");
    }

    private static Path entry(Path cache, String module, String digest, long used) throws Exception {
        Path path = cache.resolve(module).resolve(digest); Files.createDirectories(path);
        Files.writeString(path.resolve(".complete"), digest + "\n"); Files.writeString(path.resolve("data"), "x");
        Files.writeString(path.resolveSibling(digest + ".used"), Long.toString(used)); return path;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Result(int entries, long bytes, int referenced, int removed) { }
    private record Entry(Path path, Path usedPath, String digest, long used, long bytes) { }
}
