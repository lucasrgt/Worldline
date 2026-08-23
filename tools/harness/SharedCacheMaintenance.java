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
        new ModuleCacheMaintenance().execute(delete);
        Result result = maintain(cache, policy.maximumBytes(), policy.minimumAgeMillis(),
                System.currentTimeMillis(), delete);
        System.out.println("shared-cache.entries=" + result.entries + ";bytes=" + result.bytes
                + ";families=" + result.families + ";removed=" + result.removed);
        System.out.println("WORLDLINE_SHARED_CACHE_" + (delete ? "GC" : "DOCTOR") + "=PASS");
    }

    static Result maintain(Path cache, long maximum, long minimumAge, long now, boolean delete)
            throws Exception {
        List<Entry> entries = entries(cache); long total = entries.stream().mapToLong(Entry::bytes).sum();
        int removed = 0;
        for (Entry entry : entries) {
            if (!delete || now - entry.used < minimumAge && total <= maximum) continue;
            Path lockPath = entry.parent.resolve(entry.digest + ".lock");
            try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE); FileLock lock = tryLock(channel)) {
                if (lock == null) continue;
                for (Path member : entry.members) {
                    if (Files.isDirectory(member)) SafeTreeDelete.delete(member);
                    else Files.deleteIfExists(member);
                }
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
        try (Stream<Path> paths = Files.walk(cache)) {
            for (Path path : paths.toList()) {
                if (path.startsWith(cache.resolve("modules"))) continue;
                String name = path.getFileName().toString();
                String digest = name.matches("[0-9a-f]{64}") ? name
                        : name.matches("[0-9a-f]{64}[.](?:properties|log)")
                                ? name.substring(0, 64) : null;
                if (digest == null || name.endsWith(".used") || name.endsWith(".lock")) continue;
                groups.computeIfAbsent(new Key(path.getParent(), digest), ignored -> new ArrayList<>()).add(path);
            }
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
    private static long size(Path path) throws Exception { if (Files.isRegularFile(path)) return Files.size(path);
        try (Stream<Path> paths = Files.walk(path)) { long total = 0;
            for (Path file : paths.filter(Files::isRegularFile).toList()) total += Files.size(file); return total; } }
    private static FileLock tryLock(FileChannel channel) throws Exception {
        try { return channel.tryLock(); }
        catch (java.nio.channels.OverlappingFileLockException error) { return null; }
    }
    private static void selfTest() throws Exception { Path root = Files.createTempDirectory("worldline-cache-gc-");
        try { Path family = root.resolve("tests/api"), digest = family.resolve("a".repeat(64) + ".properties");
            Files.createDirectories(family); Files.writeString(digest, "proof");
            Result result = maintain(root, 1, 1, System.currentTimeMillis() + 10, true);
            require(result.removed == 1 && !Files.exists(digest), "shared cache GC self-test drifted");
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
