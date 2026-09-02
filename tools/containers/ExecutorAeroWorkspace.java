import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Disposable exact-commit Worldline and pinned external Aero worktrees. */
final class ExecutorAeroWorkspace implements AutoCloseable {
    private static final long MAX_ARTIFACT_BYTES = 64L << 20;
    private final Path source;
    private final Path donor;
    private final Path root;
    private final Path aero;
    private boolean worldlineAttached;
    private boolean aeroAttached;

    private ExecutorAeroWorkspace(Path source, Path donor, Path root) {
        this.source = source; this.donor = donor; this.root = root;
        this.aero = root.resolve("local/candidates/aero-model-lib");
    }

    static ExecutorAeroWorkspace open(Path source, String label, Path donor,
            String revision) throws Exception {
        require(label.matches("legacy|rust"), "invalid Aero workspace label");
        require(Files.exists(donor.resolve(".git"), LinkOption.NOFOLLOW_LINKS),
                "Aero donor repository is absent");
        require(revision.matches("[0-9a-f]{40}"), "invalid Aero revision");
        Path parent = source.getParent().toAbsolutePath().normalize();
        Path target = parent.resolve(source.getFileName() + "-executor-aero-"
                + ProcessHandle.current().pid() + "-" + label).normalize();
        require(target.getParent().equals(parent) && !Files.exists(target),
                "executor Aero workspace already exists");
        ExecutorAeroWorkspace workspace = new ExecutorAeroWorkspace(source, donor, target);
        try {
            workspace.git(source, List.of("worktree", "add", "--detach", target.toString(), "HEAD"), 180);
            workspace.worldlineAttached = true;
            workspace.copyArtifacts();
            Files.createDirectories(workspace.aero.getParent());
            workspace.git(donor, List.of("worktree", "add", "--detach",
                    workspace.aero.toString(), revision), 300);
            workspace.aeroAttached = true;
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

    Path authorize(Path lock, RuntimeLeaseIdentity lease) throws Exception {
        String secret = UUID.randomUUID().toString();
        Path token = root.resolve(".worldline/runtime-fabric/executor-aero.properties");
        run(root, List.of(java(), root.resolve("tools/containers/PoolLeaseToken.java").toString(),
                "create", token.toString(), secret, Long.toString(ProcessHandle.current().pid()),
                lock.toString(), "executor-differential", lease.pid(), lease.started()), 60);
        return token;
    }

    static void selfTest() throws Exception {
        Path base = Files.createTempDirectory("worldline-aero-workspace-").toAbsolutePath().normalize();
        try {
            Path source = base.resolve("source.jar"), target = base.resolve("target.jar");
            Files.write(source, new byte[] {1, 2, 3}); copyArtifact(source, target);
            require(Files.size(target) == 3L, "Aero artifact copy drift");
        } finally {
            try (var paths = Files.walk(base)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList())
                    Files.deleteIfExists(path);
            }
        }
    }

    @Override public void close() {
        try {
            if (aeroAttached) {
                aeroAttached = false;
                try { git(donor, List.of("worktree", "remove", "--force", aero.toString()), 300); }
                catch (Exception ignored) { }
                git(donor, List.of("worktree", "prune"), 60);
            }
            if (worldlineAttached) {
                worldlineAttached = false;
                try { git(source, List.of("worktree", "remove", "--force", root.toString()), 300); }
                catch (Exception ignored) { }
                if (Files.exists(root)) run(source, List.of(java(), source.resolve(
                        "tools/harness/SafeTreeDelete.java").toString(), root.toString(),
                        source.getParent().toString()), 180);
                git(source, List.of("worktree", "prune"), 60);
            }
            require(!Files.exists(aero) && !Files.exists(root),
                    "executor Aero workspace cleanup was incomplete");
        } catch (Exception error) {
            throw new IllegalStateException("executor Aero workspace cleanup failed", error);
        }
    }

    private void copyArtifacts() throws Exception {
        Path artifacts = source.resolve("local/artifacts");
        for (String name : List.of("minecraft-b1.7.3-client.jar", "minecraft-b1.7.3-server.jar"))
            copyArtifact(artifacts.resolve(name), root.resolve("local/artifacts").resolve(name));
    }

    private static void copyArtifact(Path source, Path target) throws Exception {
        require(Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(source),
                "official artifact is absent or linked: " + source);
        long bytes = Files.size(source);
        require(bytes > 0L && bytes <= MAX_ARTIFACT_BYTES, "official artifact copy budget exceeded");
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        require(Files.size(target) == bytes, "official artifact copy was incomplete");
    }

    private void git(Path repository, List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(arguments);
        run(repository, command, seconds);
    }

    private static void run(Path directory, List<String> command, int seconds) throws Exception {
        Path log = Files.createTempFile("worldline-executor-aero-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                terminate(process); throw new IllegalStateException(command.get(0) + " timed out");
            }
            require(process.exitValue() == 0, command.get(0) + " failed:\n" + Files.readString(log));
        } catch (InterruptedException error) {
            terminate(process); Thread.currentThread().interrupt(); throw error;
        } finally { Files.deleteIfExists(log); }
    }

    private static void terminate(Process process) throws Exception {
        List<ProcessHandle> descendants = process.descendants().sorted(
                java.util.Comparator.comparingInt((ProcessHandle child) ->
                        depth(child, process.pid())).reversed()).toList();
        descendants.forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
        require(process.waitFor(10, TimeUnit.SECONDS), "timed-out process did not terminate");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (descendants.stream().anyMatch(ProcessHandle::isAlive) && System.nanoTime() < deadline)
            Thread.sleep(20L);
        require(descendants.stream().noneMatch(ProcessHandle::isAlive),
                "timed-out process descendants remained active");
    }

    private static int depth(ProcessHandle process, long ancestor) {
        int value = 0;
        for (ProcessHandle current = process; current.pid() != ancestor; value++)
            current = current.parent().orElseThrow();
        return value;
    }

    private static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
