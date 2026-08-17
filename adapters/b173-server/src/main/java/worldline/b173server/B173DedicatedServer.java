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
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.ServerPlayerState;
import worldline.api.ServerLifecycle;
import worldline.api.ServerState;

/** Process adapter for the unmodified official Beta 1.7.3 dedicated server. */
public final class B173DedicatedServer implements PersistentMultiplayerServerRuntime {
    private final Path officialJar, directory;
    private final int port;
    private final long seed;
    private final Duration timeout;
    private final B173ServerLog log = new B173ServerLog();
    private ServerLifecycle lifecycle = ServerLifecycle.NEW;
    private Process process;
    private int saves;

    public B173DedicatedServer(Path officialJar, Path directory, int port, long seed, Duration timeout) {
        if (!Files.isRegularFile(officialJar)) throw new IllegalArgumentException("server JAR is absent");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid port");
        this.officialJar = officialJar.toAbsolutePath().normalize();
        this.directory = directory.toAbsolutePath().normalize();
        this.port = port;
        this.seed = seed;
        this.timeout = timeout;
    }

    @Override
    public void boot() {
        require(lifecycle == ServerLifecycle.NEW, "server was already started");
        try {
            Files.createDirectories(directory);
            Files.copy(officialJar, directory.resolve("server.jar"), StandardCopyOption.REPLACE_EXISTING);
            Files.write(directory.resolve("server.properties"), properties().getBytes(StandardCharsets.UTF_8));
            process = new ProcessBuilder(javaCommand(), "-Djava.awt.headless=true", "-Xms64m", "-Xmx256m",
                    "-jar", "server.jar", "nogui").directory(directory.toFile())
                    .redirectErrorStream(true).start();
            log.start(process);
            log.await(process, 0, "Done (", timeout);
            require(log.contains("Starting minecraft server version Beta 1.7.3"), "version marker absent");
            lifecycle = ServerLifecycle.RUNNING;
        } catch (IOException error) { throw new IllegalStateException("could not boot server", error); }
    }

    @Override
    public void setTime(long worldTime) {
        if (worldTime < 0L) throw new IllegalArgumentException("negative world time");
        send("time set " + worldTime, "CONSOLE: Set time to " + worldTime);
    }

    @Override
    public void save() {
        send("save-all", "Save complete.");
        saves++;
    }

    @Override
    public ServerState state() {
        Path level = directory.resolve("world/level.dat");
        long time = Files.isRegularFile(level) ? B173LevelDat.worldTime(level) : ServerState.UNKNOWN_TIME;
        return new ServerState(lifecycle, port, false, time, saves);
    }

    @Override
    public List<String> players() {
        require(lifecycle == ServerLifecycle.RUNNING, "server is not running");
        int start = log.size();
        write("list");
        String line = log.awaitLine(process, start, "Connected players:", timeout);
        String value = line.substring(line.indexOf("Connected players:") + "Connected players:".length()).trim();
        if (value.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String item : value.split(",")) result.add(item.trim());
        return Collections.unmodifiableList(result);
    }

    @Override
    public ServerPlayerState player(String username) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid player username");
        Path path = directory.resolve("world/players").resolve(username + ".dat").normalize();
        require(path.startsWith(directory) && Files.isRegularFile(path), "persisted player is absent");
        return B173PlayerDat.read(path, username);
    }

    @Override
    public void close() {
        if (lifecycle != ServerLifecycle.RUNNING) return;
        write("stop");
        try {
            require(process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS), "server did not stop in time");
            require(process.exitValue() == 0, "server exited " + process.exitValue() + "\n" + log.tail());
            require(log.contains("Saving chunks") && log.contains("Stopping server"),
                    "clean shutdown markers absent\n" + log.tail());
            lifecycle = ServerLifecycle.STOPPED;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while stopping server", error);
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    private void send(String command, String marker) {
        require(lifecycle == ServerLifecycle.RUNNING, "server is not running");
        int start = log.size();
        write(command);
        log.await(process, start, marker, timeout);
    }

    private void write(String command) {
        try {
            Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(command + "\n"); writer.flush();
        } catch (IOException error) { throw new IllegalStateException("could not send server command", error); }
    }

    private String properties() {
        return "allow-nether=false\nlevel-name=world\nlevel-seed=" + seed
                + "\nmax-players=4\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=" + port
                + "\nspawn-animals=false\nspawn-monsters=false\nview-distance=3\n";
    }

    private String javaCommand() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return Paths.get(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java").toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
