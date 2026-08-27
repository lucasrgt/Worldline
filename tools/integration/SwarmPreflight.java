import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Proves the supervised Ox Alpha preflight before any milestone edit is allowed. */
final class SwarmPreflight {
    static final String REQUIRED_SCAR = "NYA-01M0VSCA8F3WSMVW32R9XME7DQ";
    private static final String SEMANTIC_SCAR = "NYA-01M0YZVBKBPB0SB3CJYVQSPNA9";
    private static final Pattern SUMMARY = Pattern.compile(
            "\"(FAILED_GATE|DIRTY_SUSPENDED|RETRYABLE|STRANDED)\"\\s*:\\s*([0-9]+)");
    private static final List<String> OPTIONAL_CONTEXT_ERRORS = List.of(
            "Why This Way is not initialized; run wtw init",
            "rtw: Right This Way is not initialized; run rtw init",
            "Now We Can is not initialized; run nwc init");
    private SwarmPreflight() { }

    static void run(String id, String baseValue, String goal, Path censusValue, Path closureValue,
            Path microWaveValue) throws Exception {
        require(id.matches("m[0-9]+-[a-z0-9-]+"), "invalid milestone id: " + id);
        require(baseValue.matches("[0-9a-f]{40}"), "base must be an exact commit SHA");
        Path root = Path.of("").toAbsolutePath().normalize();
        Path census = censusValue.toAbsolutePath().normalize();
        require(Files.isRegularFile(census), "missing supervisor census: " + census);
        require(blockers(census) == 0, "wave is blocked by unresolved dirty or failed workers");
        String branch = git(root, "branch", "--show-current").trim();
        require(branch.equals("codex/milestone-" + id), "worker branch does not match milestone: " + branch);
        String base = git(root, "rev-parse", "--verify", baseValue + "^{commit}").trim();
        String head = git(root, "rev-parse", "HEAD").trim();
        require(head.equals(base), "worker must start at the exact authorized base");
        require(git(root, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                "worker tree is not initially clean");
        MilestoneObjective objective = MilestoneObjective.load(root, id, goal, base);
        RejectedContractCheck.requireAllowed(root, id, goal);
        SwarmMicroWave.verify(root, microWaveValue, closureValue, id, base);
        String worktrees = git(root, "worktree", "list", "--porcelain");
        require(worktrees.lines().filter(line -> line.equals("worktree " + slash(root.toString()))
                || line.equals("worktree " + root)).count() == 1, "worker worktree is not exclusively registered");
        Path agents = root.resolve("AGENTS.md"), workflow = root.resolve("docs/ENGINEERING_WORKFLOW.md");
        String agentsText = Files.readString(agents, StandardCharsets.UTF_8);
        String workflowText = Files.readString(workflow, StandardCharsets.UTF_8);
        require(!agentsText.isBlank() && !workflowText.isBlank(), "required engineering documents are empty");
        Path prompt = root.resolve("coordination/swarm/OX_ALPHA_PROMPT.md");
        String promptText = Files.readString(prompt, StandardCharsets.UTF_8);
        require(promptText.contains(REQUIRED_SCAR)
                && promptText.contains("Nested task/explore/subagent delegation is forbidden")
                && promptText.contains("A milestone is one coherent mini-subsystem"),
                "worker base prompt does not enforce the applicable scar");
        SwarmProcess.Result contextResult = SwarmProcess.capture(root,
                List.of("csm", "context", "--task", goal, "--path", "."), 300);
        require(contextAccepted(contextResult), "csm context failed outside the optional-store allowance");
        String context = contextResult.output();
        String recall = SwarmProcess.output(root, List.of("csm", "nya", "recall", "--task", goal,
                "--path", "smokes/" + id, "--path", "tools/smoke", "--path", "modules/testkit"), 300);
        String semanticRecall = SwarmProcess.output(root, List.of("csm", "nya", "recall", "--task",
                goal + " Required applicable scar " + SEMANTIC_SCAR,
                "--path", "coordination/swarm", "--path", "tools/integration"), 300);
        require(recall.contains(REQUIRED_SCAR), "required NYA scar was absent from recall");
        require(semanticRecall.contains(SEMANTIC_SCAR),
                "semantic exclusion scar was absent from recall");
        Path report = root.resolve(".worldline/reports/swarm/preflight-" + id + ".json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, "{\n  \"schema\":1,\n  \"id\":\"" + id
                + "\",\n  \"created\":\"" + Instant.now() + "\",\n  \"base\":\"" + base
                + "\",\n  \"head\":\"" + head + "\",\n  \"agents_sha256\":\""
                + SwarmEvidenceArchive.sha256(agents) + "\",\n  \"workflow_sha256\":\""
                + SwarmEvidenceArchive.sha256(workflow) + "\",\n  \"prompt_sha256\":\""
                + SwarmEvidenceArchive.sha256(prompt) + "\",\n  \"census_sha256\":\""
                + SwarmEvidenceArchive.sha256(census) + "\",\n  \"closure_sha256\":\""
                + SwarmEvidenceArchive.sha256(closureValue.toAbsolutePath().normalize())
                + "\",\n  \"micro_wave_sha256\":\""
                + SwarmEvidenceArchive.sha256(microWaveValue.toAbsolutePath().normalize())
                + "\",\n  \"objective_sha256\":\""
                + SwarmEvidenceArchive.sha256(objective.path())
                + "\",\n  \"goal_sha256\":\""
                + shaText(goal)
                + "\",\n  \"context_sha256\":\""
                + shaText(context) + "\",\n  \"recall_sha256\":\""
                + shaText(recall + semanticRecall)
                + "\",\n  \"required_scar\":\"" + REQUIRED_SCAR
                + "\",\n  \"nested_delegation\":\"forbidden\",\n  \"status\":\"PASS\"\n}\n",
                StandardCharsets.UTF_8);
        System.out.println("Ox Alpha preflight PASS: " + id);
        System.out.println("  required scar: " + REQUIRED_SCAR);
        System.out.println("  report: " + report);
    }

    private static int blockers(Path census) throws Exception {
        Matcher matcher = SUMMARY.matcher(Files.readString(census, StandardCharsets.UTF_8));
        int result = 0; while (matcher.find()) result += Integer.parseInt(matcher.group(2));
        return result;
    }
    static void selfTest() {
        require(contextAccepted(new SwarmProcess.Result(0, "complete", "")),
                "successful CSM context was rejected");
        String partial = "== nya ==\n" + REQUIRED_SCAR;
        require(contextAccepted(new SwarmProcess.Result(1, partial,
                OPTIONAL_CONTEXT_ERRORS.get(0) + "\n")), "valid partial CSM context was rejected");
        require(!contextAccepted(new SwarmProcess.Result(1, "== nya ==",
                OPTIONAL_CONTEXT_ERRORS.get(0))), "partial context without mandatory scar passed");
        require(!contextAccepted(new SwarmProcess.Result(1, partial, "unknown failure\n")),
                "unknown CSM context failure passed");
    }
    private static boolean contextAccepted(SwarmProcess.Result result) {
        if (result.exitCode() == 0) {
            return true;
        }
        if (result.exitCode() != 1 || !result.stdout().contains("== nya ==")
                || !result.stdout().contains(REQUIRED_SCAR)) {
            return false;
        }
        List<String> errors = result.stderr().lines().filter(line -> !line.isBlank()).toList();
        return !errors.isEmpty() && errors.stream().allMatch(OPTIONAL_CONTEXT_ERRORS::contains);
    }
    private static String shaText(String text) throws Exception {
        Path path = Files.createTempFile("worldline-swarm-text-", ".txt");
        try { Files.writeString(path, text, StandardCharsets.UTF_8); return SwarmEvidenceArchive.sha256(path); }
        finally { Files.deleteIfExists(path); }
    }
    private static String git(Path root, String... arguments) throws Exception {
        return SwarmProcess.output(root, List.of(arguments), 120);
    }
    private static String slash(String value) { return value.replace('\\', '/'); }
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
