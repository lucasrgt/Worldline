package worldline.stationapi.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MultiplayerInteractionManager;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.util.Session;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gates the real StationAPI game thread at one command per completed client tick. */
@Mixin(Minecraft.class)
public abstract class StationApiDriverMixin {
    @Shadow public World world;
    @Shadow public ClientPlayerEntity player;
    @Shadow public InteractionManager interactionManager;
    @Shadow public Session session;
    @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
    @Shadow public abstract void scheduleStop();
    @Unique private boolean worldlineConnected, worldlineReady;
    @Unique private long worldlineTick;
    @Unique private Socket worldlineSocket;
    @Unique private BufferedReader worldlineInput;
    @Unique private BufferedWriter worldlineOutput;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineControl(CallbackInfo callback) {
        if (!Boolean.getBoolean("worldline.stationapi.enabled")) return;
        try {
            if (!worldlineConnected && world == null) {
                connect();
                return;
            }
            if (!ready()) return;
            if (!worldlineReady) {
                open();
                emit("READY");
                worldlineReady = true;
            }
            else emit("STATE");
            String command = worldlineInput.readLine();
            if ("CLOSE".equals(command)) {
                scheduleStop();
                return;
            }
            if (!"TICK".equals(command)) throw new IllegalStateException("invalid driver command");
            worldlineTick++;
        } catch (Exception error) {
            scheduleStop();
            throw new IllegalStateException("StationAPI driver protocol failed", error);
        }
    }

    @Unique private void connect() {
        worldlineConnected = true;
        session.username = System.getProperty("worldline.stationapi.username");
        int port = Integer.getInteger("worldline.stationapi.serverPort", 25565);
        System.out.println("[WorldlineStationAPI] connect username=" + session.username);
        setScreen(new ConnectScreen((Minecraft) (Object) this, "127.0.0.1", port));
    }

    @Unique private boolean ready() {
        return worldlineConnected && StationApiDriverProbe.ready() && world != null && world.isRemote
                && player != null && interactionManager instanceof MultiplayerInteractionManager
                && session.username.equals(player.name);
    }

    @Unique private void open() throws Exception {
        int port = Integer.getInteger("worldline.stationapi.controlPort", -1);
        if (port < 1) throw new IllegalStateException("missing control port");
        worldlineSocket = new Socket(InetAddress.getLoopbackAddress(), port);
        worldlineSocket.setTcpNoDelay(true);
        worldlineInput = new BufferedReader(new InputStreamReader(
                worldlineSocket.getInputStream(), StandardCharsets.UTF_8));
        worldlineOutput = new BufferedWriter(new OutputStreamWriter(
                worldlineSocket.getOutputStream(), StandardCharsets.UTF_8));
    }

    @Unique private void emit(String kind) throws Exception {
        String id = System.getProperty("worldline.stationapi.session");
        String line = kind + " session=" + id + " username=" + player.name
                + " tick=" + worldlineTick + " time=" + world.getTime() + " entity=" + player.id
                + " health=" + player.health + " selected=" + player.inventory.selectedSlot
                + " x=" + player.x + " y=" + player.y + " z=" + player.z;
        worldlineOutput.write(line);
        worldlineOutput.newLine();
        worldlineOutput.flush();
    }
}
