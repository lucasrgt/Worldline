package worldline.modloader.testkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Synchronous one-command/one-tick protocol for a legacy client. */
final class LegacyProtocol implements AutoCloseable {
    private final String loader, session;
    private final Socket socket;
    private final BufferedReader input;
    private final BufferedWriter output;

    LegacyProtocol(String loader, String session, Socket socket) throws Exception {
        this.loader = loader; this.session = session; this.socket = socket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    LegacySnapshot ready() throws Exception { return read("READY"); }
    LegacySnapshot tick() throws Exception { send("TICK"); return read("STATE"); }
    void stop() throws Exception { send("CLOSE"); read("CLOSED"); }

    private LegacySnapshot read(String kind) throws Exception {
        return LegacySnapshot.parse(input.readLine(), kind, loader, session);
    }
    private void send(String command) throws Exception {
        output.write(command); output.newLine(); output.flush();
    }
    @Override public void close() {
        try { socket.close(); }
        catch (java.io.IOException error) { throw new IllegalStateException("legacy socket close failed", error); }
    }
}
