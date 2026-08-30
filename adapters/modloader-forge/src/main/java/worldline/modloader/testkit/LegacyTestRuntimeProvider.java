package worldline.modloader.testkit;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;

/** Shared factory implementation for fresh ModLoader and Forge client sessions. */
abstract class LegacyTestRuntimeProvider implements TestRuntimeProvider {
    private static final AtomicInteger SESSIONS = new AtomicInteger();
    private final String loader;
    LegacyTestRuntimeProvider(String loader) { this.loader = loader; }
    @Override public String runtimeId() { return loader + "-b1.7.3"; }

    @Override public TestRuntimeSession open(TestRuntimeRequest request) throws Exception {
        if (request == null) throw new NullPointerException("request");
        LegacyClientSettings settings = LegacyClientSettings.load(loader);
        int number = SESSIONS.incrementAndGet();
        String session = String.format("%s%02d", loader.substring(0, 1), number);
        String username = String.format("Wl%s%02d", loader.equals("forge") ? "Fg" : "Ml", number);
        Path requestedWorld = request.worldPath().toAbsolutePath().normalize();
        Path root = requestedWorld.resolve(loader + "-" + session);
        Path parent = requestedWorld.getParent();
        if (parent == null) throw new IllegalArgumentException("legacy world has no parent");
        Files.createDirectories(parent);
        ServerSocket control = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        control.setSoTimeout(1000);
        LegacyClientProcess process = null; LegacyProtocol protocol = null;
        try {
            process = LegacyClientProcess.start(settings, root, control.getLocalPort(),
                    request.seed(), session, username);
            Socket socket = accept(control, process, settings.timeoutSeconds);
            socket.setSoTimeout(settings.timeoutSeconds * 1000);
            protocol = new LegacyProtocol(loader, session, socket);
            LegacySnapshot ready = protocol.ready();
            LegacyRuntime runtime = new LegacyRuntime(
                    requestedWorld, protocol, process, settings.timeoutSeconds, ready);
            System.out.println("WORLDLINE_LEGACY_SESSION_OPEN=" + loader + ":" + session);
            return new Session(runtime, loader);
        } catch (Exception | Error error) {
            if (protocol != null) try { protocol.close(); } catch (Exception close) { error.addSuppressed(close); }
            if (process != null) try { process.close(); } catch (Exception close) { error.addSuppressed(close); }
            throw error;
        } finally { control.close(); }
    }

    private static Socket accept(ServerSocket control, LegacyClientProcess process, int seconds)
            throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (System.nanoTime() < deadline) {
            try { return control.accept(); }
            catch (SocketTimeoutException retry) { process.requireRunning(); }
        }
        throw new IllegalStateException("legacy control connection timed out");
    }

    private static final class Session implements TestRuntimeSession {
        private final LegacyRuntime runtime; private final String loader;
        Session(LegacyRuntime runtime, String loader) { this.runtime = runtime; this.loader = loader; }
        @Override public AutomatedMinecraftRuntime runtime() { return runtime; }
        @Override public void close() {
            runtime.close(); System.out.println("WORLDLINE_LEGACY_SESSION_CLOSE=" + loader + ":CLOSED");
        }
    }
}
