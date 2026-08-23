package worldline.b173server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Owns the official dedicated-server process, console, and log lifecycle. */
final class B173ServerProcess {
    private final Path officialJar;
    private final Path directory;
    private final Duration timeout;
    private final B173ServerLog log = new B173ServerLog();
    private Process process;

    B173ServerProcess(Path officialJar, Path directory, Duration timeout) {
        this.officialJar = officialJar;
        this.directory = directory;
        this.timeout = timeout;
    }

    void boot(String properties) {
        boolean running = false;
        try {
            Files.createDirectories(directory);
            Files.copy(officialJar, directory.resolve("server.jar"),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.write(directory.resolve("server.properties"),
                    properties.getBytes(StandardCharsets.UTF_8));
            process = new ProcessBuilder(javaCommand(), "-Djava.awt.headless=true",
                    "-Xms64m", "-Xmx256m", "-jar", "server.jar", "nogui")
                    .directory(directory.toFile()).redirectErrorStream(true).start();
            log.start(process);
            log.await(process, 0, "Done (", timeout);
            require(log.contains("Starting minecraft server version Beta 1.7.3"),
                    "version marker absent");
            running = true;
        } catch (IOException error) {
            throw new IllegalStateException("could not boot server", error);
        } finally {
            if (!running && process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    void send(String command, String marker) {
        int start = log.size();
        write(command);
        log.await(process, start, marker, timeout);
    }

    List<String> players() {
        int start = log.size();
        write("list");
        String line = log.awaitLine(process, start, "Connected players:", timeout);
        String value = line.substring(line.indexOf("Connected players:")
                + "Connected players:".length()).trim();
        if (value.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String item : value.split(",")) result.add(item.trim());
        return Collections.unmodifiableList(result);
    }

    void stop() {
        write("stop");
        try {
            require(process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS),
                    "server did not stop in time");
            require(process.exitValue() == 0,
                    "server exited " + process.exitValue() + "\n" + log.tail());
            require(log.contains("Saving chunks") && log.contains("Stopping server"),
                    "clean shutdown markers absent\n" + log.tail());
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while stopping server", error);
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    private void write(String command) {
        try {
            Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException error) {
            throw new IllegalStateException("could not send server command", error);
        }
    }

    private static String javaCommand() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return Paths.get(System.getProperty("java.home"), "bin",
                windows ? "java.exe" : "java").toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
