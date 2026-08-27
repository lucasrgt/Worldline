import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Stops an OpenCode worker after terminal evidence and drains observed descendants. */
final class OxAlphaTerminalMonitor {
    static final int TERMINAL_GRACE_SECONDS = 30;
    private static final Pattern EXIT = Pattern.compile("\\\"exit\\\":(-?[0-9]+)");

    private OxAlphaTerminalMonitor() {
    }

    static Capture capture(Process process, Path stdout) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(stdout, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        Capture capture = new Capture(writer);
        try {
            capture.thread = Thread.ofPlatform().daemon().name("ox-alpha-jsonl-capture")
                    .start(() -> capture.copy(process));
            return capture;
        } catch (RuntimeException failure) {
            writer.close();
            throw failure;
        }
    }

    static Outcome waitFor(Process process, Capture capture, OxAlphaProviderCapture errors,
            int timeoutSeconds) throws Exception {
        return waitFor(process, capture, errors, timeoutSeconds,
                TERMINAL_GRACE_SECONDS);
    }

    static Outcome waitFor(Process process, Capture capture, OxAlphaProviderCapture errors,
            int timeoutSeconds, int graceSeconds) throws Exception {
        Instant timeout = Instant.now().plusSeconds(timeoutSeconds);
        Instant terminalDeadline = null;
        Set<ProcessHandle> observed = new LinkedHashSet<>();
        try {
            while (Instant.now().isBefore(timeout)) {
                observe(process, observed);
                if (process.waitFor(100, TimeUnit.MILLISECONDS)) {
                    observe(process, observed);
                    stopTree(process, observed);
                    capture.await();
                    errors.await();
                    String stop = errors.classification();
                    return new Outcome(supervisorExit(process.exitValue(), stop), true, stop);
                }
                String stop = errors.classification();
                if (stop != null) {
                    stopTree(process, observed);
                    capture.await();
                    errors.await();
                    return new Outcome(supervisorExit(process.exitValue(), stop), true, stop);
                }
                if (terminalDeadline == null && capture.terminal.get()) {
                    terminalDeadline = Instant.now().plusSeconds(graceSeconds);
                }
                if (terminalDeadline != null && !Instant.now().isBefore(terminalDeadline)) {
                    stopTree(process, observed);
                    capture.await();
                    errors.await();
                    return new Outcome(process.exitValue(), true, "terminal-gate-failure");
                }
            }
            stopTree(process, observed);
            capture.await();
            errors.await();
            return new Outcome(process.exitValue(), false, "timeout");
        } catch (Exception failure) {
            stopTree(process, observed);
            capture.await();
            errors.await();
            throw failure;
        }
    }

    static void abort(Process process, Capture capture, OxAlphaProviderCapture errors)
            throws Exception {
        Set<ProcessHandle> observed = new LinkedHashSet<>();
        stopTree(process, observed);
        if (capture != null) {
            capture.await();
        }
        if (errors != null) {
            errors.await();
        }
    }

    static boolean terminalGateFailure(String line) {
        if (!line.contains("java tools/harness/Gate.java --candidate ")
                && !line.contains("java tools/harness/Gate.java --milestone ")) {
            return false;
        }
        Matcher exits = EXIT.matcher(line);
        while (exits.find()) {
            if (Integer.parseInt(exits.group(1)) != 0) {
                return true;
            }
        }
        return false;
    }

    static void selfTest() throws Exception {
        String gate = "java tools/harness/Gate.java --milestone m1-contract ";
        require(terminalGateFailure(gate + "{\"exit\":1}"), "terminal Gate failure was missed");
        require(!terminalGateFailure(gate + "{\"exit\":0}"), "passing Gate was terminal");
        require(!terminalGateFailure("repository read {\"exit\":1}"),
                "ordinary tool failure was terminal");
        providerLogTests();
    }

