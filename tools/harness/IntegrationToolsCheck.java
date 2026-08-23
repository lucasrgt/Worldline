import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Compiles and exercises the versioned integration/worktree coordination tools. */
final class IntegrationToolsCheck {
    private IntegrationToolsCheck() {}

    static void execute(Path root, Path build) throws Exception {
        Path output = build.resolve("integration-tools");
        Files.createDirectories(output);
        List<String> compile = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "21", "-Xlint:all,-options", "-Werror", "-d", output.toString()));
        try (Stream<Path> paths = Files.list(root.resolve("tools/integration"))) {
            compile.addAll(paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .map(Path::toString).collect(Collectors.toList()));
        }
        require(run(root, compile, 120) == 0, "integration tools did not compile");
        selfTest(output);
        System.out.println("  integration tools: compiled and self-tested");
    }

    private static void selfTest(Path classes) throws Exception {
        Path repository = Files.createTempDirectory("worldline-integration-test-");
        try {
            git(repository, "init", "--quiet");
            git(repository, "config", "user.email", "worldline@example.invalid");
            git(repository, "config", "user.name", "Worldline Test");
            Files.writeString(repository.resolve("README.md"), "base\n", StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "base");
            git(repository, "branch", "base");
            candidate(repository, "one", "m1-one");
            git(repository, "switch", "--quiet", "base");
            candidate(repository, "two", "m2-two");
            int valid = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only",
                    "m1-one=one", "m2-two=two"), 60);
            require(valid == 0, "valid integration train was rejected");
            git(repository, "switch", "--quiet", "-c", "bad", "base");
            Files.writeString(repository.resolve("README.md"), "bad\n", StandardCharsets.UTF_8);
            Path own = repository.resolve("smokes/m3-bad"); Files.createDirectories(own);
            Files.writeString(own.resolve("smoke.properties"), "id=m3-bad\n", StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "bad");
            int invalid = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only", "m3-bad=bad"), 60);
            require(invalid != 0, "coordinator-owned file change was accepted");
            int reconcile = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only", "--reconcile",
                    "full-integration=bad"), 60);
            require(reconcile == 0, "consolidated reconciliation train was rejected");
            int triage = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "triage", "--base", "base"), 60);
            require(triage == 0 && Files.readString(repository.resolve(
                    ".worldline/reports/branches.json")).contains("\"one-unique\""),
                    "branch triage report was not generated");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "--self-test"), 60) == 0,
                    "worktree private cleanup self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "QualificationLockMerge", "--self-test"), 60) == 0,
                    "qualification lock merge self-test failed");
        } finally { delete(repository); }
    }

    private static void candidate(Path repository, String branch, String id) throws Exception {
        git(repository, "switch", "--quiet", "-c", branch, "base");
        Path directory = repository.resolve("smokes").resolve(id); Files.createDirectories(directory);
        Files.writeString(directory.resolve("smoke.properties"), "id=" + id + "\n", StandardCharsets.UTF_8);
        git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", branch);
    }

    private static void git(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        require(run(directory, command, 60) == 0, "git failed: " + String.join(" ", arguments));
    }

    private static int run(Path directory, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.destroyForcibly(); throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static void delete(Path target) throws Exception {
        if (!Files.exists(target)) return;
        try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                path.toFile().setWritable(true);
                Files.deleteIfExists(path);
            }
        }
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
