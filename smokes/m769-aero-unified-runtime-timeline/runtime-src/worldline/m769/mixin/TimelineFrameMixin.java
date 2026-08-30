package worldline.m769.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m769.TimelineAeroMetrics;
import worldline.m769.TimelineJfrCapture;
import worldline.m769.TimelineState;
import worldline.profiling.ClientProfilerRuntime;
import worldline.profiling.WorldlineProfilerMetrics;

/** Owns the complete frame root and periodic WLPR-to-JFR anchors. */
@Mixin(value = GameRenderer.class, priority = 900)
public abstract class TimelineFrameMixin {
    @Unique private long worldlineWorldStart;
    @Unique private long worldlineSequence;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineFrameBegin(float tickDelta, CallbackInfo callback) {
        ClientProfilerRuntime.configure("stationapi", "babric");
        ClientProfilerRuntime.beginFrame();
        if (!ClientProfilerRuntime.frameOpen()) return;
        TimelineAeroMetrics.snapshotPriorFrame();
        TimelineJfrCapture.frame(worldlineSequence, System.nanoTime(),
                System.currentTimeMillis(), TimelineState.phase());
        worldlineSequence++;
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
