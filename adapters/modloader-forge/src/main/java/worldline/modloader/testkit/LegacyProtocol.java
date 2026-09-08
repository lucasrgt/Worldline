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

    LegacySnapshot progress(java.util.List<String> lines) throws Exception {
        if (lines == null || lines.isEmpty() || lines.size() > 16)
            throw new IllegalArgumentException("progress requires 1..16 lines");
        for (String line : lines) {
            if (line == null || line.length() > 256 || line.indexOf('\n') >= 0 || line.indexOf('\r') >= 0)
                throw new IllegalArgumentException("invalid progress line");
            for (int index = 0; index < line.length(); index++)
                if (line.charAt(index) < 32 || line.charAt(index) > 126)
                    throw new IllegalArgumentException("progress requires visible ASCII");
        }
        String text = String.join("\n", lines);
        send("HUD " + java.util.Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)));
        return read("HUD");
    }

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
