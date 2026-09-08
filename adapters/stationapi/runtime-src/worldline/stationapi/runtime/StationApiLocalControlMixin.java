package worldline.stationapi.runtime;

import java.io.File;
import worldline.stationapi.control.StationApiLocalChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.client.util.Session;
import net.minecraft.world.storage.WorldStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opt-in native local world, isolated storage and game-thread control; no vanilla behavior override. */
@Mixin(Minecraft.class)
public abstract class StationApiLocalControlMixin {
    @Shadow private WorldStorageSource worldStorageSource;
    @Shadow public Session session;
    @Unique private boolean worldlineStarted;
    @Unique private StationApiLocalChannel worldlineChannel;
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineLocalControl(CallbackInfo callback) {
        if (!Boolean.getBoolean("worldline.stationapi.localWorld")) return;
        if (worldlineChannel != null && worldlineChannel.isClosed()) return;
        Minecraft client = (Minecraft) (Object) this;
        try {
            if (!worldlineStarted) {
                worldlineStarted = true;
                File saves = new File(System.getProperty("worldline.stationapi.worldPath"));
                if (!saves.isAbsolute() || saves.exists()) throw new IllegalStateException("local world is not fresh");
                worldStorageSource = (WorldStorageSource) worldStorageSource.getClass()
                        .getConstructor(File.class).newInstance(saves);
                session.username = System.getProperty("worldline.stationapi.username");
                client.interactionManager = new SingleplayerInteractionManager(client);
                client.startGame("world", "Worldline StationAPI", Long.getLong("worldline.stationapi.seed", 173L));
                client.setScreen(null);
                System.out.println("[WorldlineStationAPI] connect local username=" + session.username);
                return;
            }
            if (client.world == null || client.player == null || client.world.isRemote)
                throw new IllegalStateException("StationAPI local world was lost");
            if (worldlineChannel == null) worldlineChannel = new StationApiLocalChannel(client);
            worldlineChannel.boundary();
        } catch (Exception error) {
            client.scheduleStop(); throw new IllegalStateException("StationAPI local control failed", error);
        }
    }
}
