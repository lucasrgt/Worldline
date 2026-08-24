import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/** Removes only disposable private artifacts without following filesystem links. */
public final class WorktreePrivateCleanup {
    private WorktreePrivateCleanup() { }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            require(arguments.length == 2 && "prune".equals(arguments[0]),
                    "usage: WorktreePrivateCleanup prune PATH|--self-test");
            Result result = prune(Path.of(arguments[1]).toAbsolutePath().normalize());
            System.out.println("files=" + result.files + ";bytes=" + result.bytes);
        } catch (Exception error) {
            System.err.println("worktree private cleanup failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Result prune(Path worktree) throws Exception {
        require(worktree.getNameCount() > 2, "refusing broad private cleanup target");
        long files = 0L, bytes = 0L;
        for (String name : List.of(".worldline", "tmp", "output")) {
            Path target = worktree.resolve(name).toAbsolutePath().normalize();
            require(target.startsWith(worktree) && !target.equals(worktree),
                    "unsafe private cleanup path");
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) continue;
            Result measured = size(target); files += measured.files; bytes += measured.bytes;
            delete(target);
        }
        return new Result(files, bytes);
    }

    private static Result size(Path target) throws Exception {
        BasicFileAttributes attributes = attributes(target);
        if (attributes.isRegularFile()) return new Result(1L, attributes.size());
        if (!attributes.isDirectory() || attributes.isOther()) return new Result(0L, 0L);
        long files = 0L, bytes = 0L;
        try (var children = Files.newDirectoryStream(target)) {
            for (Path child : children) {
                Result measured = size(child); files += measured.files; bytes += measured.bytes;
            }
        }
        return new Result(files, bytes);
    }

    private static void delete(Path target) throws Exception {
        BasicFileAttributes attributes = attributes(target);
        if (attributes.isDirectory() && !attributes.isOther())
            try (var children = Files.newDirectoryStream(target)) {
                for (Path child : children) delete(child);
            }
        Files.deleteIfExists(target);
    }

    private static BasicFileAttributes attributes(Path path) throws Exception {
        return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }

    private static void selfTest() throws Exception {
        Path parent = Files.createTempDirectory("worldline-lifecycle-test-");
        Path target = parent.resolve("worktree"), external = parent.resolve("external");
        Files.createDirectories(target); Files.createDirectories(external);
        Path retained = target.resolve("retained.txt"); Files.writeString(retained, "retained");
        try {
            for (String name : List.of(".worldline", "tmp", "output")) {
                Path file = target.resolve(name).resolve("private.bin");
                Files.createDirectories(file.getParent()); Files.write(file, new byte[] {1, 2, 3});
            }
            Path sentinel = external.resolve("sentinel"); Files.writeString(sentinel, "retained");
            Path link = target.resolve(".worldline/cache-link"); createLink(link, external);
            Result result = prune(target);
            require(result.files == 3L && result.bytes == 9L && Files.isRegularFile(retained)
                    && Files.isRegularFile(sentinel), "private cleanup scope drifted");
            System.out.println("worktree private cleanup self-test passed");
        } finally { delete(parent); }
    }

    private static void createLink(Path link, Path external) throws Exception {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            Files.createSymbolicLink(link, external); return;
        }
        Process process = new ProcessBuilder("cmd.exe", "/d", "/c", "mklink", "/J",
                link.toString(), external.toString())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        require(process.waitFor() == 0, "lifecycle junction fixture failed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record Result(long files, long bytes) { }
}
