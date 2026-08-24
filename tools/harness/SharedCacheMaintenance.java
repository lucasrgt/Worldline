import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Doctors and bounds every immutable shared cache under one reviewed policy. */
public final class SharedCacheMaintenance {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path cache = ModuleCacheMaintenance.cacheRoot();

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1 && List.of("doctor", "gc", "--self-test").contains(arguments[0]),
                    "usage: SharedCacheMaintenance doctor|gc|--self-test");
            if (arguments[0].equals("--self-test")) selfTest();
            else new SharedCacheMaintenance().execute(arguments[0].equals("gc"));
        } catch (Exception error) {
            System.err.println("shared cache maintenance failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void execute(boolean delete) throws Exception {
        CachePolicy policy = new CachePolicy(root);
        CacheReferences.Snapshot before = CacheReferences.inspect(root, cache);
        new ModuleCacheMaintenance().execute(delete);
        CacheReferences.Snapshot initial = CacheReferences.inspect(root, cache);
        require(!delete || initial.dangling() <= before.dangling(),
                "module cache GC created dangling worktree links");
        CacheReferences.Source references = () -> CacheReferences.snapshot(root, cache);
        Result result = maintain(cache, references, policy.maximumBytes(),
                policy.minimumAgeMillis(), System.currentTimeMillis(), delete);
        System.out.println("cache-references.live=" + initial.paths().size()
                + ";dangling=" + initial.dangling());
        System.out.println("shared-cache.entries=" + result.entries + ";bytes=" + result.bytes
                + ";families=" + result.families + ";removed=" + result.removed);
        if (delete) {
            new ModuleCacheMaintenance().execute(false);
            Result doctor = maintain(cache, references, policy.maximumBytes(),
                    policy.minimumAgeMillis(), System.currentTimeMillis(), false);
            CacheReferences.Snapshot after = CacheReferences.inspect(root, cache);
            require(after.dangling() <= initial.dangling(),
                    "shared cache GC created dangling worktree links");
            System.out.println("post-gc-cache.entries=" + doctor.entries + ";bytes=" + doctor.bytes
                    + ";families=" + doctor.families + ";dangling=" + after.dangling());
            System.out.println("WORLDLINE_POST_GC_CACHE_DOCTOR=PASS");
        }
        System.out.println("WORLDLINE_SHARED_CACHE_" + (delete ? "GC" : "DOCTOR") + "=PASS");
    }

    static Result maintain(Path cache, long maximum, long minimumAge, long now, boolean delete)
            throws Exception {
        return maintain(cache, Set::of, maximum, minimumAge, now, delete);
    }

    static Result maintain(Path cache, CacheReferences.Source source, long maximum,
            long minimumAge, long now, boolean delete) throws Exception {
        require(maximum > 0L, "shared cache maximum must be positive");
        List<Entry> entries = entries(cache); long total = entries.stream().mapToLong(Entry::bytes).sum();
        int removed = 0;
        for (Entry entry : entries) {
            if (!delete) continue;
            if (protectedBy(entry, source.snapshot()) || now - entry.used < minimumAge) continue;
            Path lockPath = entry.parent.resolve(entry.digest + ".lock");
            try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE); FileLock lock = tryLock(channel)) {
                if (lock == null || protectedBy(entry, source.snapshot())) continue;
                for (Path member : entry.members) SafeTreeDelete.delete(member);
                Files.deleteIfExists(entry.parent.resolve(entry.digest + ".used"));
                total -= entry.bytes; removed++;
            }
        }
        long families = entries.stream().map(entry -> entry.family).distinct().count();
        return new Result(entries.size(), total, Math.toIntExact(families), removed);
    }

    private static List<Entry> entries(Path cache) throws Exception {
        Map<Key, List<Path>> groups = new HashMap<>();
        if (!Files.isDirectory(cache)) return List.of();
        for (Path path : SafeTreeDelete.paths(cache)) {
                require(!SafeTreeDelete.linkLike(path), "shared cache contains a filesystem link: " + path);
                if (path.startsWith(cache.resolve("modules"))) continue;
                String name = path.getFileName().toString();
                String digest = name.matches("[0-9a-f]{64}") ? name
                        : name.matches("[0-9a-f]{64}[.](?:properties|log)")
                                ? name.substring(0, 64) : null;
                if (digest == null || name.endsWith(".used") || name.endsWith(".lock")) continue;
                groups.computeIfAbsent(new Key(path.getParent(), digest), ignored -> new ArrayList<>()).add(path);
        }
        List<Entry> result = new ArrayList<>();
        for (var row : groups.entrySet()) {
            Path marker = row.getKey().parent.resolve(row.getKey().digest + ".used");
            long used = Files.isRegularFile(marker) ? parse(marker) : newest(row.getValue());
            long bytes = 0; for (Path member : row.getValue()) bytes += size(member);
            Path relative = cache.relativize(row.getKey().parent);
            String family = relative.getNameCount() == 0 ? "root" : relative.getName(0).toString();
            result.add(new Entry(row.getKey().parent, row.getKey().digest, family,
                    List.copyOf(row.getValue()), used, bytes));
        }
        result.sort(Comparator.comparingLong(Entry::used)); return result;
    }
    private static long newest(List<Path> paths) throws Exception { long value = 0;
        for (Path path : paths) value = Math.max(value, Files.getLastModifiedTime(path).toMillis()); return value; }
    private static long parse(Path path) throws Exception {
        try { return Long.parseLong(Files.readString(path, StandardCharsets.UTF_8).trim()); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid cache usage " + path); }
    }
    private static long size(Path path) throws Exception { return SafeTreeDelete.size(path); }
    private static boolean protectedBy(Entry entry, Set<Path> references) throws Exception {
        for (Path member : entry.members)
            if (CacheReferences.protects(member, references)) return true;
        return false;
    }
    private static FileLock tryLock(FileChannel channel) throws Exception {
        try { return channel.tryLock(); }
        catch (java.nio.channels.OverlappingFileLockException error) { return null; }
    }
    private static void selfTest() throws Exception { Path root = Files.createTempDirectory("worldline-cache-gc-");
        try { Path family = root.resolve("tests/api"), digest = family.resolve("a".repeat(64) + ".properties");
            Files.createDirectories(family); Files.writeString(digest, "proof");
            Result result = maintain(root, 1, 1, System.currentTimeMillis() + 10, true);
            require(result.removed == 1 && !Files.exists(digest), "shared cache GC self-test drifted");
            Path recent = family.resolve("b".repeat(64) + ".properties"); Files.writeString(recent, "proof");
            Files.writeString(family.resolve("b".repeat(64) + ".used"),
                    Long.toString(System.currentTimeMillis()));
            Result retained = maintain(root, 1, 60_000, System.currentTimeMillis(), true);
            require(retained.removed == 0 && Files.isRegularFile(recent),
                    "shared cache GC deleted a recently used entry above the soft ceiling");
            SafeTreeDelete.delete(recent); Files.deleteIfExists(family.resolve("b".repeat(64) + ".used"));
            Path linked = family.resolve("c".repeat(64)); Files.createDirectories(linked);
            Files.writeString(linked.resolve("data"), "proof");
            java.util.concurrent.atomic.AtomicInteger snapshots = new java.util.concurrent.atomic.AtomicInteger();
            Result raced = maintain(root, () -> snapshots.getAndIncrement() == 0
                    ? Set.of() : Set.of(linked.toRealPath()), 1, 1,
                    System.currentTimeMillis() + 10, true);
            require(raced.removed == 0 && Files.isDirectory(linked) && snapshots.get() >= 2,
                    "shared cache GC did not re-snapshot live links under the entry lock");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  shared cache maintenance self-test: passed");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Result(int entries, long bytes, int families, int removed) { }
    private record Key(Path parent, String digest) { }
    private record Entry(Path parent, String digest, String family, List<Path> members, long used, long bytes) { }
}