    private static void providerLogTests() throws Exception {
        OxAlphaProviderLogMonitor monitor = new OxAlphaProviderLogMonitor(OxAlphaProfile.DEFAULT_MODEL);
        accept(monitor, providerLine("glm-5.3-flash", "opencode-go"));
        require(monitor.classification() == null, "partial provider line was classified before EOF");
        require("provider-usage-limit".equals(monitor.finish()),
                "provider line without newline was missed at EOF");
        require(classify(providerLine("deepseek-v4-flash", "opencode-go") + "\n") == null,
                "failure for the wrong selected model was accepted");
        require(classify(providerLine("glm-5.3-flash", "other-provider") + "\n") == null,
                "failure for the wrong provider was accepted");
        require(classify("level=INFO detail=\"level=ERROR message=\\\"stream error\\\" "
                + "providerID=opencode-go modelID=glm-5.3-flash usage limit\"\n") == null,
                "embedded provider-like text was accepted");
        OxAlphaProviderLogMonitor overflowMonitor = new OxAlphaProviderLogMonitor(
                OxAlphaProfile.DEFAULT_MODEL, 4);
        accept(overflowMonitor, "healthy-log-volume");
        String overflow = overflowMonitor.finish();
        require("supervisor-evidence-overflow".equals(overflow),
                "oversized private evidence was not stopped fail-closed");
        require(!new Outcome(70, true, overflow).stoppedAfterProviderFailure(),
                "healthy evidence overflow was counted as provider recurrence");
    }

    private static String classify(String text) {
        OxAlphaProviderLogMonitor monitor = new OxAlphaProviderLogMonitor(OxAlphaProfile.DEFAULT_MODEL);
        accept(monitor, text);
        return monitor.finish();
    }

    private static void accept(OxAlphaProviderLogMonitor monitor, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        monitor.accept(bytes, bytes.length);
    }

    private static String providerLine(String model, String provider) {
        return "level=ERROR message=\"stream error\" providerID=" + provider
                + " modelID=" + model + " session.id=ses_providerchild "
                + "error.error=\"Monthly usage limit reached\"";
    }

    private static int supervisorExit(int processExit, String supervisorStop) {
        return supervisorStop != null && processExit == 0 ? 70 : processExit;
    }

    private static void observe(Process process, Set<ProcessHandle> observed) {
        if (process.isAlive()) {
            process.toHandle().descendants().forEach(observed::add);
        }
        List<ProcessHandle> additions = new ArrayList<>();
        observed.forEach(handle -> handle.descendants().forEach(additions::add));
        additions.stream().filter(handle -> !observed.contains(handle)).forEach(observed::add);
    }

    private static void stopTree(Process process, Set<ProcessHandle> observed) throws Exception {
        observe(process, observed);
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        Instant deadline = Instant.now().plusSeconds(10);
        while (Instant.now().isBefore(deadline)) {
            observe(process, observed);
            observed.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
            if (!process.isAlive() && observed.stream().noneMatch(ProcessHandle::isAlive)) {
                return;
            }
            Thread.sleep(50);
        }
        require(!process.isAlive() && observed.stream().noneMatch(ProcessHandle::isAlive),
                "supervised root and observed descendants did not stop");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    static final class Capture {
        private final BufferedWriter writer;
        private final AtomicBoolean terminal = new AtomicBoolean();
        private volatile IOException failure;
        private Thread thread;

        private Capture(BufferedWriter writer) {
            this.writer = writer;
        }

        private void copy(Process process) {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), StandardCharsets.UTF_8)); writer) {
                String line;
                while ((line = input.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    if (terminalGateFailure(line)) {
                        terminal.set(true);
                    }
                }
            } catch (IOException error) {
                failure = error;
            }
        }

        private void await() throws Exception {
            thread.join(TimeUnit.SECONDS.toMillis(10));
            require(!thread.isAlive(), "stdout capture did not stop");
            if (failure != null) {
                throw failure;
            }
        }
    }

    record Outcome(int exit, boolean completed, String supervisorStop) {
        boolean stoppedAfterTerminal() {
            return "terminal-gate-failure".equals(supervisorStop);
        }

        boolean stoppedAfterProviderFailure() {
            return supervisorStop != null && supervisorStop.startsWith("provider-");
        }
    }
}
