import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Runs the two-phase, commit-bound qualification of one milestone. */
final class MilestoneCheck {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final String id;
    private final Path build, marker, log;

    private MilestoneCheck(String id) {
        this.id = id; this.build = root.resolve(".worldline/candidates").resolve(id);
        this.marker = build.resolve("qualification-static.properties");
        this.log = root.resolve(".worldline/smoke-logs").resolve(id + ".log");
    }

    static void staticPhase(String id) throws Exception { new MilestoneCheck(id).runStatic(); }
    static void runtimePhase(String id) throws Exception { new MilestoneCheck(id).runRuntime(); }

    private void runStatic() throws Exception {
        State state = cleanState();
        CandidateCheck.execute(id);
        MilestoneContract contract = new MilestoneContract(root, id, build);
        contract.validate();
        Properties values = new Properties();
        values.setProperty("schema", "1"); values.setProperty("id", id);
        values.setProperty("head", state.head); values.setProperty("tree", state.tree);
        values.setProperty("base", state.base); values.setProperty("signature", contract.signature());
        Files.createDirectories(marker.getParent());
        try (java.io.Writer writer = Files.newBufferedWriter(marker, StandardCharsets.UTF_8)) {
            values.store(writer, "Worldline milestone static qualification");
        }
        System.out.println("milestone static phase passed: " + id + " @ " + shortSha(state.head));
    }

    private void runRuntime() throws Exception {
        State state = cleanState();
        Properties staticProof = load(marker);
        require("1".equals(staticProof.getProperty("schema")) && id.equals(staticProof.getProperty("id")),
                "missing static qualification for " + id);
        require(state.head.equals(staticProof.getProperty("head"))
                && state.tree.equals(staticProof.getProperty("tree"))
                && state.base.equals(staticProof.getProperty("base")),
                "milestone changed after static qualification; rerun --milestone " + id);
        MilestoneContract contract = new MilestoneContract(root, id, build);
        contract.validate();
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        String fingerprint = cache.fingerprint(smoke);
        long duration = new SmokeProcess(root).run(smoke);
        contract.validateEvidence(log);
        cache.passed(smoke, fingerprint, duration);
        writeReceipt(state, contract);
        System.out.println("milestone qualified: " + id + " @ " + shortSha(state.head));
    }

    private State cleanState() throws Exception {
        String status = capture(List.of("git", "status", "--porcelain", "--untracked-files=all")).trim();
        require(status.isEmpty(), "final milestone qualification requires a clean committed worktree");
        String head = capture(List.of("git", "rev-parse", "HEAD")).trim();
        String tree = capture(List.of("git", "rev-parse", "HEAD^{tree}")).trim();
        String base = candidateBase(head);
        require(head.matches("[0-9a-f]{40,64}") && tree.matches("[0-9a-f]{40,64}"),
                "could not bind qualification to Git state");
        require(status(List.of("git", "merge-base", "--is-ancestor", base, head)) == 0,
                "milestone base is not an ancestor of its commit");
        return new State(head, tree, base);
    }

    private void writeReceipt(State state, MilestoneContract contract) throws Exception {
        String evidence = HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(log)));
        String json = "{\n  \"schema\": 1,\n  \"id\": \"" + escape(id)
                + "\",\n  \"status\": \"passed\",\n  \"qualified_at\": \"" + Instant.now()
                + "\",\n  \"head\": \"" + state.head + "\",\n  \"tree\": \"" + state.tree
                + "\",\n  \"base\": \"" + state.base + "\",\n  \"signature\": \""
                + escape(contract.signature()) + "\",\n  \"evidence_sha256\": \"" + evidence + "\"\n}\n";
        Path report = root.resolve(".worldline/reports/milestones").resolve(id + ".json");
        Files.createDirectories(report.getParent());
        Path temporary = report.resolveSibling(report.getFileName() + ".tmp");
        Files.writeString(temporary, json, StandardCharsets.UTF_8);
        Files.move(temporary, report, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  milestone receipt: " + relative(report));
    }

    private String candidateBase(String head) throws Exception {
        String value = System.getenv("WORLDLINE_CANDIDATE_BASE");
        if (value != null && !value.isBlank())
            return capture(List.of("git", "rev-parse", value.trim() + "^{commit}")).trim();
        if (status(List.of("git", "rev-parse", "--verify", "refs/remotes/origin/main^{commit}")) == 0)
            return capture(List.of("git", "merge-base", head, "refs/remotes/origin/main")).trim();
        return capture(List.of("git", "rev-parse", head + "^")).trim();
    }

    private String capture(List<String> command) throws Exception {
        Path output = Files.createTempFile("worldline-milestone-git-", ".log");
        Process process = new ProcessBuilder(new ArrayList<>(command)).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
            }
            String text = Files.readString(output, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, String.join(" ", command) + " failed:\n" + text);
            return text;
        } finally { Files.deleteIfExists(output); }
    }

    private int status(List<String> command) throws Exception {
        Process process = new ProcessBuilder(new ArrayList<>(command)).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static Properties load(Path path) throws IOException {
        require(Files.isRegularFile(path), "missing milestone static qualification");
        Properties values = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private static String shortSha(String value) { return value.substring(0, Math.min(12, value.length())); }
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
    private static void destroy(Process process) {
        process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private record State(String head, String tree, String base) {}
}
