package worldline.aero.mixin;

import worldline.aero.WorldlineChunkProbe;
import worldline.aero.WorldlineChunkReadiness;
import worldline.aero.WorldlineFrameOracle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opens and publishes one chunk-probe record per rendered client frame. */
@Mixin(GameRenderer.class)
public abstract class WorldlineChunkProbeFrameMixin {
    @Shadow private Minecraft client;
    @Shadow private long lastInactiveTime;
    @Unique private static final boolean WORLDLINE_CAPTURE =
        Boolean.getBoolean("worldline.capture.enabled");

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineProbeBegin(float tickDelta, CallbackInfo callback) {
        if (WORLDLINE_CAPTURE) lastInactiveTime = System.currentTimeMillis();
        WorldlineFrameOracle.prepare(client);
        WorldlineChunkProbe.beginFrame();
    }

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return WorldlineFrameOracle.fixedDelta() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineProbeEnd(float tickDelta, CallbackInfo callback) {
        WorldlineChunkReadiness.observe(client.worldRenderer);
        WorldlineFrameOracle.capture(client);
        WorldlineChunkProbe.endFrame();
    }
}
