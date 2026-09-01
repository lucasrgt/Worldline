import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** Atomic no-replace promotion for one fully validated sibling workspace. */
final class WorkspaceTransaction {
    private WorkspaceTransaction() { }

    static String promote(Path staging, Path target) throws Exception {
        staging = clean(staging); target = clean(target);
        require(staging.getParent().equals(target.getParent()) && !staging.equals(target),
                "workspace transaction requires distinct siblings");
        require(Files.isDirectory(staging, LinkOption.NOFOLLOW_LINKS), "staging workspace is absent");
        require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS), "workspace target already exists");
        String before = digest(staging);
        try { Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) {
            throw new IllegalStateException("atomic workspace promotion is unsupported", error);
        }
        require(!Files.exists(staging, LinkOption.NOFOLLOW_LINKS), "staging workspace survived promotion");
        String after = digest(target);
        if (!before.equals(after)) {
            try { Files.move(target, staging, StandardCopyOption.ATOMIC_MOVE); }
            catch (Exception rollback) { error("workspace digest drift and rollback failed", rollback); }
            throw new IllegalStateException("workspace digest drift; promotion rolled back");
        }
        return after;
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-workspace-transaction-").toAbsolutePath().normalize();
        Path staging = root.resolve("staging"), target = root.resolve("target");
        Path collision = root.resolve("collision"), occupied = root.resolve("occupied");
        try {
            Files.createDirectories(staging.resolve("nested"));
            Files.writeString(staging.resolve("nested/value"), "immutable", StandardCharsets.UTF_8);
            String digest = promote(staging, target);
            require(digest.equals(digest(target)) && !Files.exists(staging), "workspace promotion drift");
            Files.createDirectories(collision); Files.writeString(collision.resolve("new"), "new");
            Files.createDirectories(occupied); Files.writeString(occupied.resolve("old"), "old");
            String retained = digest(occupied); expectFailure(() -> promote(collision, occupied));
            require(Files.isDirectory(collision) && retained.equals(digest(occupied)),
                    "collision did not preserve both workspaces");
        } finally {
            deleteKnown(target.resolve("nested/value"), target.resolve("nested"), target,
                    collision.resolve("new"), collision, occupied.resolve("old"), occupied, root);
        }
    }

    private static String digest(Path root) throws Exception {
        List<Path> paths = new ArrayList<>(); collect(root, paths); paths.sort(Comparator.comparing(Path::toString));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Path path : paths) {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            require(!attributes.isSymbolicLink() && !attributes.isOther(), "link-like workspace entry is forbidden");
            String relative = root.relativize(path).toString().replace('\\', '/');
            update(digest, attributes.isDirectory() ? "directory" : "file"); update(digest, relative);
            if (!attributes.isDirectory()) {
                require(attributes.isRegularFile(), "non-regular workspace entry is forbidden");
                update(digest, Long.toString(attributes.size())); digest.update(Files.readAllBytes(path));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void collect(Path path, List<Path> paths) throws Exception {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        require(!attributes.isSymbolicLink() && !attributes.isOther(), "link-like workspace entry is forbidden");
        paths.add(path);
        if (attributes.isDirectory()) try (var children = Files.newDirectoryStream(path)) {
            for (Path child : children) collect(child, paths);
        }
    }

    private static void deleteKnown(Path... paths) throws Exception {
        for (Path path : paths) Files.deleteIfExists(path);
    }
    private static Path clean(Path path) { require(path.isAbsolute() && path.normalize().equals(path),
            "workspace path is not absolute and normalized"); return path; }
    private static void update(MessageDigest digest, String value) { digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0); }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid workspace transaction was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void error(String message, Exception cause) { throw new IllegalStateException(message, cause); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
