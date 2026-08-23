import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Runs bounded child processes and preserves useful diagnostics on every exit path. */
final class ProcessCapture {
    private ProcessCapture() { }

    static String require(Path directory, List<String> command, int timeoutSeconds) throws Exception {
        Result result = run(directory, command, timeoutSeconds);
        if (result.timedOut) throw new IllegalStateException(command.get(0) + " timed out after "
                + timeoutSeconds + "s\n" + tail(result.output, 8_000));
        if (result.exit != 0) throw new IllegalStateException(command.get(0) + " exited "
                + result.exit + "\n" + result.output);
        return result.output;
    }

    static Result run(Path directory, List<String> command, int timeoutSeconds) throws Exception {
        Path log = Files.createTempFile("worldline-process-", ".log");
        Process process;
        try {
            process = new ProcessBuilder(new ArrayList<>(command)).directory(directory.toFile())
                    .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        } catch (IOException error) {
            Files.deleteIfExists(log);
            throw new IllegalStateException("could not start " + command.get(0), error);
        }
        try {
            boolean finished;
            try { finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS); }
            catch (InterruptedException error) {
                destroy(process); Thread.currentThread().interrupt(); throw error;
            }
            if (!finished) destroy(process);
            String output = Files.readString(log, StandardCharsets.UTF_8);
            return new Result(finished ? process.exitValue() : 124, output, !finished);
        } finally { Files.deleteIfExists(log); }
    }

    static void destroy(Process process) throws InterruptedException {
        List<ProcessHandle> descendants = process.descendants()
                .sorted(Comparator.comparingLong(ProcessHandle::pid).reversed()).toList();
        descendants.forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
        if (!process.waitFor(10, TimeUnit.SECONDS))
            throw new IllegalStateException("process did not terminate: " + process.pid());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (descendants.stream().anyMatch(ProcessHandle::isAlive) && System.nanoTime() < deadline)
            Thread.sleep(20L);
        if (descendants.stream().anyMatch(ProcessHandle::isAlive))
            throw new IllegalStateException("process descendants did not terminate: " + process.pid());
    }

    static String tail(String value, int maximum) {
        if (value.length() <= maximum) return value;
        return "... tail ...\n" + value.substring(value.length() - maximum);
    }

    static int environmentTimeout() {
        String value = System.getenv("WORLDLINE_VERIFY_PROCESS_TIMEOUT_SECONDS");
        if (value == null || value.isBlank()) return 600;
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= 1 && parsed <= 7_200) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(
                "WORLDLINE_VERIFY_PROCESS_TIMEOUT_SECONDS must be between 1 and 7200");
    }

    record Result(int exit, String output, boolean timedOut) { }
}
