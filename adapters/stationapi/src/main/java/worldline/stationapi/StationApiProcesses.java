package worldline.stationapi;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Owns one fresh official server and one fresh StationAPI client process tree. */
final class StationApiProcesses implements AutoCloseable {
    private final StationApiSettings settings;
    private final StationApiProcess server, client;
    private boolean closed;

    private StationApiProcesses(StationApiSettings settings, StationApiProcess server,
            StationApiProcess client) {
        this.settings = settings; this.server = server; this.client = client;
    }

    static StationApiProcesses start(StationApiSettings settings, Path root, long seed,
            String session, String username, int controlPort) throws Exception {
        require(Files.notExists(root) || empty(root), "StationAPI session directory is not fresh");
        Files.createDirectories(root);
        int serverPort = freePort(); Path serverRoot = root.resolve("server");
        Files.createDirectories(serverRoot);
        Files.copy(settings.serverJar, serverRoot.resolve("server.jar"),
                StandardCopyOption.REPLACE_EXISTING);
        String properties = "server-port=" + serverPort + "\nserver-ip=127.0.0.1\n"
                + "online-mode=false\nlevel-seed=" + seed + "\nlevel-name=world\n"
                + "spawn-monsters=false\nspawn-animals=false\npvp=false\nview-distance=3\n";
        Files.writeString(serverRoot.resolve("server.properties"), properties,
                StandardCharsets.ISO_8859_1);
        StationApiProcess server = null, client = null;
        try {
            server = StationApiProcess.start(serverRoot, root.resolve("server.log"),
                    Arrays.asList(javaTool(), "-Xms64m", "-Xmx256m", "-jar", "server.jar", "nogui"));
            server.awaitText("Done (", settings.timeoutSeconds);
            List<String> command = new ArrayList<String>();
            command.add(settings.wrapper().toString()); command.add("--no-daemon");
            command.add("--init-script"); command.add(settings.initScript.toString());
            command.add("runClient");
            command.add("-PworldlineDriverRoot=" + settings.initScript.getParent());
            command.add("-PworldlineClientSha256=" + settings.clientSha256);
            command.add("-PworldlineControlPort=" + controlPort);
            command.add("-PworldlineServerPort=" + serverPort);
            command.add("-PworldlineUsername=" + username);
            command.add("-PworldlineSession=" + session);
            client = StationApiProcess.start(settings.project(), root.resolve("client.log"), command);
            return new StationApiProcesses(settings, server, client);
        } catch (Exception error) {
            if (client != null) client.close(); if (server != null) server.close(); throw error;
        }
    }

    void verifyClientStarted() throws Exception {
        client.awaitText("[WorldlineStationAPI] connect", settings.timeoutSeconds);
    }

    @Override public void close() {
        if (closed) return; closed = true; Throwable failure = null;
        try { client.awaitExit(60, "StationAPI client"); }
        catch (Throwable error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            failure = error; client.close();
        }
        try { server.write("save-all\nstop"); server.awaitExit(45, "official server"); }
        catch (Throwable error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            if (failure == null) failure = error; else failure.addSuppressed(error); server.close();
        }
        if (failure instanceof Error) throw (Error) failure;
        if (failure != null) throw new IllegalStateException("StationAPI process cleanup failed", failure);
    }

    private static boolean empty(Path root) throws IOException {
        try (java.util.stream.Stream<Path> paths = Files.list(root)) { return !paths.findAny().isPresent(); }
    }
    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private static String javaTool() {
        return Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name", "").startsWith("Windows") ? "java.exe" : "java").toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
