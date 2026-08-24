import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Deletes a tree without traversing symbolic links or Windows reparse points. */
public final class SafeTreeDelete {
    private SafeTreeDelete() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 2) throw new IllegalArgumentException(
                    "usage: SafeTreeDelete TARGET ALLOWED_ROOT");
            Path target = Path.of(arguments[0]).toAbsolutePath().normalize();
            Path allowed = Path.of(arguments[1]).toAbsolutePath().normalize();
            if (!target.startsWith(allowed) || target.equals(allowed))
                throw new IllegalArgumentException("unsafe tree deletion target: " + target);
            delete(target);
        } catch (Exception error) {
            System.err.println("safe tree deletion failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void delete(Path target) throws IOException {
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return;
        BasicFileAttributes attributes = Files.readAttributes(target,
                BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (attributes.isDirectory() && !attributes.isOther()) {
            try (var children = Files.newDirectoryStream(target)) {
                for (Path child : children) delete(child);
            }
        }
        target.toFile().setWritable(true); Files.deleteIfExists(target);
    }

    static List<Path> paths(Path target) throws IOException {
        return paths(target, Integer.MAX_VALUE);
    }

    static List<Path> paths(Path target, int maximumDepth) throws IOException {
        if (maximumDepth < 0) throw new IllegalArgumentException("maximumDepth must be non-negative");
        List<Path> result = new ArrayList<>(); collect(target, result, 0, maximumDepth);
        return List.copyOf(result);
    }

    static long size(Path target) throws IOException {
        long total = 0L;
        for (Path path : paths(target)) {
            BasicFileAttributes attributes = attributes(path);
            if (attributes.isRegularFile()) total += attributes.size();
        }
        return total;
    }

    static boolean linkLike(Path target) throws IOException {
        BasicFileAttributes attributes = attributes(target);
        return attributes.isSymbolicLink() || attributes.isOther();
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-safe-tree-");
        Path external = Files.createTempDirectory("worldline-safe-tree-external-");
        Path sentinel = external.resolve("sentinel"); Files.writeString(sentinel, "retained");
        try {
            Path link = root.resolve("external-link");
            if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
                Process process = new ProcessBuilder("cmd.exe", "/d", "/c", "mklink", "/J",
                        link.toString(), external.toString()).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
                if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0)
                    throw new IOException("safe tree junction fixture failed");
            } else Files.createSymbolicLink(link, external);
            if (!linkLike(link) || size(root) != 0L)
                throw new IllegalStateException("safe tree followed a filesystem link");
            delete(root);
            if (!Files.isRegularFile(sentinel))
                throw new IllegalStateException("safe tree deleted an external link target");
        } finally { delete(root); delete(external); }
        System.out.println("  no-follow tree self-test: passed");
    }

    private static void collect(Path target, List<Path> result, int depth, int maximumDepth)
            throws IOException {
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return;
        BasicFileAttributes attributes = attributes(target); result.add(target);
        if (depth < maximumDepth && attributes.isDirectory() && !attributes.isOther())
            try (var children = Files.newDirectoryStream(target)) {
                for (Path child : children) collect(child, result, depth + 1, maximumDepth);
            }
    }

    private static BasicFileAttributes attributes(Path target) throws IOException {
        return Files.readAttributes(target, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }
}
