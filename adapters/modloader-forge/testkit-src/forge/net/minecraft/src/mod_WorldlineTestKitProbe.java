package net.minecraft.src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import net.minecraft.client.Minecraft;
import worldline.profiling.ClientProfilerRuntime;

/** Controlled single-player Forge client boundary for the Worldline TestKit. */
public final class mod_WorldlineTestKitProbe extends BaseMod {
    private boolean started, ready, closing;
    private long controlledTick;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;

    public mod_WorldlineTestKitProbe() {
        requireLoader("forge");
        forge.MinecraftForge.versionDetectStrict("WorldlineTestKit", 1, 0, 6);
        System.out.println("WORLDLINE_LEGACY_TESTKIT_BOOT=forge");
        ModLoader.SetInGUIHook(this, true, false);
        ModLoader.SetInGameHook(this, true, true);
    }

    public String Version() { return "worldline-testkit-v1"; }

    public boolean OnTickInGUI(Minecraft client, GuiScreen screen) {
        if (started) return false;
        started = true;
        client.playerController = new PlayerControllerSP(client);
        client.startWorld(property("world"), "Worldline TestKit", Long.parseLong(property("seed")));
        client.displayGuiScreen((GuiScreen)null);
        return false;
    }

    public boolean OnTickInGame(Minecraft client) {
        if (closing || client.theWorld == null || client.thePlayer == null) return !closing;
        try {
            if (!ready) {
                open(); ClientProfilerRuntime.startCapture(); emit("READY", client); ready = true;
            } else emit("STATE", client);
            String command = input.readLine();
            if ("CLOSE".equals(command)) {
                ClientProfilerRuntime.finish("controlled-close"); emit("CLOSED", client);
                System.out.println("WORLDLINE_LEGACY_TESTKIT_SHUTDOWN=forge");
                closing = true; client.shutdown(); return false;
            }
            if (!"TICK".equals(command)) throw new IllegalStateException("invalid command");
            controlledTick++; return true;
        } catch (Exception error) {
            ClientProfilerRuntime.finish("protocol-failure"); client.shutdown();
            throw new IllegalStateException("legacy TestKit protocol failed", error);
        }
    }

    private void open() throws Exception {
        int port = Integer.parseInt(property("controlPort"));
        socket = new Socket(InetAddress.getLoopbackAddress(), port); socket.setTcpNoDelay(true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
    }

    private void emit(String kind, Minecraft client) throws Exception {
        EntityPlayerSP player = client.thePlayer;
        output.write(kind + " loader=forge session=" + property("session")
                + " username=" + player.username + " tick=" + controlledTick
                + " time=" + client.theWorld.getWorldTime() + " entity=" + player.entityId
                + " health=" + player.health + " selected=" + player.inventory.currentItem
                + " x=" + player.posX + " y=" + player.posY + " z=" + player.posZ);
        output.newLine(); output.flush();
    }

    private static void requireLoader(String expected) {
        if (!expected.equals(property("loader"))) throw new IllegalStateException("loader drift");
    }
    private static String property(String suffix) {
        String value = System.getProperty("worldline.legacy.testkit." + suffix);
        if (value == null || value.length() == 0) throw new IllegalStateException("missing " + suffix);
        return value;
    }
}
