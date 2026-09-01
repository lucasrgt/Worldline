package worldline.b173server;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import worldline.api.BlockStateDomainDriver;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** Public state-domain TestKit provider backed by the unmodified b1.7.3 server. */
public final class B173ServerStateDomainTestRuntimeProvider implements TestRuntimeProvider {
    public static final String RUNTIME_ID = "b1.7.3-server-state-domain";

    @Override public String runtimeId() { return RUNTIME_ID; }

    @Override public TestRuntimeSession open(TestRuntimeRequest request) throws Exception {
        if (request == null) throw new NullPointerException("request");
        if (request.modPath() != null) throw new IllegalArgumentException(
                "state-domain provider does not load server mods");
        if (request.seed() != B173StateDomainArena.SEED) throw new IllegalArgumentException(
                "state-domain provider requires seed " + B173StateDomainArena.SEED);
        B173StateDomainLoadout loadout = B173StateDomainLoadout.from(request);
        B173ServerLifecycleSettings settings = B173ServerLifecycleSettings.load();
        Path base = request.worldPath().toAbsolutePath().normalize();
        Files.createDirectories(base);
        Path workspace = Files.createTempDirectory(base, "b173-state-domain-");
        int port = freePort();
        B173DedicatedServer server = new B173DedicatedServer(settings.serverJar, workspace,
                port, request.seed(), settings.timeout, 3, true);
        B173WireClient client = null;
        try {
            server.boot();
            client = B173StateDomainArena.open(server, workspace, port, settings.timeout, loadout);
            final int reconnectPort = port;
            Supplier<B173WireClient> sessions = new Supplier<B173WireClient>() {
                @Override public B173WireClient get() {
                    return new B173WireClient("127.0.0.1", reconnectPort,
                            B173StateDomainArena.USERNAME, settings.timeout);
                }
            };
            return new Session(new B173BlockStateDomainDriver(server, client, sessions), server);
        } catch (Exception error) {
            if (client != null) closeAfter(client, error);
            closeAfter(server, error);
            throw error;
        }
    }

    private static int freePort() throws java.io.IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    private static void closeAfter(AutoCloseable resource, Exception failure) {
        try { resource.close(); } catch (Exception close) { failure.addSuppressed(close); }
    }

    private static final class Session implements TestRuntimeSession {
        private final B173BlockStateDomainDriver driver;
        private final B173DedicatedServer server;
        private boolean closed;

        Session(B173BlockStateDomainDriver driver, B173DedicatedServer server) {
            this.driver = driver; this.server = server;
        }

        @Override public <T> T capability(Class<T> type) {
            if (type == null) throw new NullPointerException("type");
            if (type == BlockStateDomainDriver.class) return type.cast(driver);
            return TestRuntimeSession.super.capability(type);
        }

        @Override public void close() {
            if (closed) return;
            closed = true;
            RuntimeException failure = null;
            try { driver.close(); } catch (RuntimeException error) { failure = error; }
            try { server.close(); } catch (RuntimeException error) {
                if (failure == null) failure = error; else failure.addSuppressed(error);
            }
            if (failure != null) throw failure;
        }
    }
}
