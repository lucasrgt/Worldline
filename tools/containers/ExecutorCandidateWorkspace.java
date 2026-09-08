import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Disposable exact-commit worktree for one Candidate executor. */
final class ExecutorCandidateWorkspace implements AutoCloseable {
    private final Path source;
    private final Path root;
    private boolean attached;

    private ExecutorCandidateWorkspace(Path source, Path root) {
        this.source = source; this.root = root;
    }

    static ExecutorCandidateWorkspace open(Path source, String label) throws Exception {
        require(label.matches("legacy|rust"), "invalid Candidate workspace label");
        Path parent = source.getParent().toAbsolutePath().normalize();
        Path target = parent.resolve(source.getFileName() + "-executor-candidate-"
                + ProcessHandle.current().pid() + "-" + label).normalize();
        require(target.getParent().equals(parent) && !Files.exists(target),
                "executor Candidate workspace already exists");
        ExecutorCandidateWorkspace workspace = new ExecutorCandidateWorkspace(source, target);
        try {
            workspace.git(List.of("worktree", "add", "--detach", target.toString(), "HEAD"), 180);
            workspace.attached = true;
            require(workspace.capture(target, List.of("git", "rev-parse", "HEAD"), 30).trim()
                    .equals(workspace.capture(source, List.of("git", "rev-parse", "HEAD"), 30).trim()),
                    "executor Candidate workspace commit drift");
            return workspace;
        } catch (Exception error) {
            try { workspace.close(); } catch (Exception cleanup) { error.addSuppressed(cleanup); }
            throw error;
        }
    }

    Path root() { return root; }

    @Override public void close() {
        if (!attached) return;
        attached = false;
        try {
            try { git(List.of("worktree", "remove", "--force", root.toString()), 180); }
            catch (Exception ignored) { }
            if (Files.exists(root)) run(source, List.of(java(), source.resolve(
                    "tools/harness/SafeTreeDelete.java").toString(), root.toString(),
                    source.getParent().toString()), 60);
            git(List.of("worktree", "prune"), 60);
            require(!Files.exists(root), "executor Candidate workspace cleanup was incomplete");
        } catch (Exception error) {
            throw new IllegalStateException("executor Candidate workspace cleanup failed", error);
        }
    }

    private void git(List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(arguments);
        Path log = run(source, command, seconds); Files.deleteIfExists(log);
    }
    private String capture(Path directory, List<String> command, int seconds) throws Exception {
        Path log = run(directory, command, seconds); try { return Files.readString(log, StandardCharsets.UTF_8); }
        finally { Files.deleteIfExists(log); }
    }
    private static Path run(Path directory, List<String> command, int seconds) throws Exception {
        Path log = Files.createTempFile("worldline-executor-candidate-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        require(process.waitFor(seconds, TimeUnit.SECONDS), command.get(0) + " timed out");
        if (process.exitValue() != 0) {
            String output = Files.readString(log, StandardCharsets.UTF_8); Files.deleteIfExists(log);
            throw new IllegalStateException(command.get(0) + " failed:\n" + output);
        }
        return log;
    }
    private static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
