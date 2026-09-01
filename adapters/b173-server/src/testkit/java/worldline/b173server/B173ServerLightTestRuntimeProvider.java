package worldline.b173server;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import worldline.api.BlockLightDriver;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** Public light TestKit provider backed by the unmodified b1.7.3 server. */
public final class B173ServerLightTestRuntimeProvider implements TestRuntimeProvider {
    public static final String RUNTIME_ID = "b1.7.3-server-light";

    @Override public String runtimeId() { return RUNTIME_ID; }

    @Override public TestRuntimeSession open(TestRuntimeRequest request) throws Exception {
        if (request == null) throw new NullPointerException("request");
        if (request.modPath() != null) throw new IllegalArgumentException(
                "light provider does not load server mods");
        if (request.seed() != B173LightArena.SEED) throw new IllegalArgumentException(
                "light provider requires seed " + B173LightArena.SEED);
        B173LightLoadout loadout = B173LightLoadout.from(request);
        B173ServerLifecycleSettings settings = B173ServerLifecycleSettings.load();
        Path base = request.worldPath().toAbsolutePath().normalize(); Files.createDirectories(base);
        Path workspace = Files.createTempDirectory(base, "b173-light-"); int port = freePort();
        B173DedicatedServer server = new B173DedicatedServer(settings.serverJar, workspace,
                port, request.seed(), settings.timeout, 3, true);
        B173LightArena.Start start = null;
        try {
            server.boot(); start = B173LightArena.open(workspace, port, settings.timeout, loadout);
            final int reconnectPort = port;
            Supplier<B173WireClient> sessions = new Supplier<B173WireClient>() {
                @Override public B173WireClient get() {
                    return new B173WireClient("127.0.0.1", reconnectPort,
                            B173LightArena.USERNAME, settings.timeout);
                }
            };
            B173BlockLightDriver driver = new B173BlockLightDriver(server, start.client, sessions);
            return new Session(driver, server);
        } catch (Exception error) {
            if (start != null) closeAfter(start.client, error); closeAfter(server, error); throw error;
        }
    }

    private static int freePort() throws java.io.IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private static void closeAfter(AutoCloseable resource, Exception failure) {
        try { resource.close(); } catch (Exception close) { failure.addSuppressed(close); }
    }

    private static final class Session implements TestRuntimeSession {
        private final B173BlockLightDriver driver;
        private final B173DedicatedServer server;
        private boolean closed;
        Session(B173BlockLightDriver driver, B173DedicatedServer server) {
            this.driver = driver; this.server = server;
        }
        @Override public <T> T capability(Class<T> type) {
            if (type == null) throw new NullPointerException("type");
            if (type == BlockLightDriver.class) return type.cast(driver);
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
