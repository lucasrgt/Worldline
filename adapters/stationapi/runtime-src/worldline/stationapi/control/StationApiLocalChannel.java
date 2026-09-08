package worldline.stationapi.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.TitleScreen;
import worldline.profiling.ClientProfilerRuntime;

/** Request ordering and acknowledgements live entirely on the controlled native game thread. */
public final class StationApiLocalChannel {
    private final Minecraft client;
    private final Socket socket;
    private final BufferedReader input;
    private final BufferedWriter output;
    private final String session = System.getProperty("worldline.stationapi.session");
    private long tick, request;
    private boolean ready, closed;
    public StationApiLocalChannel(Minecraft client) throws Exception {
        this.client = client;
        socket = new Socket(InetAddress.getLoopbackAddress(),
                Integer.getInteger("worldline.stationapi.controlPort", -1));
        socket.setTcpNoDelay(true); socket.setSoTimeout(240000);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        ClientProfilerRuntime.startCapture();
    }
    public boolean isClosed() { return closed; }
    public void boundary() throws Exception {
        if (closed) return;
        emit(ready ? "STATE" : "READY"); ready = true;
        for (;;) {
            String command = input.readLine();
            if ("TICK".equals(command)) { tick++; return; }
            if ("CLOSE".equals(command)) {
                ClientProfilerRuntime.finish("controlled-close"); emit("CLOSED");
                closed = true; socket.close(); client.setScreen(new TitleScreen());
                client.setWorld(null); client.scheduleStop(); return;
            }
            String prefix = "CONTROL session=" + session + " request=" + (request + 1) + " ";
            if (command == null || !command.startsWith(prefix))
                throw new IllegalStateException("command identity drift");
            String value = StationApiLocalActions.execute(client, command.substring(prefix.length())); request++;
            line("RESULT session=" + session + " request=" + request + " value=" + value); emit("STATE");
        }
    }
    private void emit(String kind) throws Exception {
        line(kind + " session=" + session + " username=" + client.player.name + " tick=" + tick
                + " time=" + client.world.getTime() + " entity=" + client.player.id + " health=" + client.player.health
                + " selected=" + client.player.inventory.selectedSlot + " x=" + client.player.x
                + " y=" + client.player.y + " z=" + client.player.z);
    }
    private void line(String value) throws Exception { output.write(value); output.newLine(); output.flush(); }
}
