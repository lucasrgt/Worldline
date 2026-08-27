import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Exercises the complete supervised Ox Alpha process boundary. */
final class OxAlphaWorkerSelfTest {
    private OxAlphaWorkerSelfTest() {
    }

    static void execute() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        String head = git(root, "rev-parse", "HEAD").trim();
        OxAlphaRequest checkpoint = new OxAlphaRequest("m1-contract", "Prove a real behavior",
                head, head, "checkpoint", 1, null, 60, null, null);
        List<String> valid = OxAlphaWorker.command(OxAlphaWorker.message(checkpoint), null, false);
        require(OxAlphaWorker.messagePrecedesFiles(valid), "canonical argument order was rejected");
        int print = valid.indexOf("--print-logs");
        int log = valid.indexOf("--log-level");
        int run = valid.indexOf("run");
        require(print >= 0 && log == print + 1 && valid.get(log + 1).equals("INFO")
                && run == log + 2, "private OpenCode provider logs are disabled");
        List<String> invalid = new ArrayList<>(valid);
        String message = invalid.remove(invalid.indexOf("run") + 1);
        invalid.add(message);
        require(!OxAlphaWorker.messagePrecedesFiles(invalid),
                "variadic attachment swallowed the worker message");
        requireMalformedSessionRejected();
        requireRolloverArgumentsBound();
        OxAlphaProfile.selfTest();
        OxAlphaTelemetry.selfTest();
        OxAlphaProviderFailure.selfTest();
        OxAlphaTerminalMonitor.selfTest();
        require(OxAlphaWorker.command(OxAlphaWorker.message(checkpoint), null, false)
                .contains(OxAlphaProfile.DEFAULT_MODEL), "default Ox Alpha model is not allowlisted");
        require(OxAlphaWorker.command(OxAlphaWorker.message(checkpoint), "session", true)
                .contains(OxAlphaProfile.DEFAULT_FALLBACK_MODEL),
                "fallback model is not allowlisted");
        require(OxAlphaWorker.message(checkpoint).contains(
                "Nested task, explore, or subagent delegation is forbidden"),
                "worker message omitted delegation prohibition");
        require(OxAlphaWorker.ancestor(root, head, head), "exact control base was rejected");
        require(!OxAlphaWorker.ancestor(root, "0".repeat(40), head),
                "missing control base was accepted");
        stdinTest();
        terminalGateTest();
        providerFailureTest("--self-test-provider-child", false);
        providerFailureTest("--self-test-provider-exit-child", true);
        captureCollisionTest();
        launcherTimeoutTest(root);
        providerSessionTest();
    }

    private static void requireMalformedSessionRejected() {
        boolean rejected = false;
        try {
            OxAlphaRequest.parse(new String[] {"--control-base", "0".repeat(40),
                    "--session", "not-a-session"});
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        require(rejected, "malformed session reached the OpenCode process boundary");
    }

    private static void requireRolloverArgumentsBound() {
        String sha = "0".repeat(40);
        OxAlphaRequest parsed = OxAlphaRequest.parse(new String[] {"--control-base", sha,
                "--attempt", "2", "--launch", "3", "--session", "ses_rollover",
                "--rollover-receipt", "receipt.json", "--rollover-sha256", "1".repeat(64)});
        require(parsed.attempt() == 2 && parsed.launch() == 3
                && parsed.evidenceStem().endsWith("-attempt2-launch3"),
                "rollover launch ordinal was not parsed independently");
        boolean rejected = false;
        try {
            OxAlphaRequest.parse(new String[] {"--control-base", sha,
                    "--rollover-receipt", "receipt.json"});
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        require(rejected, "unhashed rollover receipt was accepted");
    }

    private static void stdinTest() throws Exception {
        Process child = child("--self-test-stdin-child").start();
        OxAlphaWorker.closeStdin(child);
        require(child.waitFor(5, TimeUnit.SECONDS), "launcher left the child stdin open");
        require(child.exitValue() == 0, "stdin closure child failed");
    }

    private static void terminalGateTest() throws Exception {
        Path directory = Files.createTempDirectory("worldline-ox-terminal-");
        Path stdout = directory.resolve("stdout.jsonl");
        Path stderr = directory.resolve("stderr.log");
        try {
            Process runaway = child("--self-test-terminal-child").start();
            OxAlphaProviderCapture errors = OxAlphaProviderCapture.start(
                    runaway, stderr, OxAlphaProfile.DEFAULT_MODEL);
            OxAlphaTerminalMonitor.Capture capture = OxAlphaTerminalMonitor.capture(runaway, stdout);
            OxAlphaWorker.closeStdin(runaway);
            OxAlphaTerminalMonitor.Outcome stopped = OxAlphaTerminalMonitor.waitFor(
                    runaway, capture, errors, 10, 1);
            require(stopped.stoppedAfterTerminal(), "terminal Gate failure did not stop worker");
        } finally {
            Files.deleteIfExists(stdout);
            Files.deleteIfExists(stderr);
            Files.deleteIfExists(directory);
        }
    }

    private static void providerFailureTest(String mode, boolean naturalExit) throws Exception {
        Path directory = Files.createTempDirectory("worldline-ox-provider-");
        Path stdout = directory.resolve("stdout.jsonl");
        Path stderr = directory.resolve("stderr.log");
        try {
            Process failed = child(mode).start();
            OxAlphaProviderCapture errors = OxAlphaProviderCapture.start(
                    failed, stderr, OxAlphaProfile.DEFAULT_MODEL);
            OxAlphaTerminalMonitor.Capture capture = OxAlphaTerminalMonitor.capture(failed, stdout);
            OxAlphaWorker.closeStdin(failed);
            OxAlphaTerminalMonitor.Outcome stopped = OxAlphaTerminalMonitor.waitFor(
                    failed, capture, errors, 10, 1);
            require(stopped.stoppedAfterProviderFailure()
                    && "provider-usage-limit".equals(stopped.supervisorStop()),
                    "provider quota failure did not stop worker with its exact class");
            require("ses_providerchild".equals(OxAlphaProviderFailure.resolveSession(
                    java.util.Set.of(), null, stderr, OxAlphaProfile.DEFAULT_MODEL)),
                    "provider failure lost its session");
            requireDead(pid(stderr, "selftest.child.pid="),
                    "provider failure left its child alive");
            requireDead(pid(stderr, "selftest.grandchild.pid="),
                    "provider failure left its grandchild alive");
            require(!naturalExit || stopped.exit() != 0,
                    "naturally exiting provider failure reported success");
        } finally {
            Files.deleteIfExists(stdout);
            Files.deleteIfExists(stderr);
            Files.deleteIfExists(directory);
        }
    }

    private static long pid(Path stderr, String prefix) throws Exception {
        for (String line : Files.readAllLines(stderr, StandardCharsets.UTF_8)) {
            if (line.startsWith(prefix)) {
                return Long.parseLong(line.substring(prefix.length()));
            }
        }
        throw new IllegalStateException("provider fixture did not report " + prefix);
    }

    private static void captureCollisionTest() throws Exception {
        Path directory = Files.createTempDirectory("worldline-ox-capture-collision-");
        Path stdout = directory.resolve("stdout.jsonl");
        Path stderr = directory.resolve("stderr.log");
        Process root = null;
        OxAlphaTerminalMonitor.Capture capture = null;
        OxAlphaProviderCapture errors = null;
        try {
            Files.writeString(stdout, "immutable", StandardCharsets.UTF_8);
            root = child("--self-test-provider-child").start();
            errors = OxAlphaProviderCapture.start(root, stderr, OxAlphaProfile.DEFAULT_MODEL);
            OxAlphaWorker.closeStdin(root);
            awaitPid(stderr, "selftest.grandchild.pid=");
            boolean rejected = false;
            try {
                capture = OxAlphaTerminalMonitor.capture(root, stdout);
            } catch (java.nio.file.FileAlreadyExistsException expected) {
                rejected = true;
            } finally {
                OxAlphaTerminalMonitor.abort(root, capture, errors);
            }
            require(rejected, "stdout evidence collision was accepted");
            requireDead(pid(stderr, "selftest.child.pid="),
                    "capture collision left its child alive");
            requireDead(pid(stderr, "selftest.grandchild.pid="),
                    "capture collision left its grandchild alive");
        } finally {
            if (root != null && root.isAlive()) {
                OxAlphaTerminalMonitor.abort(root, capture, errors);
            }
            Files.deleteIfExists(stdout);
            Files.deleteIfExists(stderr);
            Files.deleteIfExists(directory);
        }
    }

    private static void launcherTimeoutTest(Path root) throws Exception {
        Path readiness = Files.createTempFile("worldline-launcher-timeout-", ".ready");
        Files.delete(readiness);
        String marker = readiness.toString();
        try {
            List<String> command = List.of(OxAlphaWorker.javaTool(), "-cp",
                    System.getProperty("java.class.path"), "OxAlphaWorker",
                    "--self-test-launcher-timeout-child", marker);
            boolean timedOut = false;
            try {
                OxAlphaLauncher.run(root, command, 5);
            } catch (IllegalStateException expected) {
                timedOut = expected.getMessage().contains("timed out");
            }
            require(timedOut, "source launcher timeout fixture did not time out");
            require(Files.isRegularFile(readiness),
                    "source launcher fixture did not spawn its child and grandchild before timeout");
            require(ProcessHandle.allProcesses().noneMatch(handle -> handle.info().commandLine()
                    .orElse("").contains(marker)),
                    "source launcher timeout left an observed process alive");
        } finally {
            Files.deleteIfExists(readiness);
        }
    }

    private static void providerSessionTest() {
        boolean rejected = false;
        try {
            OxAlphaWorker.requireProviderSession(
                    new OxAlphaTerminalMonitor.Outcome(70, true, "provider-stream-error"), null);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "provider failure without an exact session was receipted");
    }

    private static void awaitPid(Path stderr, String prefix) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (Files.exists(stderr) && Files.readString(stderr, StandardCharsets.UTF_8)
                    .contains(prefix)) {
                return;
            }
            Thread.sleep(25);
        }
        throw new IllegalStateException("process-tree fixture did not become ready: " + prefix);
    }

    private static void requireDead(long pid, String message) {
        require(ProcessHandle.of(pid).isEmpty()
                || !ProcessHandle.of(pid).orElseThrow().isAlive(), message);
    }

    private static ProcessBuilder child(String mode) {
        return new ProcessBuilder(OxAlphaWorker.javaTool(), "-cp",
                System.getProperty("java.class.path"), "OxAlphaWorker", mode);
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

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
