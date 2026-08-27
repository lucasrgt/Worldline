import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Seals one zero-event provider failure without consuming another contract attempt. */
public final class OxAlphaInfrastructureRollover {
    private static final String EMPTY_SHA =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private OxAlphaInfrastructureRollover() {
    }

    public static void main(String[] arguments) {
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Path receipt = create(root, CreateRequest.parse(arguments));
            System.out.println("Ox Alpha infrastructure rollover PASS: " + receipt);
            System.out.println("  receipt sha256: " + sha(receipt));
        } catch (Exception error) {
            System.err.println("Ox Alpha infrastructure rollover failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Path create(Path root, CreateRequest request) throws Exception {
        Path exactRoot = root.toAbsolutePath().normalize();
        require(request.id.matches("m[0-9]+-[a-z0-9-]+"), "invalid milestone id");
        require(request.controlBase.matches("[0-9a-f]{40}"), "invalid control base");
        require(OxAlphaProviderFailure.validSession(request.session), "invalid session");
        require(OxAlphaProfile.allowedFallbackModel(request.authorizedModel),
                "rollover model is not allowlisted");
        Path providerLog = canonicalProviderLog();
        require(request.providerLog.equals(providerLog)
                && request.providerLog.toRealPath().equals(providerLog),
                "provider log is not the canonical OpenCode log");
        require(git(exactRoot, "branch", "--show-current").trim()
                .equals("codex/milestone-" + request.id), "wrong milestone branch");
        String head = git(exactRoot, "rev-parse", "HEAD").trim();
        require(head.equals(request.controlBase), "rollover must be sealed at the exact clean control base");
        require(git(exactRoot, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                "rollover requires a clean worktree");
        Path reports = exactRoot.resolve(".worldline/reports/swarm");
        Path prior = canonical(reports, "opencode-" + request.id + "-checkpoint-attempt2.json",
                request.priorReceipt);
        Path adoption = canonical(reports, "legacy-retry-adoption-" + request.id + ".json",
                request.adoptionReceipt);
        Path sessionEvidence = canonical(reports, "opencode-session-" + request.session + ".json",
                request.sessionEvidence);
        requireHash(prior, request.priorSha, "prior launch receipt");
        requireHash(adoption, request.adoptionSha, "legacy adoption receipt");
        requireHash(sessionEvidence, request.sessionEvidenceSha, "session export");
        OpenCodeSessionExport.validateEvidence(Files.readString(sessionEvidence,
                StandardCharsets.UTF_8), request.session, exactRoot);
        Map<String, Object> priorJson = json(prior);
        String priorModel = string(priorJson, "model");
        require(OxAlphaProfile.allowedModel(priorModel)
                && !priorModel.equals(request.authorizedModel),
                "prior and authorized models are not distinct and allowlisted");
        validatePrior(exactRoot, request, priorJson, reports, priorModel);
        Adoption adopted = validateAdoption(exactRoot, request, adoption);
        require(!adopted.mode.equals("process-recovery")
                || number(priorJson, "wall_seconds") <= adopted.recoveryTimeout,
                "original process-recovery launch exceeded its adoption budget");
        var occurrence = OxAlphaProviderOccurrence.capture(request.providerLog, request.session,
                priorModel, exactRoot, adopted.mode,
                Instant.parse(string(priorJson, "started")),
                Instant.parse(string(priorJson, "finished")));
        require(occurrence.sourceSha().equalsIgnoreCase(request.providerLogSha),
                "provider log snapshot SHA-256 drifted from supervisor input");
        String tree = git(exactRoot, "rev-parse", "HEAD^{tree}").trim();
        return OxAlphaRolloverReceipt.publish(reports, request, head, tree, prior, adoption,
                sessionEvidence, priorModel, adopted.mode, occurrence);
    }

    static void validate(Path root, OxAlphaRequest request, boolean fallback,
            String selectedModel) throws Exception {
        require(request.phase().equals("checkpoint") && request.attempt() == 2
                && request.launch() == 3, "rollover only authorizes checkpoint attempt 2 launch 3");
        require(request.session() != null && fallback,
                "rollover requires the exact recovered session on fallback");
        Path expected = root.resolve(".worldline/reports/swarm/infrastructure-rollover-"
                + request.id() + "-attempt2-launch3.json").normalize();
        require(Path.of(request.rolloverReceipt()).toAbsolutePath().normalize().equals(expected)
                && Files.isRegularFile(expected), "rollover receipt is not at its canonical path");
        requireHash(expected, request.rolloverSha(), "rollover receipt");
        Map<String, Object> json = json(expected);
        String head = git(root, "rev-parse", "HEAD").trim();
        String tree = git(root, "rev-parse", "HEAD^{tree}").trim();
        require(string(json, "id").equals(request.id())
                && integer(json, "attempt") == 2 && integer(json, "prior_launch") == 2
                && integer(json, "authorized_launch") == 3
                && string(json, "session").equals(request.session())
                && string(json, "authorized_model").equals(selectedModel)
                && !string(json, "prior_model").equals(selectedModel)
                && string(json, "control_base").equals(request.controlBase())
                && string(json, "head").equals(head) && string(json, "tree").equals(tree)
                && string(json, "adoption_sha256").equalsIgnoreCase(request.adoptionSha())
                && string(json, "classification").equals("provider-usage-limit")
                && string(json, "status").equals("PASS"), "rollover receipt drifted");
        Path seal = root.resolve(".worldline/reports/swarm/infrastructure-rollover-"
                + request.id() + "-attempt2-launch3.seal.claim").normalize();
        require(Path.of(string(json, "seal_claim")).toAbsolutePath().normalize().equals(seal),
                "rollover seal claim path drifted");
        requireHash(seal, string(json, "seal_claim_sha256"), "rollover seal claim");
        OxAlphaProviderOccurrence.validateSealed(json, request.session(),
                string(json, "prior_model"), root, string(json, "adoption_mode"));
        Path prior = Path.of(string(json, "prior_receipt")).toAbsolutePath().normalize();
        Path sessionEvidence = Path.of(string(json, "session_evidence")).toAbsolutePath().normalize();
        Path reports = root.resolve(".worldline/reports/swarm").normalize();
        require(prior.equals(reports.resolve("opencode-" + request.id()
                + "-checkpoint-attempt2.json"))
                && sessionEvidence.equals(reports.resolve("opencode-session-"
                        + request.session() + ".json")), "rollover input evidence path drifted");
        requireHash(prior, string(json, "prior_receipt_sha256"), "prior launch receipt");
        requireHash(sessionEvidence, string(json, "session_evidence_sha256"), "session export");
        validateStreams(reports, request.id(), json(prior));
        OpenCodeSessionExport.validateEvidence(Files.readString(sessionEvidence,
                StandardCharsets.UTF_8), request.session(), root);
        require(integer(json, "contract_events") == 0, "rollover consumed contract events");
        OxAlphaRolloverLaunch.validateAvailable(root, request);
    }

    private static void validatePrior(Path root, CreateRequest request,
            Map<String, Object> prior, Path reports, String priorModel) throws Exception {
        require(integer(prior, "schema") == 1 && string(prior, "id").equals(request.id)
                && string(prior, "phase").equals("checkpoint") && integer(prior, "attempt") == 2,
                "prior launch identity drifted");
        require(MiniJson.bool(prior, "completed") && integer(prior, "exit") != 0,
                "prior launch is not a completed failure");
        require(prior.get("session") == null && prior.get("supervisor_stop") == null
                && integer(prior, "steps") == 0
                && integer(prior, "tool_calls") == 0 && integer(prior, "tokens_input") == 0
                && integer(prior, "tokens_output") == 0 && integer(prior, "tokens_reasoning") == 0
                && integer(prior, "tokens_cache_read") == 0
                && integer(prior, "tokens_cache_write") == 0
                && integer(prior, "context_peak_tokens") == 0
                && number(prior, "cost") == 0.0,
                "prior launch consumed contract events");
        require(string(prior, "stdout_sha256").equals(EMPTY_SHA)
                && string(prior, "stderr_sha256").equals(EMPTY_SHA),
                "legacy zero-event launch did not preserve empty streams");
        require(string(prior, "legacy_adoption_sha256").equalsIgnoreCase(request.adoptionSha),
                "prior launch used a different adoption receipt");
        String priorBase = string(prior, "base");
        require(string(prior, "model").equals(priorModel)
                && string(prior, "head").equals(priorBase)
                && string(prior, "control_base").equals(priorBase)
                && GitAncestry.contains(root, priorBase, request.controlBase),
                "prior launch model or commit lineage drifted");
        validateStreams(reports, request.id, prior);
    }

    private static void validateStreams(Path reports, String id, Map<String, Object> prior)
            throws Exception {
        Path stdout = reports.resolve("opencode-" + id + "-checkpoint-attempt2.jsonl");
        Path stderr = reports.resolve("opencode-" + id + "-checkpoint-attempt2.stderr.log");
        require(Files.isRegularFile(stdout) && Files.size(stdout) == 0
                && sha(stdout).equalsIgnoreCase(string(prior, "stdout_sha256"))
                && Files.isRegularFile(stderr) && Files.size(stderr) == 0
                && sha(stderr).equalsIgnoreCase(string(prior, "stderr_sha256")),
                "prior zero-event stream files drifted");
    }

    private static Adoption validateAdoption(Path root, CreateRequest request, Path path)
            throws Exception {
        OxAlphaAdoptionReceipt receipt = OxAlphaAdoptionReceipt.read(path);
        String mode = receipt.mode();
        require(receipt.schema() == 1 && receipt.id().equals(request.id)
                && GitAncestry.contains(root, receipt.controlBase(), request.controlBase)
                && receipt.branch().equals("codex/milestone-" + request.id)
                && receipt.worktree().equals(root) && receipt.priorAttempt() == 1
                && receipt.authorizedAttempt() == 2 && receipt.maxAttempts() == 2
                && receipt.status().equals("PASS"), "legacy adoption receipt drifted");
        boolean recovery = mode.equals("process-recovery") && receipt.session() == null
                && receipt.recoverySessions() == 1;
        boolean resume = mode.equals("resume-session")
                && request.session.equals(receipt.session()) && receipt.recoverySessions() == 0;
        require(recovery || resume, "legacy adoption mode or session drifted");
        long timeout = recovery ? receipt.recoveryTimeout() : 0;
        require(!recovery || timeout > 0 && timeout <= 3600,
                "process-recovery adoption budget drifted");
        return new Adoption(mode, timeout);
    }

    private static Path canonical(Path reports, String name, Path supplied) {
        Path expected = reports.resolve(name).normalize();
        require(supplied.toAbsolutePath().normalize().equals(expected)
                        && Files.isRegularFile(expected, LinkOption.NOFOLLOW_LINKS),
                name + " is not canonical");
        try {
            require(expected.toRealPath().equals(expected), name + " crosses a filesystem link");
        } catch (java.io.IOException error) {
            throw new IllegalStateException(name + " real path is unavailable", error);
        }
        return expected;
    }

    private static Path canonicalProviderLog() throws Exception {
        Path expected = Path.of(System.getProperty("user.home"), ".local", "share", "opencode",
                "log", "opencode.log").toAbsolutePath().normalize();
        require(Files.isRegularFile(expected, LinkOption.NOFOLLOW_LINKS)
                && expected.toRealPath().equals(expected),
                "canonical OpenCode provider log is unavailable or linked");
        return expected;
    }

    private static Map<String, Object> json(Path path) throws Exception {
        return MiniJson.object(Files.readString(path, StandardCharsets.UTF_8));
    }

    private static String string(Map<String, Object> json, String name) {
        return MiniJson.string(json, name);
    }

    private static long integer(Map<String, Object> json, String name) {
        return MiniJson.integer(json, name);
    }

    private static double number(Map<String, Object> json, String name) {
        Object value = json.get(name);
        require(value instanceof Number, name + " is not a number");
        return ((Number) value).doubleValue();
    }

    private static void requireHash(Path path, String expected, String name) throws Exception {
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                && path.toRealPath().equals(path.toAbsolutePath().normalize())
                && sha(path).equalsIgnoreCase(expected), name + " SHA-256 drifted or linked");
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(120, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
        return output;
    }

    static String sha(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (count > 0) {
                    digest.update(buffer, 0, count);
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    record CreateRequest(String id, String controlBase, String session, String authorizedModel,
            Path priorReceipt, String priorSha, Path adoptionReceipt, String adoptionSha,
            Path sessionEvidence, String sessionEvidenceSha, Path providerLog, String providerLogSha) {
        static CreateRequest parse(String[] arguments) {
            require(arguments.length == 24, "rollover requires exactly twelve argument pairs");
            Map<String, String> values = new java.util.LinkedHashMap<>();
            for (int index = 0; index < arguments.length; index += 2) {
                require(index + 1 < arguments.length, "missing value for " + arguments[index]);
                require(values.put(arguments[index], arguments[index + 1]) == null,
                        "duplicate argument: " + arguments[index]);
            }
            return new CreateRequest(value(values, "--id"), value(values, "--control-base"),
                    value(values, "--session"), value(values, "--authorized-model"),
                    Path.of(value(values, "--prior-receipt")),
                    value(values, "--prior-receipt-sha256"),
                    Path.of(value(values, "--adoption-receipt")),
                    value(values, "--adoption-sha256"),
                    Path.of(value(values, "--session-evidence")),
                    value(values, "--session-evidence-sha256"),
                    Path.of(value(values, "--provider-log")).toAbsolutePath().normalize(),
                    value(values, "--provider-log-sha256"));
        }

        private static String value(Map<String, String> values, String name) {
            String value = values.get(name);
            require(value != null, "missing argument: " + name);
            return value;
        }
    }

    private record Adoption(String mode, long recoveryTimeout) {
    }
}
