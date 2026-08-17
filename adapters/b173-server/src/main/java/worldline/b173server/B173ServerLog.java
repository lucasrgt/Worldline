package worldline.b173server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class B173ServerLog {
    private final List<String> lines = new ArrayList<>();

    void start(Process process) {
        Thread reader = new Thread(() -> read(process), "worldline-b173-server-output");
        reader.setDaemon(true);
        reader.start();
    }

    synchronized int size() { return lines.size(); }

    void await(Process process, int start, String marker, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            synchronized (this) {
                for (int index = Math.min(start, lines.size()); index < lines.size(); index++)
                    if (lines.get(index).contains(marker)) return;
            }
            if (!process.isAlive()) throw new IllegalStateException(
                    "server exited before " + marker + "\n" + tail());
            try { Thread.sleep(100L); }
            catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while waiting for server", error);
            }
        }
        throw new IllegalStateException("timed out waiting for " + marker + "\n" + tail());
    }

    synchronized boolean contains(String marker) {
        return lines.stream().anyMatch(line -> line.contains(marker));
    }

    synchronized String tail() {
        int start = Math.max(0, lines.size() - 30);
        return lines.subList(start, lines.size()).stream().collect(Collectors.joining("\n"));
    }

    private void read(Process process) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(
                process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = input.readLine()) != null) synchronized (this) { lines.add(line); }
        } catch (IOException error) {
            synchronized (this) { lines.add("reader-error: " + error.getMessage()); }
        }
    }
}
