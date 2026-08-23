import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Produces a read-only commit and tracked-file capability for Docker workers. */
final class ContainerPoolContext {
    private ContainerPoolContext() { }

    static Context create(Path root, Path batch, String image) throws Exception {
        require(git(root, "status", "--porcelain", "--untracked-files=all").isBlank(),
                "container pool requires a clean worktree");
        String head = git(root, "rev-parse", "HEAD"), tree = git(root, "rev-parse", "HEAD^{tree}");
        require(head.matches("[0-9a-f]{40,64}") && tree.matches("[0-9a-f]{40,64}"),
                "invalid container Git identity");
        Path directory = batch.resolve("context"); Files.createDirectories(directory);
        String tracked = git(root, "ls-files");
        for (String path : tracked.lines().toList()) require(path.matches("[A-Za-z0-9._/+-]+"),
                "container-incompatible tracked path: " + path);
        Files.writeString(directory.resolve("tracked-files"), tracked + "\n", StandardCharsets.UTF_8);
        String secret = UUID.randomUUID().toString(); Properties lease = new Properties();
        lease.setProperty("schema", "1"); lease.setProperty("backend", "docker");
        lease.setProperty("secret", secret); lease.setProperty("parent.pid",
                Long.toString(ProcessHandle.current().pid())); lease.setProperty("root", "/workspace");
        lease.setProperty("head", head); lease.setProperty("tree", tree);
        lease.setProperty("image.id", image); lease.setProperty("lock.path", "/runtime/locks/official.lock");
        try (var writer = Files.newBufferedWriter(directory.resolve("lease.properties"),
                StandardCharsets.UTF_8)) { lease.store(writer, "Worldline Docker pool capability"); }
        return new Context(directory.toAbsolutePath().normalize(), secret, head, tree);
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        require(process.waitFor(Duration.ofSeconds(60).toMillis(), TimeUnit.MILLISECONDS), "git timed out");
        process.getInputStream().transferTo(output); String text = output.toString(StandardCharsets.UTF_8).trim();
        require(process.exitValue() == 0, "git failed: " + text); return text;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Context(Path directory, String secret, String head, String tree) { }
}
