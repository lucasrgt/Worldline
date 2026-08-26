package worldline.b173server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Official-server profile boundary for whitelist and capacity admission. */
public final class B173AdmissionServer implements AutoCloseable {
    private final Path directory;
    private final B173ServerProcess process;
    private final String properties;
    private final List<String> whitelist;
    private boolean running;

    public B173AdmissionServer(Path officialJar, Path directory, int port, long seed,
            Duration timeout, int maximumPlayers, boolean whitelistEnabled,
            List<String> listedPlayers) {
        if (officialJar == null || directory == null || timeout == null
                || listedPlayers == null) {
            throw new IllegalArgumentException("null admission profile");
        }
        Set<String> names = new LinkedHashSet<>();
        for (String player : listedPlayers) {
            if (player == null || !player.matches("[A-Za-z0-9_]{1,16}")
                    || !names.add(player)) {
                throw new IllegalArgumentException("invalid whitelist identity");
            }
        }
        this.directory = directory.toAbsolutePath().normalize();
        this.process = new B173ServerProcess(
                officialJar.toAbsolutePath().normalize(), this.directory, timeout);
        this.properties = B173ServerProperties.admission(
                seed, port, maximumPlayers, whitelistEnabled);
        this.whitelist = Collections.unmodifiableList(new ArrayList<>(names));
    }

    public void boot() {
        if (running) {
            throw new IllegalStateException("admission server already running");
        }
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve("white-list.txt"), whitelist,
                    StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalStateException("could not prepare whitelist", error);
        }
        process.boot(properties);
        running = true;
    }

    public List<String> players() {
        if (!running) {
            throw new IllegalStateException("admission server is not running");
        }
        return process.players();
    }

    public void awaitPlayers(int expected) {
        if (expected < 0) {
            throw new IllegalArgumentException("negative player count");
        }
        long deadline = System.currentTimeMillis() + 5_000L;
        while (System.currentTimeMillis() < deadline) {
            if (players().size() == expected) {
                return;
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("player census interrupted", error);
            }
        }
        throw new IllegalStateException("player count drift");
    }

    @Override public void close() {
        if (!running) {
            return;
        }
        process.stop();
        running = false;
    }
}
