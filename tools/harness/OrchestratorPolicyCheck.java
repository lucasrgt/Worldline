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
            Files.writeString(milestone.resolve("smoke.properties"),
                    "id=m1-one\nrunner.source=tools/smoke/TestCycle.java\n", StandardCharsets.UTF_8);
            Path runner = repository.resolve("tools/smoke/TestCycle.java");
            Files.createDirectories(runner.getParent());
            Files.writeString(runner, "final class TestCycle {}\n", StandardCharsets.UTF_8);
            Path process = repository.resolve("tools/harness/SmokeProcess.java");
            Files.createDirectories(process.getParent());
            Files.writeString(process, "final class SmokeProcess {}\n", StandardCharsets.UTF_8);
            Files.writeString(repository.resolve("harness.properties"), "java.release=8\n",
                    StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "candidate");
            String head = output(repository, "rev-parse", "HEAD").trim();
            Path plan = repository.resolve(".worldline/reports/integration-plan.json");
            Files.createDirectories(plan.getParent());
            Files.writeString(plan, "{\"base\":\"" + base + "\",\"verified\":true,\"candidates\":["
                    + "{\"id\":\"m1-one\",\"head\":\"" + head + "\"}]}\n", StandardCharsets.UTF_8);
            qualifySmoke(repository);
            OrchestratorCheck.Context context = OrchestratorCheck.preflight(repository);
            OrchestratorCheck.validate(repository, context);
            Path authorization = repository.resolve(".worldline/reports/orchestrator-push.json");
            require(!Files.exists(authorization), "validation emitted push authorization");
            OrchestratorCheck.authorize(repository, context, false);
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
        SafeTreeDelete.delete(target);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static void qualifySmoke(Path repository) throws Exception {
        SmokeDiscovery.Entry entry = SmokeDiscovery.require(repository, "m1-one");
        SmokeReceiptCache cache = new SmokeReceiptCache(repository);
        Path log = repository.resolve(".worldline/smoke-logs/m1-one.log");
        Files.createDirectories(log.getParent()); Files.writeString(log, "PASS\n", StandardCharsets.UTF_8);
        cache.passed(entry, cache.fingerprint(entry), 1L); cache.finish(1);
    }
    private interface Action { void run() throws Exception; }
}
