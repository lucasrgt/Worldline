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
        compile.add(root.resolve("tools/harness/SafeTreeDelete.java").toString());
        compile.add(root.resolve("tools/harness/MiniJson.java").toString());
        require(run(root, compile, 120) == 0, "integration tools did not compile");
        require(run(root, List.of(javaTool("java"), root.resolve(
                "tools/integration/WorktreeLifecycleLauncher.java").toString(), "--self-test"), 180) == 0,
                "worktree lifecycle source launcher did not compile its closure");
        require(run(root, List.of(javaTool("java"), root.resolve(
                "tools/integration/OxAlphaLauncher.java").toString(), "--self-test"), 180) == 0,
                "Ox Alpha source launcher did not compile its closure");
        require(run(root, List.of(javaTool("java"), root.resolve(
                "tools/integration/LegacyRetryControlLauncher.java").toString(),
                "--self-test"), 180) == 0,
                "legacy retry control source launcher did not compile its closure");
        require(run(root, List.of(javaTool("java"), root.resolve(
                "tools/integration/LegacyProfilerInstallerLauncher.java").toString(),
                "--self-test"), 180) == 0, "legacy profiler installer self-test failed");
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
            Files.writeString(repository.resolve(".gitignore"), ".worldline/\n", StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "base");
            git(repository, "branch", "base");
            candidate(repository, "codex/milestone-m1-one", "m1-one");
            git(repository, "switch", "--quiet", "base");
            candidate(repository, "codex/milestone-m2-two", "m2-two");
            int valid = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only",
                    "m1-one=codex/milestone-m1-one", "m2-two=codex/milestone-m2-two"), 60);
            require(valid == 0, "valid integration train was rejected");
            git(repository, "switch", "--quiet", "-c", "codex/milestone-m3-bad", "base");
            Files.writeString(repository.resolve("README.md"), "bad\n", StandardCharsets.UTF_8);
            Path own = repository.resolve("smokes/m3-bad"); Files.createDirectories(own);
            Files.writeString(own.resolve("smoke.properties"), "id=m3-bad\n", StandardCharsets.UTF_8);
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "bad");
            int invalid = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only",
                    "m3-bad=codex/milestone-m3-bad"), 60);
            require(invalid != 0, "coordinator-owned file change was accepted");
            git(repository, "branch", "codex/train-full-integration");
            int reconcile = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only", "--reconcile",
                    "full-integration=codex/train-full-integration"), 60);
            require(reconcile == 0, "consolidated reconciliation train was rejected");
            git(repository, "branch", "codex/experiment-m3-bad");
            int experiment = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only",
                    "m3-bad=codex/experiment-m3-bad"), 60);
            require(experiment != 0, "experiment branch was accepted for integration");
            git(repository, "switch", "--quiet", "-c", "codex/fix-m4-runtime", "base");
            Path runtime = repository.resolve("tools/smoke/FixCycle.java");
            Files.createDirectories(runtime.getParent()); Files.writeString(runtime, "final class FixCycle {}\n");
            Path fix = repository.resolve("smokes/m4-runtime"); Files.createDirectories(fix);
            Files.writeString(fix.resolve("smoke.properties"), "id=m4-runtime\n");
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "Fix runtime fixture");
            int missingScar = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only",
                    "m4-runtime=codex/fix-m4-runtime"), 60);
            require(missingScar != 0, "runtime fix without an NYA scar was accepted");
            Path scar = repository.resolve(".csm/nya/scars/NYA-TEST.toml");
            Files.createDirectories(scar.getParent()); Files.writeString(scar, "schema = 1\n");
            git(repository, "add", "."); git(repository, "commit", "--quiet", "--amend", "--no-edit");
            int withScar = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "IntegrationTrain", "--base", "base", "--plan-only",
                    "m4-runtime=codex/fix-m4-runtime"), 60);
            require(withScar == 0, "runtime fix with an NYA scar was rejected");
            Path experimentTree = repository.getParent().resolve(repository.getFileName() + "-experiment");
            git(repository, "worktree", "add", "--quiet", experimentTree.toString(),
                    "codex/experiment-m3-bad");
            int missingDeferment = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "audit", "--base", "base"), 60);
            require(missingDeferment != 0, "experiment without an NWC deferment was accepted");
            git(repository, "worktree", "remove", "--force", experimentTree.toString());
            git(repository, "switch", "--quiet", "base");
            candidate(repository, "codex/milestone-m5-reconciled", "m5-reconciled");
            String reconciledHead = output(repository, "rev-parse", "HEAD").trim();
            git(repository, "switch", "--quiet", "base");
            Path trainLock = repository.resolve("smokes/train-reconciliation.lock");
            Files.createDirectories(trainLock.getParent());
            Files.writeString(trainLock, "smoke.m5-reconciled.kind=milestone\n"
                    + "smoke.m5-reconciled.receipt.head=" + reconciledHead + "\n");
            git(repository, "add", "."); git(repository, "commit", "--quiet", "-m", "seal train receipt");
            Path reconciledTree = repository.getParent().resolve(repository.getFileName() + "-reconciled");
            git(repository, "worktree", "add", "--quiet", reconciledTree.toString(),
                    "codex/milestone-m5-reconciled");
            int receiptAudit = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "audit", "--base", "base"), 60);
            require(receiptAudit == 0 && Files.readString(repository.resolve(
                    ".worldline/reports/worktrees.json")).contains("\"head\":\"" + reconciledHead
                    + "\",\"branch\":\"codex/milestone-m5-reconciled\",\"exists\":true,"
                    + "\"dirty\":false,\"integrated\":true"), "train receipt did not integrate worktree");
            git(repository, "worktree", "remove", "--force", reconciledTree.toString());
            git(repository, "branch", "codex/milestone-m6-husk", "base");
            Path huskTree = repository.getParent().resolve(repository.getFileName() + "-husk");
            git(repository, "worktree", "add", "--quiet", huskTree.toString(), "codex/milestone-m6-husk");
            Path dirtyMilestone = huskTree.resolve("smokes/m6-husk/smoke.properties");
            Files.createDirectories(dirtyMilestone.getParent()); Files.writeString(dirtyMilestone, "id=m6-husk\n");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "audit", "--base", "base"), 60) == 0
                            && Files.readString(repository.resolve(".worldline/reports/worktrees.json"))
                                    .contains("\"branch\":\"codex/milestone-m6-husk\",\"exists\":true,"
                                            + "\"dirty\":true,\"integrated\":true,\"husk\":true,"
                                            + "\"retracted\":false,"
                                            + "\"milestoneDirty\":true,\"archiveEligible\":false"),
                    "husk or milestone-dirty worktree was silently archive-eligible");
            git(repository, "worktree", "remove", "--force", huskTree.toString());
            int triage = run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "triage", "--base", "base"), 60);
            require(triage == 0 && Files.readString(repository.resolve(
                    ".worldline/reports/branches.json")).contains("\"one-unique\"")
                    && Files.readString(repository.resolve(".worldline/reports/branches.json"))
                            .contains("\"receipt_contained\":true"),
                    "branch triage report was not generated");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "BranchTriage", "--self-test"), 60) == 0,
                    "branch triage row parser self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "WorktreeLifecycle", "--self-test"), 60) == 0,
                    "worktree private cleanup self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "QualificationLockMerge", "--self-test"), 60) == 0,
                    "qualification lock merge self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "SwarmHandoff", "--self-test"), 60) == 0, "swarm handoff self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "SwarmLoop", "--self-test"), 60) == 0, "swarm loop self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "OxAlphaWorker", "--self-test"), 60) == 0,
                    "Ox Alpha launcher self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "LegacyRetryAdoptionSelfTest"), 60) == 0,
                    "legacy retry adoption self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "OxAlphaInfrastructureRolloverSelfTest"), 60) == 0,
                    "Ox Alpha infrastructure rollover self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "OpenCodeSessionExportSelfTest"), 60) == 0,
                    "OpenCode session export self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "SwarmDashboard", "--self-test"), 60) == 0, "swarm dashboard self-test failed");
            require(run(repository, List.of(javaTool("java"), "-cp", classes.toString(),
                    "ChangelogPartition", "--self-test"), 60) == 0,
                    "changelog partition self-test failed");
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

    private static String output(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).start();
        String value = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
        return value;
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
        SafeTreeDelete.delete(target);
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
