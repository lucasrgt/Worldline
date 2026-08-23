package worldline.stationapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Synchronous one-command/one-state protocol for a gated StationAPI game thread. */
final class StationApiProtocol implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader input;
    private final BufferedWriter output;

    StationApiProtocol(Socket socket) throws Exception {
        this.socket = socket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    StationApiSnapshot ready(String session) throws Exception {
        return StationApiSnapshot.parse(input.readLine(), "READY", session);
    }

    StationApiSnapshot tick(String session) throws Exception {
        send("TICK"); return StationApiSnapshot.parse(input.readLine(), "STATE", session);
    }

    void stop() throws Exception { send("CLOSE"); }

    private void send(String command) throws Exception {
        output.write(command); output.newLine(); output.flush();
    }

    @Override public void close() {
        try { socket.close(); }
        catch (java.io.IOException error) {
            throw new IllegalStateException("StationAPI control socket close failed", error);
        }
    }
}
