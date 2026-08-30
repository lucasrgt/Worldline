package worldline.m769.mixin;

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
import worldline.profiling.ClientProfilerRuntime;
import worldline.profiling.WorldlineProfilerMetrics;

/** Measures vanilla chunk compile work and observed backlog. */
@Mixin(WorldRenderer.class)
public abstract class TimelineWorldRendererMixin {
    @Shadow private List<ChunkBuilder> dirtyChunks;
    @Unique private long worldlineCompileStart;

    @Inject(method = "compileChunks(Lnet/minecraft/entity/LivingEntity;Z)Z", at = @At("HEAD"))
    private void worldlineCompileBegin(LivingEntity entity, boolean forced,
            CallbackInfoReturnable<Boolean> callback) {
        worldlineCompileStart = ClientProfilerRuntime.timer();
        if (ClientProfilerRuntime.frameOpen()) {
            ClientProfilerRuntime.gauge("chunk.backlog.count", dirtyChunks.size());
        }
    }

    @Inject(method = "compileChunks(Lnet/minecraft/entity/LivingEntity;Z)Z", at = @At("RETURN"))
    private void worldlineCompileEnd(LivingEntity entity, boolean forced,
            CallbackInfoReturnable<Boolean> callback) {
        ClientProfilerRuntime.elapsed(WorldlineProfilerMetrics.CHUNK_COMPILE,
                worldlineCompileStart);
    }
}
