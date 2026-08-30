package worldline.m771.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.profiling.ClientProfilerRuntime;
import worldline.profiling.WorldlineProfilerMetrics;

/** Captures every complete CPU frame in the retained M771 window. */
@Mixin(value = GameRenderer.class, priority = 900)
public abstract class HitchFrameMixin {
    @Unique private long worldlineWorldStart;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineFrameBegin(float tickDelta, CallbackInfo callback) {
        ClientProfilerRuntime.configure("stationapi", "babric");
        ClientProfilerRuntime.beginFrame();
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("RETURN"))
    private void worldlineFrameEnd(float tickDelta, CallbackInfo callback) {
        ClientProfilerRuntime.endFrame();
    }

    @Inject(method = "renderWorld(FI)V", at = @At("HEAD"))
    private void worldlineWorldBegin(float tickDelta, int eye, CallbackInfo callback) {
        worldlineWorldStart = ClientProfilerRuntime.timer();
    }

    @Inject(method = "renderWorld(FI)V", at = @At("RETURN"))
    private void worldlineWorldEnd(float tickDelta, int eye, CallbackInfo callback) {
        ClientProfilerRuntime.elapsed(WorldlineProfilerMetrics.RENDER_WORLD,
                worldlineWorldStart);
    }
}
