import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Exercises commit-bound orchestrator authorization in an isolated Git repository. */
final class OrchestratorPolicyCheck {
    private OrchestratorPolicyCheck() {}

    static void execute() throws Exception {
        Path repository = Files.createTempDirectory("worldline-orchestrator-policy-");
        try {
            git(repository, "init", "--quiet");
            git(repository, "config", "user.email", "worldline@example.invalid");
            git(repository, "config", "user.name", "Worldline Test");
            Files.writeString(repository.resolve(".gitignore"), ".worldline/\n", StandardCharsets.UTF_8);
            Files.writeString(repository.resolve("base.txt"), "base\n", StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "base");
            String base = output(repository, "rev-parse", "HEAD").trim();
            Path milestone = repository.resolve("smokes/m1-one"); Files.createDirectories(milestone);
            Files.writeString(milestone.resolve("smoke.properties"), "id=m1-one\n", StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "candidate");
            String head = output(repository, "rev-parse", "HEAD").trim();
            Path plan = repository.resolve(".worldline/reports/integration-plan.json");
            Files.createDirectories(plan.getParent());
            Files.writeString(plan, "{\"base\":\"" + base + "\",\"verified\":true,\"candidates\":["
                    + "{\"id\":\"m1-one\",\"head\":\"" + head + "\"}]}\n", StandardCharsets.UTF_8);
            OrchestratorCheck.Context context = OrchestratorCheck.preflight(repository);
            OrchestratorCheck.qualify(repository, context, false);
            PushCheck.verify(repository, head, "refs/heads/main");
            reject(() -> PushCheck.verify(repository, base, "refs/heads/main"),
                    "receipt accepted a different commit");
            Files.writeString(plan, " ", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
            reject(() -> PushCheck.verify(repository, head, "refs/heads/main"),
                    "receipt accepted a changed integration plan");
            Files.writeString(repository.resolve("dirty.txt"), "dirty\n", StandardCharsets.UTF_8);
            reject(() -> OrchestratorCheck.preflight(repository), "dirty orchestrator worktree was accepted");
            System.out.println("  orchestrator policy: commit-bound handoff self-test passed");
        } finally { delete(repository); }
    }

    private static String output(Path root, String... arguments) throws Exception {
        Path log = Files.createTempFile("worldline-policy-git-", ".log");
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                    "git failed: " + String.join(" ", arguments));
            return Files.readString(log, StandardCharsets.UTF_8);
        } finally { Files.deleteIfExists(log); }
    }

    private static void git(Path root, String... arguments) throws Exception { output(root, arguments); }
    private static void reject(Action action, String message) throws Exception {
        try { action.run(); throw new IllegalStateException(message); }
        catch (IllegalStateException expected) {
            if (message.equals(expected.getMessage())) throw expected;
        }
    }

    private static void delete(Path target) throws Exception {
        if (!Files.exists(target)) return;
        try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                path.toFile().setWritable(true); Files.deleteIfExists(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private interface Action { void run() throws Exception; }
}
