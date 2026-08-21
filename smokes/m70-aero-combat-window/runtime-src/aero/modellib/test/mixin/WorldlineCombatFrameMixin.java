package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineCombatProbe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MultiplayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Arms after remote readiness and closes after the post-event Aero window. */
@Mixin(GameRenderer.class)
public abstract class WorldlineCombatFrameMixin {
    @Shadow private Minecraft client;
    @Unique private boolean worldlineComplete;
    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineFrame(float delta, CallbackInfo callback) {
        if (worldlineComplete || !Boolean.getBoolean("worldline.combat.enabled")) return;
        if (!WorldlineCombatProbe.readyState() && WorldlineCombatProbe.networkReady()
                && client.world != null && client.world.isRemote && client.player != null
                && client.getNetworkHandler() != null
                && client.interactionManager instanceof MultiplayerInteractionManager
                && System.getProperty("worldline.combat.username").equals(client.player.name)) {
            WorldlineCombatProbe.ready(); client.currentScreen = null; client.paused = false;
            client.skipGameRender = false; client.options.hideHud = true; client.options.bobView = false; return;
        }
        WorldlineCombatProbe.frame(); if (!WorldlineCombatProbe.complete()) return;
        worldlineComplete = true; System.out.println("[WorldlineCombat] complete postFrames="
                + WorldlineCombatProbe.postFrames() + " aeroLinesAfterEvent=" + WorldlineCombatProbe.aeroLines());
        client.getNetworkHandler().disconnect(); client.scheduleStop();
    }
}
