import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** Canonical binary envelope for Worldline-owned compiled module outputs. */
final class ModuleCacheBundle {
    private static final byte[] MAGIC = "worldline.module-cache-bundle.v1\n"
            .getBytes(StandardCharsets.US_ASCII);
    private static final long MAXIMUM_BYTES = 8L * 1024L * 1024L;
    private static final int MAXIMUM_FILES = 4096;

    private ModuleCacheBundle() { }

    static Identity pack(String module, Path entry, Path target) throws Exception {
        require(module.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid module bundle name");
        Path root = entry.toRealPath();
        String input = Files.readString(root.resolve(".complete"), StandardCharsets.UTF_8).trim();
        require(input.matches("[0-9a-f]{64}"), "invalid module cache identity");
        List<FileEntry> files = readClasses(root);
        require(!files.isEmpty(), "module cache bundle has no classes");
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        Files.createDirectories(target.getParent());
        try (OutputStream raw = Files.newOutputStream(temporary);
                DataOutputStream output = new DataOutputStream(raw)) {
            output.write(MAGIC); writeString(output, module); writeString(output, input);
            writeString(output, System.getProperty("java.version")); output.writeInt(files.size());
            for (FileEntry file : files) {
                writeString(output, file.path); output.writeLong(file.bytes.length);
                output.write(HexFormat.of().parseHex(file.sha256)); output.write(file.bytes);
            }
        }
        require(Files.size(temporary) <= MAXIMUM_BYTES, "module cache bundle is too large");
        move(temporary, target); return validate(target);
    }

    static Identity validate(Path bundle) throws Exception { return read(bundle, null).identity; }

    static void extract(Path bundle, String module, String input, Path target) throws Exception {
        Bundle decoded = read(bundle, Boolean.TRUE);
        require(decoded.identity.module.equals(module) && decoded.identity.input.equals(input),
                "module bundle identity drifted");
        require(decoded.identity.javaVersion.equals(System.getProperty("java.version")),
                "module bundle Java toolchain drifted");
        require(!Files.exists(target), "module cache target already exists");
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp-"
                + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try {
            Files.createDirectories(temporary);
            for (FileEntry file : decoded.files) {
                Path output = temporary.resolve(file.path);
                require(output.normalize().startsWith(temporary), "module bundle path escaped target");
                Files.createDirectories(output.getParent()); Files.write(output, file.bytes);
            }
            Files.writeString(temporary.resolve(".complete"), input + "\n", StandardCharsets.UTF_8);
            move(temporary, target);
        } finally { if (Files.exists(temporary)) SafeTreeDelete.delete(temporary); }
    }

    static void requireEqual(Path expected, Path restored) throws Exception {
        List<FileEntry> left = readClasses(expected.toRealPath());
        List<FileEntry> right = readClasses(restored.toRealPath());
        require(left.size() == right.size(), "restored module file count drifted");
        for (int index = 0; index < left.size(); index++) {
            FileEntry a = left.get(index), b = right.get(index);
            require(a.path.equals(b.path) && a.sha256.equals(b.sha256)
                    && Arrays.equals(a.bytes, b.bytes), "restored module bytes drifted: " + a.path);
        }
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-module-bundle-");
        try {
            Path entry = root.resolve("entry"); Files.createDirectories(entry.resolve("example"));
            String input = "a".repeat(64);
            Files.writeString(entry.resolve(".complete"), input + "\n", StandardCharsets.UTF_8);
            Files.write(entry.resolve("example/Fixture.class"), new byte[] {-54, -2, -70, -66, 0, 1});
            Path first = root.resolve("first.wmb"), second = root.resolve("second.wmb");
            Identity identity = pack("fixture", entry, first); pack("fixture", entry, second);
            require(Files.mismatch(first, second) == -1L && identity.input.equals(input),
                    "module bundle was not deterministic");
            Path restored = root.resolve("restored"); extract(first, "fixture", input, restored);
            requireEqual(entry, restored);
            byte[] corrupt = Files.readAllBytes(first); corrupt[corrupt.length - 1] ^= 1;
            Files.write(root.resolve("corrupt.wmb"), corrupt);
            rejected(() -> validate(root.resolve("corrupt.wmb")), "corrupt module bundle was accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  module cache bundle self-test: passed");
    }

    private static Bundle read(Path bundle, Boolean retain) throws Exception {
        require(Files.isRegularFile(bundle) && Files.size(bundle) <= MAXIMUM_BYTES,
                "invalid module cache bundle file");
        try (InputStream raw = Files.newInputStream(bundle); DataInputStream input = new DataInputStream(raw)) {
            require(Arrays.equals(input.readNBytes(MAGIC.length), MAGIC), "invalid module bundle schema");
            String module = readString(input, 128), digest = readString(input, 64);
            String javaVersion = readString(input, 128); int count = input.readInt();
            require(module.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                    && digest.matches("[0-9a-f]{64}"), "invalid module bundle identity");
            require(!javaVersion.isBlank() && javaVersion.matches("[A-Za-z0-9._+\\-]+"),
                    "invalid module bundle Java version");
            require(count > 0 && count <= MAXIMUM_FILES, "invalid module bundle file count");
            List<FileEntry> files = new ArrayList<>(); String previous = null; long total = 0L;
            for (int index = 0; index < count; index++) {
                String path = readString(input, 4096); long size = input.readLong();
                byte[] expected = input.readNBytes(32);
                require(expected.length == 32 && size > 0L && size <= 1024L * 1024L,
                        "invalid module bundle entry size");
                require(safe(path) && (previous == null || previous.compareTo(path) < 0),
                        "invalid module bundle entry path");
                total += size; require(total <= MAXIMUM_BYTES, "module bundle content is too large");
                byte[] bytes = input.readNBytes(Math.toIntExact(size));
                require(bytes.length == size && Arrays.equals(expected, sha256(bytes)),
                        "module bundle entry digest drifted: " + path);
                files.add(new FileEntry(path, HexFormat.of().formatHex(expected),
                        Boolean.TRUE.equals(retain) ? bytes : new byte[0])); previous = path;
            }
            require(input.read() == -1, "module bundle has trailing bytes");
            Identity identity = new Identity(module, digest, javaVersion, sha256(bundle), Files.size(bundle));
            return new Bundle(identity, List.copyOf(files));
        } catch (EOFException error) { throw new IllegalStateException("truncated module bundle", error); }
    }

    private static List<FileEntry> readClasses(Path root) throws Exception {
        List<FileEntry> files = new ArrayList<>();
        for (Path path : SafeTreeDelete.paths(root).stream().sorted(Comparator.naturalOrder()).toList()) {
            require(!SafeTreeDelete.linkLike(path), "module cache entry contains a filesystem link");
            if (!Files.isRegularFile(path) || path.getFileName().toString().equals(".complete")) continue;
            String relative = root.relativize(path).toString().replace('\\', '/');
            require(safe(relative), "module cache entry contains a non-class file: " + relative);
            byte[] bytes = Files.readAllBytes(path);
            files.add(new FileEntry(relative, HexFormat.of().formatHex(sha256(bytes)), bytes));
        }
        return List.copyOf(files);
    }

    private static boolean safe(String path) {
        return path.endsWith(".class") && !path.startsWith("/") && !path.contains("\\")
                && !path.contains("//") && !path.equals(".class")
                && Arrays.stream(path.split("/", -1)).noneMatch(part -> part.isEmpty()
                        || part.equals(".") || part.equals(".."));
    }
    private static void writeString(DataOutputStream output, String value) throws Exception {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8); output.writeInt(bytes.length); output.write(bytes);
    }
    private static String readString(DataInputStream input, int maximum) throws Exception {
        int length = input.readInt(); require(length > 0 && length <= maximum, "invalid module bundle string");
        byte[] bytes = input.readNBytes(length); require(bytes.length == length, "truncated module bundle string");
        String value = new String(bytes, StandardCharsets.UTF_8);
        require(Arrays.equals(value.getBytes(StandardCharsets.UTF_8), bytes), "invalid module bundle UTF-8");
        return value;
    }
    private static byte[] sha256(byte[] bytes) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(bytes);
    }
    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static void move(Path source, Path target) throws Exception {
        require(!Files.exists(target), "module bundle target already exists");
        try { Files.move(source, target, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) { Files.move(source, target); }
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false; try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Identity(String module, String input, String javaVersion, String sha256, long size) { }
    private record FileEntry(String path, String sha256, byte[] bytes) { }
    private record Bundle(Identity identity, List<FileEntry> files) { }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
