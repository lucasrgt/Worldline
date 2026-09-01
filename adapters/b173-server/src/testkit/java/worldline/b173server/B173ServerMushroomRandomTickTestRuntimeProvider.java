package worldline.b173server;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import worldline.api.BlockRandomTickSpreadDriver;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** Official-server provider for the roofed mushroom random-tick arena. */
public final class B173ServerMushroomRandomTickTestRuntimeProvider
        implements TestRuntimeProvider {
    public static final String RUNTIME_ID = "b1.7.3-server-mushroom-random-tick";
    public static final String SERVER_PROPERTY = B173ServerLifecycleSettings.SERVER_PROPERTY;
    @Override public String runtimeId() { return RUNTIME_ID; }

    @Override public TestRuntimeSession open(TestRuntimeRequest request) throws Exception {
        if (request == null) throw new NullPointerException("request");
        if (request.modPath() != null) throw new IllegalArgumentException(
                "mushroom random-tick provider does not load server mods");
        if (request.seed() != B173MushroomRandomTickArena.SEED) {
            throw new IllegalArgumentException("mushroom random-tick seed drift");
        }
        B173MushroomRandomTickLoadout loadout = B173MushroomRandomTickLoadout.from(request);
        B173ServerLifecycleSettings settings = B173ServerLifecycleSettings.load();
        Path base = request.worldPath().toAbsolutePath().normalize(); Files.createDirectories(base);
        Path workspace = Files.createTempDirectory(base, "b173-mushroom-random-tick-");
        int port = freePort();
        B173DedicatedServer server = new B173DedicatedServer(settings.serverJar, workspace,
                port, request.seed(), settings.timeout, 3, true);
        B173WireClient client = null;
        try {
            server.boot();
            B173MushroomRandomTickArena.Start start = B173MushroomRandomTickArena.open(
                    workspace, port, settings.timeout, loadout);
            client = start.client;
            final int reconnectPort = port;
            Supplier<B173WireClient> sessions = () -> new B173WireClient("127.0.0.1",
                    reconnectPort, B173MushroomRandomTickArena.USERNAME, settings.timeout);
            client.close();
            B173FixtureSupport.awaitPlayers(server, 0);
            server.save();
            client = sessions.get();
            client.connect();
            client.synchronizePose();
            client.awaitInventory();
            client.awaitRemoteChunk(0, 0);
            return new Session(new B173BlockRandomTickSpreadDriver(
                    server, client, start.origin, sessions), server);
        } catch (Exception error) {
            if (client != null) closeAfter(client, error); closeAfter(server, error); throw error;
        }
    }
    private static int freePort() throws java.io.IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private static void closeAfter(AutoCloseable value, Exception failure) {
        try { value.close(); } catch (Exception close) { failure.addSuppressed(close); }
    }
    private static final class Session implements TestRuntimeSession {
        private final B173BlockRandomTickSpreadDriver driver;
        private final B173DedicatedServer server; private boolean closed;
        Session(B173BlockRandomTickSpreadDriver driver, B173DedicatedServer server) {
            this.driver = driver; this.server = server;
        }
        @Override public <T> T capability(Class<T> type) {
            if (type == BlockRandomTickSpreadDriver.class) return type.cast(driver);
            return TestRuntimeSession.super.capability(type);
        }
        @Override public void close() {
            if (closed) return; closed = true; RuntimeException failure = null;
            try { driver.close(); } catch (RuntimeException error) { failure = error; }
            try { server.close(); } catch (RuntimeException error) {
                if (failure == null) failure = error; else failure.addSuppressed(error);
            }
            if (failure != null) throw failure;
        }
    }
}
