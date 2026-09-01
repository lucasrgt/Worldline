import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Disposable exact-commit worktree with a bounded local GUI workspace copy. */
final class ExecutorGuiWorkspace implements AutoCloseable {
    private static final int MAX_FILES = 10_000;
    private static final long MAX_BYTES = 512L << 20;
    private final Path source;
    private final Path root;
    private boolean attached;

    private ExecutorGuiWorkspace(Path source, Path root) { this.source = source; this.root = root; }

    static ExecutorGuiWorkspace open(Path source, String label, Path localWorkspace) throws Exception {
        require(label.matches("legacy|rust"), "invalid GUI workspace label");
        require(localWorkspace.startsWith(source.resolve("local"))
                && Files.isDirectory(localWorkspace, LinkOption.NOFOLLOW_LINKS),
                "GUI runtime workspace is absent or outside local/");
        Path parent = source.getParent().toAbsolutePath().normalize();
        Path target = parent.resolve(source.getFileName() + "-executor-gui-"
                + ProcessHandle.current().pid() + "-" + label).normalize();
        require(target.getParent().equals(parent) && !Files.exists(target),
                "executor GUI workspace already exists");
        ExecutorGuiWorkspace workspace = new ExecutorGuiWorkspace(source, target);
        try {
            workspace.git(List.of("worktree", "add", "--detach", target.toString(), "HEAD"), 180);
            workspace.attached = true;
            copy(localWorkspace, target.resolve(source.relativize(localWorkspace)));
            return workspace;
        } catch (Exception error) {
            try { workspace.close(); } catch (Exception cleanup) { error.addSuppressed(cleanup); }
            throw error;
        }
    }

    Path root() { return root; }

    static void selfTest() throws Exception {
        Path base = Files.createTempDirectory("worldline-gui-workspace-").toAbsolutePath().normalize();
        try {
            Path source = base.resolve("source"), target = base.resolve("target");
            Files.createDirectories(source.resolve("nested")); Files.write(source.resolve("nested/probe"),
                    new byte[] {1, 2, 3}); copy(source, target);
            require(Files.size(target.resolve("nested/probe")) == 3L, "GUI workspace copy drift");
        } finally { try (var paths = Files.walk(base)) { for (Path path : paths
                .sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); } }
    }

    void prepare() throws Exception {
        run(root, List.of(java(), root.resolve("tools/harness/Gate.java").toString()), 600);
    }

    Path authorize(Path lock, String leasePid) throws Exception {
        String secret = UUID.randomUUID().toString();
        Path token = root.resolve(".worldline/runtime-fabric/executor-gui.properties");
        run(root, List.of(java(), root.resolve("tools/containers/PoolLeaseToken.java").toString(),
                "create", token.toString(), secret, Long.toString(ProcessHandle.current().pid()),
                lock.toString(), "executor-differential", leasePid), 60);
        return token;
    }

    @Override public void close() {
        if (!attached) return; attached = false;
        try {
            try { git(List.of("worktree", "remove", "--force", root.toString()), 180); }
            catch (Exception ignored) { }
            if (Files.exists(root)) run(source, List.of(java(), source.resolve(
                    "tools/harness/SafeTreeDelete.java").toString(), root.toString(),
                    source.getParent().toString()), 120);
            git(List.of("worktree", "prune"), 60);
            require(!Files.exists(root), "executor GUI workspace cleanup was incomplete");
        } catch (Exception error) {
            throw new IllegalStateException("executor GUI workspace cleanup failed", error);
        }
    }

    private static void copy(Path source, Path target) throws Exception {
        Counter counter = new Counter();
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes)
                    throws java.io.IOException {
                require(!attributes.isSymbolicLink() && !attributes.isOther(),
                        "GUI workspace contains a linked directory");
                Files.createDirectories(target.resolve(source.relativize(directory)));
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws java.io.IOException {
                require(attributes.isRegularFile() && !Files.isSymbolicLink(file),
                        "GUI workspace contains a non-regular file");
                counter.files++; counter.bytes = Math.addExact(counter.bytes, attributes.size());
                require(counter.files <= MAX_FILES && counter.bytes <= MAX_BYTES,
                        "GUI workspace copy budget exceeded");
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
        require(counter.files > 0 && counter.bytes > 0L, "GUI workspace copy is empty");
    }

    private void git(List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(arguments);
        run(source, command, seconds);
    }
    private static void run(Path directory, List<String> command, int seconds) throws Exception {
        Path log = Files.createTempFile("worldline-executor-gui-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try { require(process.waitFor(seconds, TimeUnit.SECONDS), command.get(0) + " timed out");
            require(process.exitValue() == 0, command.get(0) + " failed:\n" + Files.readString(log));
        } finally { Files.deleteIfExists(log); }
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private static final class Counter { private int files; private long bytes; }
}
