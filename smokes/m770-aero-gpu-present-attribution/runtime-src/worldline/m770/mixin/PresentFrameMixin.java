package worldline.m770.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m770.GpuQueryCapture;
import worldline.profiling.ClientProfilerRuntime;
import worldline.profiling.WorldlineProfilerMetrics;

/** Binds complete CPU frames to frame-keyed asynchronous GPU queries. */
@Mixin(value = GameRenderer.class, priority = 900)
public abstract class PresentFrameMixin {
    @Unique private long worldlineWorldStart;
    @Unique private long worldlineSequence;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineFrameBegin(float tickDelta, CallbackInfo callback) {
        ClientProfilerRuntime.configure("stationapi", "babric");
        ClientProfilerRuntime.beginFrame();
        if (!ClientProfilerRuntime.frameOpen()) return;
        GpuQueryCapture.begin(worldlineSequence);
        worldlineSequence++;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("RETURN"))
    private void worldlineFrameEnd(float tickDelta, CallbackInfo callback) {
        GpuQueryCapture.end();
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
