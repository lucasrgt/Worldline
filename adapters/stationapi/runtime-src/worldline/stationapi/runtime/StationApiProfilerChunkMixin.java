package worldline.stationapi.runtime;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.profiling.WorldlineProfilerMetrics;
import worldline.profiling.ClientProfilerRuntime;

/** Measures each concrete vanilla chunk display-list rebuild. */
@Mixin(ChunkBuilder.class)
public abstract class StationApiProfilerChunkMixin {
    @Unique private long worldlineRebuildStart;

    @Inject(method = "rebuild()V", at = @At("HEAD"))
    private void worldlineRebuildBegin(CallbackInfo callback) {
        worldlineRebuildStart = ClientProfilerRuntime.timer();
        ClientProfilerRuntime.count("chunk.rebuild.calls");
    }

    @Inject(method = "rebuild()V", at = @At("RETURN"))
    private void worldlineRebuildEnd(CallbackInfo callback) {
        ClientProfilerRuntime.elapsed(WorldlineProfilerMetrics.CHUNK_REBUILD,
                worldlineRebuildStart);
    }
}
