import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Disposable exact-commit worktree for one official server differential execution. */
final class ExecutorServerWorkspace implements AutoCloseable {
    private final Path source;
    private final Path root;
    private boolean attached;

    private ExecutorServerWorkspace(Path source, Path root) {
        this.source = source; this.root = root;
    }

    static ExecutorServerWorkspace open(Path source, String label, Path artifact) throws Exception {
        require(label.matches("legacy|rust"), "invalid server workspace label");
        Path parent = source.getParent().toAbsolutePath().normalize();
        Path target = parent.resolve(source.getFileName() + "-executor-server-"
                + ProcessHandle.current().pid() + "-" + label).normalize();
        require(target.getParent().equals(parent) && !Files.exists(target),
                "executor server workspace already exists");
        ExecutorServerWorkspace workspace = new ExecutorServerWorkspace(source, target);
        try {
            workspace.git(List.of("worktree", "add", "--detach", target.toString(), "HEAD"), 180);
            workspace.attached = true;
            Path relative = source.relativize(artifact);
            require(relative.startsWith("local") && !relative.equals(Path.of("local")),
                    "official server artifact is outside local/");
            Path copy = target.resolve(relative).normalize();
            require(copy.startsWith(target.resolve("local")), "official server copy escaped local/");
            Files.createDirectories(copy.getParent());
            Files.copy(artifact, copy, StandardCopyOption.COPY_ATTRIBUTES);
            return workspace;
        } catch (Exception error) {
            try { workspace.close(); } catch (Exception cleanup) { error.addSuppressed(cleanup); }
            throw error;
        }
    }

    Path root() { return root; }

    void prepare() throws Exception {
        run(root, List.of(java(), root.resolve("tools/harness/Gate.java").toString()), 600);
    }

    Path authorize(Path lock, String leasePid) throws Exception {
        String secret = UUID.randomUUID().toString();
        Path token = root.resolve(".worldline/runtime-fabric/executor-server.properties");
        List<String> command = List.of(java(), root.resolve("tools/containers/PoolLeaseToken.java").toString(),
                "create", token.toString(), secret, Long.toString(ProcessHandle.current().pid()),
                lock.toString(), "executor-differential", leasePid);
        run(root, command, 60); return token;
    }

    @Override public void close() {
        if (!attached) return;
        attached = false;
        try {
            git(List.of("worktree", "remove", "--force", root.toString()), 180);
            require(!Files.exists(root), "executor server workspace cleanup was incomplete");
        } catch (Exception error) {
            throw new IllegalStateException("executor server workspace cleanup failed", error);
        }
    }

    private void git(List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(arguments);
        run(source, command, seconds);
    }

    private static void run(Path directory, List<String> command, int seconds) throws Exception {
        Path log = Files.createTempFile("worldline-executor-server-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            require(process.waitFor(seconds, TimeUnit.SECONDS), command.get(0) + " timed out");
            String output = Files.readString(log);
            require(process.exitValue() == 0, command.get(0) + " failed:\n" + output);
        } finally { Files.deleteIfExists(log); }
    }

    private static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
