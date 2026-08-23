package worldline.stationapi;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** TestKit provider for fresh hash-verified StationAPI client/server process pairs. */
public final class StationApiTestRuntimeProvider implements TestRuntimeProvider {
    private final AtomicInteger sessions = new AtomicInteger();
    @Override public String runtimeId() { return "stationapi-b1.7.3"; }

    @Override public TestRuntimeSession open(TestRuntimeRequest request) throws Exception {
        if (request == null) throw new NullPointerException("request");
        StationApiSettings settings = StationApiSettings.load();
        int number = sessions.incrementAndGet();
        String id = String.format("s%02d", number);
        String username = String.format("WlSta%02d", number);
        Path requestedWorld = request.worldPath().toAbsolutePath().normalize();
        Path root = requestedWorld.resolve(id);
        Path parent = requestedWorld.getParent();
        if (parent == null) throw new IllegalArgumentException("StationAPI world has no parent");
        Files.createDirectories(parent);
        ServerSocket control = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        control.setSoTimeout(settings.timeoutSeconds * 1000);
        StationApiProcesses processes = null; StationApiProtocol protocol = null;
        try {
            processes = StationApiProcesses.start(settings, root, request.seed(), id, username,
                    control.getLocalPort());
            processes.verifyClientStarted();
            Socket socket;
            try { socket = control.accept(); }
            catch (SocketTimeoutException error) {
                throw new IllegalStateException("StationAPI control connection timed out", error);
            }
            socket.setSoTimeout(settings.timeoutSeconds * 1000);
            protocol = new StationApiProtocol(socket);
            StationApiRuntime runtime = new StationApiRuntime(id, requestedWorld, protocol, processes);
            System.out.println("WORLDLINE_STATIONAPI_SESSION_OPEN=" + id);
            return new Session(runtime);
        } catch (Exception | Error error) {
            if (protocol != null) try { protocol.close(); } catch (Exception close) { error.addSuppressed(close); }
            if (processes != null) try { processes.close(); } catch (Exception close) { error.addSuppressed(close); }
            throw error;
        } finally { control.close(); }
    }

    private static final class Session implements TestRuntimeSession {
        private final StationApiRuntime runtime;
        Session(StationApiRuntime runtime) { this.runtime = runtime; }
        @Override public StationApiRuntime runtime() { return runtime; }
        @Override public void close() {
            runtime.close();
            System.out.println("WORLDLINE_STATIONAPI_SESSION_CLOSE=" + runtime.state());
        }
    }
}
