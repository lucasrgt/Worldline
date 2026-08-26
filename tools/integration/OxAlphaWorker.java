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
    private OxAlphaWorker() {
    }

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
            Request request = parse(arguments);
            int exit = launch(request);
            System.exit(exit);
        } catch (Exception exception) {
            System.err.println("Ox Alpha launch failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static int launch(Request request) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        validate(root, request);
        boolean fallback = OxAlphaProfile.fallback();
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
        Process process = builder.redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
        closeStdin(process);
        boolean completed = process.waitFor(request.timeoutSeconds(), TimeUnit.SECONDS);
        if (!completed) {
            terminate(process);
        }
        int exit = completed ? process.exitValue() : 124;
        String head = git(root, "rev-parse", "HEAD").trim();
        writeReceipt(receipt, request, started, head, stdout, stderr, exit, completed, fallback, config);
        return exit;
    }

    private static void validate(Path root, Request request) throws Exception {
        require(request.id().matches("m[0-9]+-[a-z0-9-]+"), "invalid milestone id");
        require(request.base().matches("[0-9a-f]{40}"), "base must be an exact commit SHA");
        require(request.phase().equals("checkpoint") || request.phase().equals("qualify"),
                "phase must be checkpoint or qualify");
        require(request.attempt() > 0 && request.timeoutSeconds() > 0, "invalid launch limits");
        require(!request.goal().isBlank(), "goal is required");
        String branch = git(root, "branch", "--show-current").trim();
        require(branch.equals("codex/milestone-" + request.id()), "wrong milestone branch: " + branch);
        String head = git(root, "rev-parse", "HEAD").trim();
        require(gitStatus(root, "merge-base", "--is-ancestor", request.base(), head) == 0,
                "authorized base is not an ancestor of HEAD");
        Path preflight = root.resolve(".worldline/reports/swarm/preflight-" + request.id() + ".json");
        requireBoundPass(preflight, request.id(), request.base(), true);
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
            requireBoundPass(readiness, request.id(), request.base(), false);
            require(request.session() != null && !request.session().isBlank(),
                    "qualify phase must resume the supervised checkpoint session");
        }
    }

    private static void requireBoundSession(Path path, String session) throws Exception {
        require(Files.isRegularFile(path) && Files.readString(path, StandardCharsets.UTF_8)
                .contains("\"session\":\"" + session + "\""), "checkpoint session is not bound to prior receipt");
    }

    private static void requireBoundPass(Path path, String id, String base, boolean head) throws Exception {
        require(Files.isRegularFile(path), "missing supervisor proof: " + path.getFileName());
        String text = Files.readString(path, StandardCharsets.UTF_8);
        require(text.contains("\"id\":\"" + id + "\"") && text.contains("\"base\":\"" + base + "\"")
                && text.contains("\"status\":\"PASS\""), "supervisor proof is not bound to launch");
        if (head) {
            require(text.contains("\"head\":\"" + base + "\""), "preflight head drifted");
        }
    }

    private static List<String> command(String message, String session, boolean fallback) {
        List<String> result = new ArrayList<>(List.of(opencodeTool(), "run", message,
                "--pure", "--auto", "--agent", AGENT, "--model", OxAlphaProfile.model(fallback),
                "--format", "json", "--title", "Worldline Ox Alpha"));
        if (!fallback) {
            result.addAll(List.of("--variant", "max"));
        }
        if (session != null && !session.isBlank()) {
            result.add("--session");
            result.add(session);
        }
        result.add("-f");
        result.add(PROMPT);
        return result;
    }

    private static String message(Request request) {
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
                + request.goal() + ". Authorized base: " + request.base() + ". Read AGENTS.md, "
                + "docs/ENGINEERING_WORKFLOW.md, and the attached worker contract completely. "
                + "Nested task, explore, or subagent delegation is forbidden. Inspect the repository directly. "
                + phase;
    }

    private static void writeReceipt(Path receipt, Request request, Instant started, String head,
            Path stdout, Path stderr, int exit, boolean completed, boolean fallback, String config)
            throws Exception {
        String session = firstSession(stdout);
        String value = "{\n  \"schema\":1,\n  \"id\":\"" + request.id()
                + "\",\n  \"phase\":\"" + request.phase() + "\",\n  \"attempt\":" + request.attempt()
                + ",\n  \"started\":\"" + started + "\",\n  \"finished\":\"" + Instant.now()
                + "\",\n  \"base\":\"" + request.base() + "\",\n  \"head\":\"" + head
                + "\",\n  \"profile\":\"" + (fallback ? "fallback" : "primary")
                + "\",\n  \"model\":\"" + OxAlphaProfile.model(fallback) + "\",\n  \"variant\":\""
                + (fallback ? "default" : "max")
                + "\",\n  \"agent\":\"" + AGENT + "\",\n  \"nested_delegation\":\"denied\""
                + ",\n  \"config_sha256\":\"" + sha(config) + "\",\n  \"stdout_sha256\":\""
                + sha(stdout) + "\",\n  \"stderr_sha256\":\"" + sha(stderr) + "\",\n  \"session\":"
                + (session == null ? "null" : "\"" + escape(session) + "\"")
                + ",\n  \"exit\":" + exit + ",\n  \"completed\":" + completed + "\n}\n";
        Files.writeString(receipt, value, StandardCharsets.UTF_8);
    }

    private static String firstSession(Path stdout) throws Exception {
        for (String line : Files.readAllLines(stdout, StandardCharsets.UTF_8)) {
            int key = line.indexOf("\"sessionID\":\"");
            if (key >= 0) {
                int start = key + 13;
                int end = line.indexOf('"', start);
                if (end > start) {
                    return line.substring(start, end);
                }
            }
        }
        return null;
    }

    private static Request parse(String[] arguments) {
        String id = null;
        String goal = null;
        String base = null;
        String phase = null;
        String session = null;
        int attempt = 0;
        int timeout = 3600;
        for (int index = 0; index < arguments.length; index += 2) {
            require(index + 1 < arguments.length, "missing value for " + arguments[index]);
            String value = arguments[index + 1];
            switch (arguments[index]) {
                case "--id" -> id = value;
                case "--goal" -> goal = value;
                case "--base" -> base = value;
                case "--phase" -> phase = value;
                case "--attempt" -> attempt = Integer.parseInt(value);
                case "--session" -> session = value;
                case "--timeout-seconds" -> timeout = Integer.parseInt(value);
                default -> throw new IllegalArgumentException("unknown argument: " + arguments[index]);
            }
        }
        return new Request(id, goal, base, phase, attempt, session, timeout);
    }

    private static void selfTest() throws Exception {
        Request checkpoint = new Request("m1-contract", "Prove a real behavior",
                "0123456789012345678901234567890123456789", "checkpoint", 1, null, 60);
        List<String> valid = command(message(checkpoint), null, false);
        require(messagePrecedesFiles(valid), "canonical argument order was rejected");
        List<String> invalid = new ArrayList<>(valid);
        String message = invalid.remove(2);
        invalid.add(message);
        require(!messagePrecedesFiles(invalid), "variadic attachment swallowed the worker message");
        require(OxAlphaProfile.config(false).contains("\"task\":\"deny\""),
                "nested task permission is not denied");
        require(OxAlphaProfile.config(false).contains("\"question\":\"deny\""),
                "interactive worker question is not denied");
        require(command(message(checkpoint), "session", true).contains(OxAlphaProfile.FALLBACK_MODEL),
                "fallback model is not allowlisted");
        require(message(checkpoint).contains("Nested task, explore, or subagent delegation is forbidden"),
                "worker message omitted delegation prohibition");
        Process child = new ProcessBuilder(javaTool(), "-cp", System.getProperty("java.class.path"),
                "OxAlphaWorker", "--self-test-stdin-child").start();
        closeStdin(child);
        require(child.waitFor(5, TimeUnit.SECONDS), "launcher left the child stdin open");
        require(child.exitValue() == 0, "stdin closure child failed");
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

    private static void terminate(Process process) {
        process.toHandle().descendants().sorted((left, right) -> Long.compare(right.pid(), left.pid()))
                .forEach(handle -> handle.destroyForcibly());
        process.destroyForcibly();
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

    private record Request(String id, String goal, String base, String phase, int attempt,
            String session, int timeoutSeconds) {
    }
}
