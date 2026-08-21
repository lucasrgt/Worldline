package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineMultiplayerProbe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MultiplayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Counts completed real renderer frames after remote readiness. */
@Mixin(GameRenderer.class)
public abstract class WorldlineMultiplayerFrameMixin {
    @Shadow private Minecraft client;
    @Unique private static final int WORLDLINE_FRAMES =
            Integer.getInteger("worldline.multiplayer.frames", 60);
    @Unique private boolean worldlineComplete;
    @Unique private boolean worldlineFrameSeen;
    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineFrame(float delta, CallbackInfo callback) {
        if (worldlineComplete || !Boolean.getBoolean("worldline.multiplayer.enabled")) return;
        if (!worldlineFrameSeen) { worldlineFrameSeen = true;
            System.out.println("[WorldlineMultiplayer] renderer-frame"); }
        if (!WorldlineMultiplayerProbe.isReady() && WorldlineMultiplayerProbe.networkReady()
                && client.world != null && client.world.isRemote && client.player != null
                && client.getNetworkHandler() != null
                && client.interactionManager instanceof MultiplayerInteractionManager
                && System.getProperty("worldline.multiplayer.username").equals(client.player.name)) {
            WorldlineMultiplayerProbe.ready(); client.currentScreen = null; client.paused = false;
            client.skipGameRender = false; client.options.hideHud = true; client.options.bobView = false;
            System.out.println("[WorldlineMultiplayer] ready username=" + client.player.name
                    + " entity=" + client.player.id + " chunks=" + WorldlineMultiplayerProbe.chunks()
                    + " aeroBaseline=" + WorldlineMultiplayerProbe.baselineLines());
            return;
        }
        WorldlineMultiplayerProbe.frame();
        if (WorldlineMultiplayerProbe.frames() < WORLDLINE_FRAMES) return;
        int lines = WorldlineMultiplayerProbe.aeroLinesAfterReady();
        if (lines < 1) return;
        worldlineComplete = true;
        System.out.println("[WorldlineMultiplayer] complete frames="
                + WorldlineMultiplayerProbe.frames() + " aeroLinesAfterReady=" + lines);
        client.getNetworkHandler().disconnect(); client.scheduleStop();
    }
}
