import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Stops an OpenCode worker that keeps exploring after an official Gate failure. */
final class OxAlphaTerminalMonitor {
    static final int TERMINAL_GRACE_SECONDS = 30;
    private static final Pattern EXIT = Pattern.compile("\\\"exit\\\":(-?[0-9]+)");

    private OxAlphaTerminalMonitor() {
    }

    static Capture capture(Process process, Path stdout) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(stdout, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        Capture capture = new Capture(writer);
        capture.thread = Thread.ofPlatform().daemon().name("ox-alpha-jsonl-capture")
                .start(() -> capture.copy(process));
        return capture;
    }

    static Outcome waitFor(Process process, Capture capture, int timeoutSeconds) throws Exception {
        return waitFor(process, capture, timeoutSeconds, TERMINAL_GRACE_SECONDS);
    }

    static Outcome waitFor(Process process, Capture capture, int timeoutSeconds, int graceSeconds)
            throws Exception {
        Instant timeout = Instant.now().plusSeconds(timeoutSeconds);
        Instant terminalDeadline = null;
        while (Instant.now().isBefore(timeout)) {
            if (process.waitFor(1, TimeUnit.SECONDS)) {
                capture.await();
                return new Outcome(process.exitValue(), true, null);
            }
            if (terminalDeadline == null && capture.terminal.get()) {
                terminalDeadline = Instant.now().plusSeconds(graceSeconds);
            }
            if (terminalDeadline != null && !Instant.now().isBefore(terminalDeadline)) {
                terminate(process);
                require(process.waitFor(10, TimeUnit.SECONDS), "terminal worker did not stop");
                capture.await();
                return new Outcome(process.exitValue(), true, "terminal-gate-failure");
            }
        }
        terminate(process);
        process.waitFor(10, TimeUnit.SECONDS);
        capture.await();
        return new Outcome(process.isAlive() ? 124 : process.exitValue(), false, "timeout");
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

    static void selfTest() {
        String gate = "java tools/harness/Gate.java --milestone m1-contract ";
        require(terminalGateFailure(gate + "{\"exit\":1}"), "terminal Gate failure was missed");
        require(!terminalGateFailure(gate + "{\"exit\":0}"), "passing Gate was terminal");
        require(!terminalGateFailure("repository read {\"exit\":1}"), "ordinary tool failure was terminal");
    }

    private static void terminate(Process process) {
        process.toHandle().descendants()
                .sorted((left, right) -> Long.compare(right.pid(), left.pid()))
                .forEach(handle -> handle.destroyForcibly());
        process.destroyForcibly();
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
    }
}
