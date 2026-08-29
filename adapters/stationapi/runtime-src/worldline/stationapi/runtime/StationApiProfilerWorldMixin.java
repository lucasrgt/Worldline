package worldline.stationapi.runtime;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.profiling.WorldlineProfilerMetrics;
import worldline.stationapi.profiler.StationApiProfilerRuntime;

/** Measures vanilla chunk-compile work and its observed queue depth. */
@Mixin(WorldRenderer.class)
public abstract class StationApiProfilerWorldMixin {
    @Shadow private List<ChunkBuilder> dirtyChunks;
    @Unique private long worldlineCompileStart;

    @Inject(method = "compileChunks(Lnet/minecraft/entity/LivingEntity;Z)Z", at = @At("HEAD"))
    private void worldlineCompileBegin(LivingEntity entity, boolean forced,
            CallbackInfoReturnable<Boolean> callback) {
        worldlineCompileStart = StationApiProfilerRuntime.timer();
        if (StationApiProfilerRuntime.frameOpen())
            StationApiProfilerRuntime.gauge("chunk.backlog.count", dirtyChunks.size());
    }

    @Inject(method = "compileChunks(Lnet/minecraft/entity/LivingEntity;Z)Z", at = @At("RETURN"))
    private void worldlineCompileEnd(LivingEntity entity, boolean forced,
            CallbackInfoReturnable<Boolean> callback) {
        StationApiProfilerRuntime.elapsed(WorldlineProfilerMetrics.CHUNK_COMPILE,
                worldlineCompileStart);
    }
}
