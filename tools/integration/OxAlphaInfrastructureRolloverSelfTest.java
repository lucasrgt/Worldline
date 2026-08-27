import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Proves positive and negative infrastructure-rollover boundaries. */
final class OxAlphaInfrastructureRolloverSelfTest {
    private static final String ID = "m1-contract";
    private static final String SESSION = "ses_rolloverfixture";
    private static final String MODEL = "opencode-go/glm-5.3-flash";
    private static final String FALLBACK = "opencode-go/deepseek-v4-flash";
    private static final String EMPTY_SHA =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private OxAlphaInfrastructureRolloverSelfTest() {
    }

    public static void main(String[] arguments) throws Exception {
        Path parent = Files.createTempDirectory("worldline-rollover-");
        String priorHome = System.getProperty("user.home");
        System.setProperty("user.home", parent.toString());
        try {
            Fixture fixture = fixture(parent.resolve("valid"), 0, "process-recovery", MODEL);
            Path receipt = OxAlphaInfrastructureRollover.create(fixture.root, fixture.request);
            String rolloverSha = OxAlphaInfrastructureRollover.sha(receipt);
            OxAlphaRequest launch = new OxAlphaRequest(ID, "goal", fixture.head, fixture.head,
                    "checkpoint", 2, 3, SESSION, 7200, fixture.adoption.toString(),
                    fixture.adoptionSha, receipt.toString(), rolloverSha);
            OxAlphaInfrastructureRollover.validate(fixture.root, launch, true, FALLBACK);
            OxAlphaLegacyAdoption.validate(fixture.root, launch);
            expectFailure(() -> OxAlphaLegacyAdoption.validate(fixture.root,
                    new OxAlphaRequest(ID, "goal", fixture.head, fixture.head, "checkpoint",
                            2, 3, SESSION, 7201, fixture.adoption.toString(), fixture.adoptionSha,
                            receipt.toString(), rolloverSha)),
                    "unbounded rollover adoption budget was accepted");
            require(launch.evidenceStem().equals(
                    "opencode-m1-contract-checkpoint-attempt2-launch3"),
                    "rollover evidence stem is not collision-free");
            expectFailure(() -> OxAlphaInfrastructureRollover.validate(fixture.root,
                    new OxAlphaRequest(ID, "goal", fixture.head, fixture.head, "checkpoint",
                            2, 4, SESSION, 7200, fixture.adoption.toString(), fixture.adoptionSha,
                            receipt.toString(), rolloverSha), true, FALLBACK),
                    "fourth infrastructure launch was accepted");
            expectFailure(() -> OxAlphaInfrastructureRollover.validate(fixture.root,
                    new OxAlphaRequest(ID, "goal", fixture.head, fixture.head, "checkpoint",
                            2, 3, SESSION, 7200, fixture.adoption.toString(), fixture.adoptionSha,
                            receipt.toString(), "0".repeat(64)), true, FALLBACK),
                    "rollover hash drift was accepted");
            expectFailure(() -> OxAlphaInfrastructureRollover.validate(fixture.root, launch,
                    false, FALLBACK), "primary profile was accepted for rollover");
            expectFailure(() -> OxAlphaInfrastructureRollover.validate(fixture.root, launch,
                    true, MODEL), "failed provider model was accepted for rollover");
            require(!OxAlphaProfile.budgetAllowed(true, "checkpoint", SESSION, 3600)
                    && OxAlphaProfile.budgetAllowed(true, "checkpoint", SESSION, 7200),
                    "fallback rollover budget interlock drifted");
            Path stdout = fixture.root.resolve(".worldline/reports/swarm/opencode-" + ID
                    + "-checkpoint-attempt2.jsonl");
            Files.writeString(stdout, "mutated", StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.validate(fixture.root, launch,
                    true, FALLBACK), "stream mutation after seal was accepted");
            Files.writeString(stdout, "", StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(fixture.root, fixture.request),
                    "second rollover receipt replaced immutable evidence");
            Path claim = OxAlphaRolloverLaunch.reserve(fixture.root, launch);
            require(Files.isRegularFile(claim)
                    && OxAlphaRolloverLaunch.claimSha(fixture.root, launch)
                            .equals(OxAlphaInfrastructureRollover.sha(claim)),
                    "rollover launch claim was not hash-bound");
            expectFailure(() -> OxAlphaRolloverLaunch.reserve(fixture.root, launch),
                    "concurrent rollover launch acquired the same claim");
            Fixture resume = fixture(parent.resolve("resume"), 0, "resume-session", MODEL);
            Path resumeReceipt = OxAlphaInfrastructureRollover.create(resume.root, resume.request);
            OxAlphaRequest resumeLaunch = new OxAlphaRequest(ID, "goal", resume.head, resume.head,
                    "checkpoint", 2, 3, SESSION, 7200, resume.adoption.toString(),
                    resume.adoptionSha, resumeReceipt.toString(),
                    OxAlphaInfrastructureRollover.sha(resumeReceipt));
            OxAlphaInfrastructureRollover.validate(resume.root, resumeLaunch, true, FALLBACK);
            OxAlphaLegacyAdoption.validate(resume.root, resumeLaunch);
            OxAlphaRolloverLaunch.reserve(resume.root, resumeLaunch);
            ProcessBuilder missing = new ProcessBuilder("worldline-missing-opencode-executable");
            expectFailure(() -> OxAlphaRolloverLaunch.start(resume.root, resumeLaunch, missing),
                    "process start failure was not surfaced");
            OxAlphaInfrastructureRollover.validate(resume.root, resumeLaunch, true, FALLBACK);
            OxAlphaRolloverLaunch.reserve(resume.root, resumeLaunch);
            expectFailure(() -> OxAlphaRolloverLaunch.start(resume.root, resumeLaunch, missing),
                    "bounded process start retry failure was not surfaced");
            expectFailure(() -> OxAlphaInfrastructureRollover.validate(
                    resume.root, resumeLaunch, true, FALLBACK),
                    "exhausted process start retry was reopened");
            Fixture terminal = fixture(parent.resolve("terminal"), 0, "resume-session", MODEL);
            Path terminalReceipt = OxAlphaInfrastructureRollover.create(
                    terminal.root, terminal.request);
            OxAlphaRequest terminalLaunch = new OxAlphaRequest(ID, "goal", terminal.head,
                    terminal.head, "checkpoint", 2, 3, SESSION, 7200,
                    terminal.adoption.toString(), terminal.adoptionSha,
                    terminalReceipt.toString(), OxAlphaInfrastructureRollover.sha(terminalReceipt));
            OxAlphaRolloverLaunch.reserve(terminal.root, terminalLaunch);
            OxAlphaRolloverLaunch.recordTerminal(terminal.root, terminalLaunch,
                    "capture", new java.io.IOException("fixture"));
            require(Files.isRegularFile(terminal.root.resolve(".worldline/reports/swarm/"
                    + "infrastructure-rollover-" + ID
                    + "-attempt2-launch3-terminal-failure.json")),
                    "post-start failure lacked a terminal infrastructure receipt");
            Fixture events = fixture(parent.resolve("events"), 1, "process-recovery", MODEL);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(events.root, events.request),
                    "provider failure after contract work received a free rollover");
            Fixture step = fixture(parent.resolve("step"), 0, "process-recovery", MODEL);
            Files.writeString(step.providerLog, Files.readString(step.providerLog)
                    + "timestamp=2026-08-27T05:47:53.5Z level=INFO run=fixture session.id="
                    + SESSION + " message=loop step=1\n", StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(step.root, step.request),
                    "same-run step 1 was accepted as zero-event evidence");
            Fixture event = fixture(parent.resolve("event"), 0, "process-recovery", MODEL);
            Files.writeString(event.providerLog, Files.readString(event.providerLog)
                    + "timestamp=2026-08-27T05:47:53.5Z level=INFO run=fixture session.id="
                    + SESSION + " message=tool\n", StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(event.root, event.request),
                    "unclassified same-session event was discarded");
            Fixture overlap = fixture(parent.resolve("overlap"), 0, "process-recovery", MODEL);
            Files.writeString(overlap.providerLog, Files.readString(overlap.providerLog)
                    + "timestamp=2026-08-27T05:47:53.5Z level=INFO run=other session.id="
                    + SESSION + " message=loop step=0\n", StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(overlap.root, overlap.request),
                    "overlapping same-session run was accepted");
            Fixture directory = fixture(parent.resolve("directory"), 0,
                    "process-recovery", MODEL);
            Files.writeString(directory.providerLog, Files.readString(directory.providerLog)
                    .replace(escape(directory.root.toString()), escape(parent.toString())),
                    StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(
                    directory.root, directory.request), "wrong created worktree was accepted");
            Fixture created = fixture(parent.resolve("created"), 0, "process-recovery", MODEL);
            Files.writeString(created.providerLog, Files.readString(created.providerLog)
                    .replace("message=created id=" + SESSION,
                            "message=created id=ses_other"), StandardCharsets.UTF_8);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(created.root, created.request),
                    "wrong created session was accepted");
            Fixture model = fixture(parent.resolve("model"), 0, "process-recovery", FALLBACK);
            expectFailure(() -> OxAlphaInfrastructureRollover.create(model.root,
                    new OxAlphaInfrastructureRollover.CreateRequest(ID, model.head, SESSION,
                            FALLBACK, model.prior, model.priorSha, model.adoption, model.adoptionSha,
                            model.sessionEvidence, model.sessionSha, model.providerLog,
                            OxAlphaInfrastructureRollover.sha(model.providerLog))),
                    "provider evidence for a different model was accepted");
        } finally {
            System.setProperty("user.home", priorHome);
            SafeTreeDelete.delete(parent);
        }
    }

    private static Fixture fixture(Path root, int steps, String adoptionMode,
            String providerModel) throws Exception {
        Files.createDirectories(root);
        git(root, "init", "--quiet", "--initial-branch=codex/milestone-" + ID);
        git(root, "config", "user.name", "Worldline Self Test");
        git(root, "config", "user.email", "selftest@worldline.invalid");
        Files.writeString(root.resolve(".gitignore"), ".worldline/\n", StandardCharsets.UTF_8);
        Files.writeString(root.resolve("tracked.txt"), "control\n", StandardCharsets.UTF_8);
        git(root, "add", ".gitignore", "tracked.txt");
        git(root, "commit", "--quiet", "-m", "control");
        String head = git(root, "rev-parse", "HEAD").trim();
        Path reports = root.resolve(".worldline/reports/swarm");
        Files.createDirectories(reports);
        Path adoption = reports.resolve("legacy-retry-adoption-" + ID + ".json");
        Files.writeString(adoption, adoption(root, head, adoptionMode), StandardCharsets.UTF_8);
        String adoptionSha = OxAlphaInfrastructureRollover.sha(adoption);
        Path prior = reports.resolve("opencode-" + ID + "-checkpoint-attempt2.json");
        Files.writeString(prior, prior(head, adoptionSha, steps), StandardCharsets.UTF_8);
        Files.writeString(reports.resolve("opencode-" + ID
                + "-checkpoint-attempt2.jsonl"), "", StandardCharsets.UTF_8);
        Files.writeString(reports.resolve("opencode-" + ID
                + "-checkpoint-attempt2.stderr.log"), "", StandardCharsets.UTF_8);
        String priorSha = OxAlphaInfrastructureRollover.sha(prior);
        Path sessionEvidence = reports.resolve("opencode-session-" + SESSION + ".json");
        Files.writeString(sessionEvidence, "{\"info\":{\"id\":\"" + SESSION
                + "\",\"directory\":\"" + escape(root.toAbsolutePath().normalize().toString())
                + "\"}}\n", StandardCharsets.UTF_8);
        String sessionSha = OxAlphaInfrastructureRollover.sha(sessionEvidence);
        Path providerLog = Path.of(System.getProperty("user.home"), ".local", "share",
                "opencode", "log", "opencode.log");
        Files.createDirectories(providerLog.getParent());
        Files.writeString(providerLog, provider(providerModel, adoptionMode, root),
                StandardCharsets.UTF_8);
        var request = new OxAlphaInfrastructureRollover.CreateRequest(ID, head, SESSION, FALLBACK,
                prior, priorSha, adoption, adoptionSha, sessionEvidence, sessionSha, providerLog,
                OxAlphaInfrastructureRollover.sha(providerLog));
        return new Fixture(root, head, adoption, adoptionSha, prior, priorSha,
                sessionEvidence, sessionSha, providerLog, request);
    }

    private static String adoption(Path root, String head, String mode) {
        boolean recovery = mode.equals("process-recovery");
        return "{\"schema\":1,\"id\":\"" + ID + "\",\"mode\":\"" + mode + "\","
                + "\"owner\":\"orchestrator\",\"prior_attempt\":1,\"authorized_attempt\":2,"
                + "\"max_attempts\":2,\"new_control_base\":\"" + head + "\","
                + "\"branch\":\"codex/milestone-" + ID + "\",\"worktree\":\""
                + escape(root.toAbsolutePath().normalize().toString()) + "\",\"session\":"
                + (recovery ? "null" : "\"" + SESSION + "\"")
                + ",\"archive_sha256\":\"" + "a".repeat(64) + "\""
                + ",\"recovery_sessions_allowed\":" + (recovery ? 1 : 0)
                + ",\"recovery_timeout_seconds\":" + (recovery ? 3600 : 0) + ","
                + "\"status\":\"PASS\"}\n";
    }

    private static String prior(String head, String adoptionSha, int steps) {
        return "{\"schema\":1,\"id\":\"" + ID + "\",\"phase\":\"checkpoint\","
                + "\"attempt\":2,\"started\":\"2026-08-27T05:47:52Z\","
                + "\"finished\":\"2026-08-27T05:48:00Z\",\"base\":\"" + head + "\","
                + "\"control_base\":\"" + head + "\",\"head\":\"" + head + "\","
                + "\"model\":\"" + MODEL + "\","
                + "\"stdout_sha256\":\"" + EMPTY_SHA + "\",\"stderr_sha256\":\""
                + EMPTY_SHA + "\",\"session\":null,\"legacy_adoption_sha256\":\""
                + adoptionSha + "\",\"exit\":-1,\"completed\":true,\"wall_seconds\":313.0,"
                + "\"steps\":" + steps
                + ",\"tool_calls\":0,\"tokens_input\":0,\"tokens_output\":0,"
                + "\"tokens_reasoning\":0,\"tokens_cache_read\":0,"
                + "\"tokens_cache_write\":0,\"context_peak_tokens\":0,\"cost\":0.0,"
                + "\"supervisor_stop\":null}\n";
    }

    private static String provider(String model, String adoptionMode, Path root) {
        String prefix = "timestamp=2026-08-27T05:47:";
        String common = " run=fixture session.id=" + SESSION;
        String created = adoptionMode.equals("process-recovery")
                ? prefix + "52.5Z level=INFO run=fixture message=created id=" + SESSION
                        + " directory=\"" + escape(root.toAbsolutePath().normalize().toString())
                        + "\"\n" : "";
        String historical = adoptionMode.equals("resume-session")
                ? "timestamp=2026-08-25T05:47:53Z level=INFO run=older session.id="
                        + SESSION + " message=loop step=33\n" : "";
        return historical + created + prefix + "53Z level=INFO" + common + " message=loop step=0\n"
                + prefix + "54Z level=INFO" + common + " message=process messageID=msg_one\n"
                + prefix + "55Z level=INFO" + common + " message=stream providerID=opencode-go"
                + " modelID=" + model.substring("opencode-go/".length()) + "\n"
                + prefix + "56Z level=ERROR" + common + " message=\"stream error\""
                + " providerID=opencode-go modelID=" + model.substring("opencode-go/".length())
                + " error.error=\"AI_APICallError: Monthly usage limit reached.\"\n";
    }

    private static void expectFailure(Checked action, String message) throws Exception {
        boolean rejected = false;
        try {
            action.run();
        } catch (Exception expected) {
            rejected = true;
        }
        require(rejected, message);
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
        return output;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface Checked {
        void run() throws Exception;
    }

    private record Fixture(Path root, String head, Path adoption, String adoptionSha,
            Path prior, String priorSha, Path sessionEvidence, String sessionSha,
            Path providerLog, OxAlphaInfrastructureRollover.CreateRequest request) {
    }
}
