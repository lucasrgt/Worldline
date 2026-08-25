import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Binds an audited integration train and repository gate to one orchestrator-owned commit. */
final class OrchestratorCheck {
    private OrchestratorCheck() {}

    static Context preflight(Path root) throws Exception {
        require(capture(root, List.of("git", "status", "--porcelain", "--untracked-files=all")).isBlank(),
                "orchestrator qualification requires a clean committed worktree");
        Path plan = root.resolve(".worldline/reports/integration-plan.json");
        require(Files.isRegularFile(plan), "missing qualified integration plan");
        String json = Files.readString(plan, StandardCharsets.UTF_8);
        Map<String, Object> document = MiniJson.object(json);
        require(MiniJson.bool(document, "verified"), "integration plan is not qualified");
        String base = MiniJson.string(document, "base");
        require(base.matches("[0-9a-f]{40,64}"), "invalid integration base");
        List<String> candidates = new java.util.ArrayList<>();
        for (Object value : MiniJson.array(document, "candidates")) {
            String head = MiniJson.string(MiniJson.asObject(value, "candidate"), "head");
            require(head.matches("[0-9a-f]{40,64}"), "invalid candidate head");
            candidates.add(head);
        }
        require(!candidates.isEmpty(), "integration plan has no candidates");
        String head = capture(root, List.of("git", "rev-parse", "HEAD")).trim();
        String tree = capture(root, List.of("git", "rev-parse", "HEAD^{tree}")).trim();
        require(ancestor(root, base, head), "integration base is not an ancestor of orchestrator HEAD");
        for (String candidate : candidates) require(ancestor(root, candidate, head),
                "qualified candidate is not integrated: " + shortSha(candidate));
        String smoke = SmokeReceiptCache.validateSuite(root, head, tree);
        return new Context(head, tree, base, digest(plan), smoke, candidates.size());
    }

    static void validate(Path root, Context expected) throws Exception {
        Context actual = preflight(root);
        require(expected.equals(actual), "integration state changed while the orchestrator gate was running");
    }

    static void authorize(Path root, Context actual, boolean announce) throws Exception {
        String json = "{\n  \"schema\": 1,\n  \"status\": \"passed\",\n  \"qualified_at\": \""
                + Instant.now() + "\",\n  \"head\": \"" + actual.head + "\",\n  \"tree\": \""
                + actual.tree + "\",\n  \"base\": \"" + actual.base
                + "\",\n  \"integration_plan_sha256\": \"" + actual.planDigest
                + "\",\n  \"smoke_suite_sha256\": \"" + actual.smokeDigest
                + "\",\n  \"candidate_count\": " + actual.candidateCount + "\n}\n";
        Path receipt = root.resolve(".worldline/reports/orchestrator-push.json");
        Files.createDirectories(receipt.getParent());
        Path temporary = receipt.resolveSibling(receipt.getFileName() + ".tmp");
        Files.writeString(temporary, json, StandardCharsets.UTF_8);
        Files.move(temporary, receipt, StandardCopyOption.REPLACE_EXISTING);
        if (announce) System.out.println("  orchestrator push receipt: " + root.relativize(receipt));
    }

    static void revoke(Path root) throws IOException {
        Files.deleteIfExists(root.resolve(".worldline/reports/orchestrator-push.json"));
    }

    static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static boolean ancestor(Path root, String base, String head) throws Exception {
        return status(root, List.of("git", "merge-base", "--is-ancestor", base, head)) == 0;
    }

    private static String capture(Path root, List<String> command) throws Exception {
        Path log = Files.createTempFile("worldline-orchestrator-git-", ".log");
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, String.join(" ", command) + " failed:\n" + output);
            return output;
        } finally { Files.deleteIfExists(log); }
    }

    private static int status(Path root, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static String shortSha(String value) { return value.substring(0, Math.min(12, value.length())); }
    private static void destroy(Process process) {
        process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record Context(String head, String tree, String base, String planDigest,
            String smokeDigest, int candidateCount) {}
}
