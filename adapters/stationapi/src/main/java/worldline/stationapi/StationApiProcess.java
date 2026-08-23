package worldline.stationapi;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Captured child process with bounded output waits and complete tree cleanup. */
final class StationApiProcess implements AutoCloseable {
    private final Process process;
    private final Path log;

    private StationApiProcess(Process process, Path log) { this.process = process; this.log = log; }

    static StationApiProcess start(Path directory, Path log, List<String> command) throws IOException {
        Files.createDirectories(directory); Files.createDirectories(log.getParent());
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        return new StationApiProcess(process, log);
    }

    void awaitText(String marker, int seconds) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (System.nanoTime() < deadline) {
            String text = text();
            if (text.contains(marker)) return;
            if (text.contains("Stopping!") || text.contains("BUILD FAILED"))
                throw new IllegalStateException("process failed before " + marker + "\n" + tail(text));
            if (!process.isAlive()) throw new IllegalStateException(
                    "process exited " + process.exitValue() + " before " + marker + "\n" + tail(text));
            Thread.sleep(100L);
        }
        throw new IllegalStateException("process timed out before " + marker + "\n" + tail(text()));
    }

    void write(String line) throws IOException {
        Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(line); writer.write('\n'); writer.flush();
    }

    void awaitExit(int seconds, String label) throws Exception {
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            destroy(); throw new IllegalStateException(label + " did not stop\n" + tail(text()));
        }
        if (process.exitValue() != 0) throw new IllegalStateException(
                label + " exited " + process.exitValue() + "\n" + tail(text()));
    }

    String text() throws IOException {
        return Files.isRegularFile(log)
                ? new String(Files.readAllBytes(log), StandardCharsets.UTF_8) : "";
    }

    void destroy() {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(handle -> { handle.destroyForcibly(); try { handle.onExit().get(10,
                        TimeUnit.SECONDS); } catch (Exception ignored) { /* best effort after force */ } });
        if (process.isAlive()) process.destroyForcibly();
        try { process.onExit().get(10, TimeUnit.SECONDS); }
        catch (Exception ignored) { /* caller still owns the authoritative failure */ }
    }

    private static String tail(String text) {
        String[] lines = text.split("\\R", -1); int start = Math.max(0, lines.length - 40);
        return String.join("\n", java.util.Arrays.copyOfRange(lines, start, lines.length));
    }

    @Override public void close() { destroy(); }
}
