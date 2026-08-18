package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlinePairedProbe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MultiplayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies fixed warmup/window bounds without reading the Aero log in-process. */
@Mixin(GameRenderer.class)
public abstract class WorldlinePairedFrameMixin {
    @Shadow private Minecraft client;
    @Unique private boolean worldlineComplete;
    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineFrame(float delta, CallbackInfo callback) {
        if (worldlineComplete || !Boolean.getBoolean("worldline.combat.enabled")) return;
        if (!WorldlinePairedProbe.readyState() && WorldlinePairedProbe.networkReady()
                && client.world != null && client.world.isRemote && client.player != null
                && client.getNetworkHandler() != null && client.interactionManager instanceof MultiplayerInteractionManager
                && System.getProperty("worldline.combat.username").equals(client.player.name)) {
            WorldlinePairedProbe.ready(); client.currentScreen = null; client.paused = false;
            client.skipGameRender = false; client.options.hideHud = true; client.options.bobView = false; return;
        }
        WorldlinePairedProbe.frame(); if (!WorldlinePairedProbe.complete()) return;
        worldlineComplete = true; System.out.println("[WorldlinePair] complete arm=" + WorldlinePairedProbe.arm()
                + " windowFrames=" + WorldlinePairedProbe.windowFrames());
        client.getNetworkHandler().disconnect(); client.scheduleStop();
    }
}
