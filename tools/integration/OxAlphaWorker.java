import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Launches one top-level Ox Alpha worker behind the supervised swarm interlocks. */
final class OxAlphaWorker {
    private static final String AGENT = "ox-alpha";
    private static final String PROMPT = "coordination/swarm/OX_ALPHA_PROMPT.md";

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) {
                selfTest();
                return;
            }
            if (List.of(arguments).equals(List.of("--self-test-stdin-child"))) {
                System.in.readAllBytes();
                System.out.println("stdin closed");
                return;
            }
            if (List.of(arguments).equals(List.of("--self-test-terminal-child"))) {
                System.out.println("{\"type\":\"tool_use\",\"title\":\"java tools/harness/Gate.java "
                        + "--milestone m1-contract\",\"metadata\":{\"exit\":1}}");
                System.out.flush();
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
                return;
            }
            OxAlphaRequest request = OxAlphaRequest.parse(arguments);
            int exit = launch(request);
            System.exit(exit);
        } catch (Exception exception) {
            System.err.println("Ox Alpha launch failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static int launch(OxAlphaRequest request) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        boolean fallback = OxAlphaProfile.fallback();
        require(OxAlphaProfile.budgetAllowed(fallback, request.phase(), request.session(),
                request.timeoutSeconds()), "fallback checkpoint resume requires at least 7200 seconds");
        validate(root, request);
        String message = message(request);
        List<String> command = command(message, request.session(), fallback);
        require(messagePrecedesFiles(command), "worker message must precede variadic file attachments");
        Path reports = root.resolve(".worldline/reports/swarm");
        Files.createDirectories(reports);
        String stem = "opencode-" + request.id() + "-" + request.phase()
                + "-attempt" + request.attempt();
        Path stdout = reports.resolve(stem + ".jsonl");
        Path stderr = reports.resolve(stem + ".stderr.log");
        Path receipt = reports.resolve(stem + ".json");
        require(!Files.exists(stdout) && !Files.exists(stderr) && !Files.exists(receipt),
                "immutable launch evidence already exists: " + stem);
        Instant started = Instant.now();
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile());
        String config = OxAlphaProfile.config(fallback);
        builder.environment().put("OPENCODE_CONFIG_CONTENT", config);
        Process process = builder.redirectError(stderr.toFile()).start();
        OxAlphaTerminalMonitor.Capture capture = OxAlphaTerminalMonitor.capture(process, stdout);
        closeStdin(process);
        OxAlphaTerminalMonitor.Outcome outcome = OxAlphaTerminalMonitor.waitFor(
                process, capture, request.timeoutSeconds());
        Instant finished = Instant.now();
        String head = git(root, "rev-parse", "HEAD").trim();
        writeReceipt(receipt, request, started, finished, head, stdout, stderr,
                outcome, fallback, config);
        return outcome.exit();
    }

    private static void validate(Path root, OxAlphaRequest request) throws Exception {
        require(request.id().matches("m[0-9]+-[a-z0-9-]+"), "invalid milestone id");
        require(request.base().matches("[0-9a-f]{40}"), "base must be an exact commit SHA");
        require(request.controlBase().matches("[0-9a-f]{40}"),
                "control base must be an exact commit SHA");
        require(request.phase().equals("checkpoint") || request.phase().equals("qualify"),
                "phase must be checkpoint or qualify");
        require(request.attempt() > 0 && request.timeoutSeconds() > 0, "invalid launch limits");
        require(!request.goal().isBlank(), "goal is required");
        MilestoneObjective.load(root, request.id(), request.goal(), request.base());
        String branch = git(root, "branch", "--show-current").trim();
        require(branch.equals("codex/milestone-" + request.id()), "wrong milestone branch: " + branch);
        String head = git(root, "rev-parse", "HEAD").trim();
        require(ancestor(root, request.controlBase(), request.base()),
                "authorized base predates the orchestrator control base");
        require(ancestor(root, request.controlBase(), head),
                "worktree predates the orchestrator control base");
        require(gitStatus(root, "merge-base", "--is-ancestor", request.base(), head) == 0,
                "authorized base is not an ancestor of HEAD");
        Path preflight = root.resolve(".worldline/reports/swarm/preflight-" + request.id() + ".json");
        requireBoundPass(preflight, request.id(), request.base(), request.goal(), true);
        Path prompt = root.resolve(PROMPT);
        require(Files.isRegularFile(prompt), "missing Ox Alpha prompt");
        if (request.phase().equals("checkpoint")) {
            require(head.equals(request.base()), "checkpoint must start at the exact base");
            if (request.session() == null) {
                require(git(root, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                        "initial checkpoint must start with a clean tree");
            } else {
                require(request.attempt() > 1, "checkpoint resume requires a later attempt");
                Path prior = root.resolve(".worldline/reports/swarm/opencode-" + request.id()
                        + "-checkpoint-attempt" + (request.attempt() - 1) + ".json");
                requireBoundSession(prior, request.session());
            }
        } else {
            Path readiness = root.resolve(".worldline/reports/swarm/readiness-" + request.id() + ".json");
            requireBoundPass(readiness, request.id(), request.base(), request.goal(), false);
            require(request.session() != null && !request.session().isBlank(),
                    "qualify phase must resume the supervised checkpoint session");
        }
    }

    private static void requireBoundSession(Path path, String session) throws Exception {
        require(Files.isRegularFile(path) && Files.readString(path, StandardCharsets.UTF_8)
                .contains("\"session\":\"" + session + "\""), "checkpoint session is not bound to prior receipt");
    }

    private static void requireBoundPass(Path path, String id, String base, String goal,
            boolean head) throws Exception {
        require(Files.isRegularFile(path), "missing supervisor proof: " + path.getFileName());
        String text = Files.readString(path, StandardCharsets.UTF_8);
        require(text.contains("\"id\":\"" + id + "\"") && text.contains("\"base\":\"" + base + "\"")
                && text.contains("\"status\":\"PASS\""), "supervisor proof is not bound to launch");
        if (head) {
            require(text.contains("\"goal_sha256\":\"" + sha(goal) + "\""),
                    "supervisor proof goal differs from launch");
            require(text.contains("\"head\":\"" + base + "\""), "preflight head drifted");
        }
    }

    private static List<String> command(String message, String session, boolean fallback) {
        List<String> result = new ArrayList<>(List.of(opencodeTool(), "run", message,
                "--pure", "--auto", "--agent", AGENT, "--model", OxAlphaProfile.model(fallback),
                "--format", "json", "--title", "Worldline Ox Alpha"));
        if (session != null && !session.isBlank()) {
            result.add("--session");
            result.add(session);
        }
        result.add("-f");
        result.add(PROMPT);
        return result;
    }

    private static String message(OxAlphaRequest request) {
        String phase = request.phase().equals("checkpoint") && request.session() == null
                ? "Implement the complete real contract, but do not run Candidate or Milestone Gate, "
                        + "commit, or hand off. "
                        + "Stop with CHECKPOINT_READY, RETRYABLE_PROPOSED, or REJECTED_PROPOSED."
                : request.phase().equals("checkpoint")
                ? "Resume the existing dirty checkpoint after the archived provider failure. Do not run Candidate "
                        + "or Milestone Gate, commit, or hand off. Stop with an exact checkpoint disposition."
                : "The supervisor readiness interlock passed. Do not change sources before Candidate Gate. "
                        + "Run Candidate Gate once. If it fails, stop with RETRYABLE_PROPOSED. "
                        + "If it passes, make one logical commit, require a clean tree, run Milestone Gate, "
                        + "and stop with the exact handoff.";
        return "You are the supervised top-level Ox Alpha worker for " + request.id() + ". Goal: "
                + request.goal() + ". Authorized base: " + request.base() + ". Control base: "
                + request.controlBase() + ". Read AGENTS.md, "
                + "docs/ENGINEERING_WORKFLOW.md, and the attached worker contract completely. "
                + "Nested task, explore, or subagent delegation is forbidden. Inspect the repository directly. "
                + phase;
    }

    private static void writeReceipt(Path receipt, OxAlphaRequest request, Instant started,
            Instant finished, String head,
            Path stdout, Path stderr, OxAlphaTerminalMonitor.Outcome outcome,
            boolean fallback, String config)
            throws Exception {
        String session = OxAlphaTelemetry.firstSession(stdout);
        OxAlphaTelemetry.Result telemetry = OxAlphaTelemetry.read(stdout);
        String value = "{\n  \"schema\":1,\n  \"id\":\"" + request.id()
                + "\",\n  \"phase\":\"" + request.phase() + "\",\n  \"attempt\":" + request.attempt()
                + ",\n  \"started\":\"" + started + "\",\n  \"finished\":\"" + finished
                + "\",\n  \"base\":\"" + request.base() + "\",\n  \"control_base\":\""
                + request.controlBase() + "\",\n  \"head\":\"" + head
                + "\",\n  \"profile\":\"" + (fallback ? "fallback" : "primary")
                + "\",\n  \"model\":\"" + OxAlphaProfile.model(fallback) + "\",\n  \"variant\":\""
                + "default"
                + "\",\n  \"agent\":\"" + AGENT + "\",\n  \"nested_delegation\":\"denied\""
                + ",\n  \"config_sha256\":\"" + sha(config) + "\",\n  \"stdout_sha256\":\""
                + sha(stdout) + "\",\n  \"stderr_sha256\":\"" + sha(stderr) + "\",\n  \"session\":"
                + (session == null ? "null" : "\"" + escape(session) + "\"")
                + ",\n  \"exit\":" + outcome.exit() + ",\n  \"completed\":" + outcome.completed()
                + OxAlphaTelemetry.receiptFields(telemetry, started, finished)
                + ",\n  \"supervisor_stop\":" + (outcome.supervisorStop() == null ? "null"
                        : "\"" + outcome.supervisorStop() + "\"") + "\n}\n";
        Files.writeString(receipt, value, StandardCharsets.UTF_8);
    }

    private static void selfTest() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        String head = git(root, "rev-parse", "HEAD").trim();
        OxAlphaRequest checkpoint = new OxAlphaRequest("m1-contract", "Prove a real behavior",
                head, head, "checkpoint", 1, null, 60);
        List<String> valid = command(message(checkpoint), null, false);
        require(messagePrecedesFiles(valid), "canonical argument order was rejected");
        List<String> invalid = new ArrayList<>(valid);
        String message = invalid.remove(2);
        invalid.add(message);
        require(!messagePrecedesFiles(invalid), "variadic attachment swallowed the worker message");
        OxAlphaProfile.selfTest();
        OxAlphaTelemetry.selfTest();
        OxAlphaTerminalMonitor.selfTest();
        require(command(message(checkpoint), null, false).contains(OxAlphaProfile.DEFAULT_MODEL),
                "default Ox Alpha model is not allowlisted");
        require(command(message(checkpoint), "session", true).contains(OxAlphaProfile.DEFAULT_FALLBACK_MODEL),
                "fallback model is not allowlisted");
        require(message(checkpoint).contains("Nested task, explore, or subagent delegation is forbidden"),
                "worker message omitted delegation prohibition");
        require(ancestor(root, head, head), "exact control base was rejected");
        require(!ancestor(root, "0".repeat(40), head), "missing control base was accepted");
        Process child = new ProcessBuilder(javaTool(), "-cp", System.getProperty("java.class.path"),
                "OxAlphaWorker", "--self-test-stdin-child").start();
        closeStdin(child);
        require(child.waitFor(5, TimeUnit.SECONDS), "launcher left the child stdin open");
        require(child.exitValue() == 0, "stdin closure child failed");
        Path terminalDirectory = Files.createTempDirectory("worldline-ox-terminal-");
        Path terminal = terminalDirectory.resolve("stdout.jsonl");
        try {
            Process runaway = new ProcessBuilder(javaTool(), "-cp", System.getProperty("java.class.path"),
                    "OxAlphaWorker", "--self-test-terminal-child").start();
            OxAlphaTerminalMonitor.Capture capture = OxAlphaTerminalMonitor.capture(runaway, terminal);
            closeStdin(runaway);
            OxAlphaTerminalMonitor.Outcome stopped = OxAlphaTerminalMonitor.waitFor(runaway, capture, 10, 1);
            require(stopped.stoppedAfterTerminal(), "terminal Gate failure did not stop worker");
        } finally {
            Files.deleteIfExists(terminal);
            Files.deleteIfExists(terminalDirectory);
        }
    }

    private static void closeStdin(Process process) throws Exception {
        process.getOutputStream().close();
    }

    private static boolean messagePrecedesFiles(List<String> command) {
        int run = command.indexOf("run");
        int file = command.indexOf("-f");
        return run >= 0 && run + 1 < file && file == command.size() - 2
                && !command.get(run + 1).startsWith("-");
    }

    private static String opencodeTool() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return "opencode";
        }
        String appData = System.getenv("APPDATA");
        require(appData != null, "APPDATA is unavailable");
        Path tool = Path.of(appData, "npm", "node_modules", "opencode-ai", "bin", "opencode.exe");
        require(Files.isRegularFile(tool), "OpenCode executable is unavailable: " + tool);
        return tool.toString();
    }

    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : ""))
                .toString();
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(120, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
        return output;
    }

    private static int gitStatus(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        require(process.waitFor(120, TimeUnit.SECONDS), "git timed out");
        return process.exitValue();
    }

    private static boolean ancestor(Path root, String ancestor, String descendant) throws Exception {
        return gitStatus(root, "merge-base", "--is-ancestor", ancestor, descendant) == 0;
    }

    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static String sha(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
