import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Canonical Java-owned mapping from logical module inputs to opaque CAS blobs. */
final class ModuleCacheIndex {
    private static final String SCHEMA = "worldline.module-cache-index.v1";
    private static final Set<String> ROOT_KEYS = Set.of("schema", "entries");
    private static final Set<String> ENTRY_KEYS = Set.of(
            "module", "input_sha256", "java_version", "blob_sha256", "blob_size");
    private final Map<Key, Entry> entries;

    private ModuleCacheIndex(List<Entry> values) {
        Map<Key, Entry> indexed = new HashMap<>();
        for (Entry entry : values) {
            validate(entry); Key key = new Key(entry.module, entry.inputSha256, entry.javaVersion);
            require(indexed.putIfAbsent(key, entry) == null, "duplicate module cache index entry");
        }
        require(!indexed.isEmpty() && indexed.size() <= 64, "invalid module cache index size");
        entries = Map.copyOf(indexed);
    }

    static void write(Path path, List<Entry> entries) throws Exception {
        ModuleCacheIndex index = new ModuleCacheIndex(entries);
        String text = encode(index.sorted()); Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) { Files.move(temporary, path); }
    }

    static ModuleCacheIndex read(Path path) throws Exception {
        require(Files.isRegularFile(path) && Files.size(path) <= 1024L * 1024L,
                "invalid module cache index file");
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Map<String, Object> root = MiniJson.object(text);
        require(root.keySet().equals(ROOT_KEYS) && SCHEMA.equals(MiniJson.string(root, "schema")),
                "invalid module cache index schema");
        List<Entry> entries = new ArrayList<>();
        for (Object value : MiniJson.array(root, "entries")) {
            Map<String, Object> row = MiniJson.asObject(value, "module cache index entry");
            require(row.keySet().equals(ENTRY_KEYS), "invalid module cache index entry fields");
            entries.add(new Entry(MiniJson.string(row, "module"),
                    MiniJson.string(row, "input_sha256"), MiniJson.string(row, "java_version"),
                    MiniJson.string(row, "blob_sha256"), MiniJson.integer(row, "blob_size")));
        }
        ModuleCacheIndex index = new ModuleCacheIndex(entries);
        require(text.equals(encode(index.sorted())), "noncanonical module cache index"); return index;
    }

    static void validate(Path path) throws Exception { read(path); }

    Entry lookup(String module, String inputSha256, String javaVersion) {
        return entries.get(new Key(module, inputSha256, javaVersion));
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-module-index-");
        try {
            Entry entry = new Entry("api", "1".repeat(64), "21.0.1", "2".repeat(64), 4096L);
            Path path = root.resolve("index.wmi"); write(path, List.of(entry));
            require(read(path).lookup("api", "1".repeat(64), "21.0.1").equals(entry),
                    "module cache index lookup drifted");
            String text = Files.readString(path); Files.writeString(path,
                    text.replace("\"entries\": [", "\"unknown\": true,\n  \"entries\": ["));
            rejected(() -> read(path), "unknown module cache index field was accepted");
            rejected(() -> new ModuleCacheIndex(List.of(entry, entry)),
                    "duplicate module cache index entry was accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  module cache index self-test: passed");
    }

    private List<Entry> sorted() {
        return entries.values().stream().sorted(Comparator.comparing(Entry::module)
                .thenComparing(Entry::inputSha256).thenComparing(Entry::javaVersion)).toList();
    }
    private static String encode(List<Entry> entries) {
        StringBuilder text = new StringBuilder("{\n  \"schema\": \"").append(SCHEMA)
                .append("\",\n  \"entries\": [\n");
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            text.append("    {\"module\":\"").append(entry.module)
                    .append("\",\"input_sha256\":\"").append(entry.inputSha256)
                    .append("\",\"java_version\":\"").append(entry.javaVersion)
                    .append("\",\"blob_sha256\":\"").append(entry.blobSha256)
                    .append("\",\"blob_size\":").append(entry.blobSize).append('}');
            text.append(index + 1 == entries.size() ? '\n' : ",\n");
        }
        return text.append("  ]\n}\n").toString();
    }
    private static void validate(Entry entry) {
        require(entry.module.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                && entry.inputSha256.matches("[0-9a-f]{64}")
                && entry.javaVersion.matches("[A-Za-z0-9._+\\-]+")
                && entry.blobSha256.matches("[0-9a-f]{64}")
                && entry.blobSize > 0L && entry.blobSize <= 8L * 1024L * 1024L,
                "invalid module cache index entry");
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Entry(String module, String inputSha256, String javaVersion,
            String blobSha256, long blobSize) { }
    private record Key(String module, String inputSha256, String javaVersion) { }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
