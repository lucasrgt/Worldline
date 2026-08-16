package aero.modellib.test.mixin;

import aero.modellib.experimental.Aero_ChunkWorkContract;
import aero.modellib.experimental.Aero_ChunkWorkContract.Result;
import aero.modellib.test.worldline.WorldlineChunkProbe;
import aero.modellib.test.worldline.WorldlineChunkWork;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Maps the M15 explicit contract to vanilla's Boolean caller boundary. */
@Mixin(GameRenderer.class)
public abstract class WorldlineChunkCallerMixin {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.chunkContract.enabled");

    @Redirect(method = "renderFrame(FJ)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;compileChunks"
                    + "(Lnet/minecraft/entity/LivingEntity;Z)Z"))
    private boolean worldlineSchedule(WorldRenderer renderer, LivingEntity camera, boolean forced) {
        if (!ENABLED) return renderer.compileChunks(camera, forced);
        WorldlineChunkWork queue = new WorldlineChunkWork(renderer, camera, forced);
        WorldlineChunkProbe.beginCompile(queue.size(), forced);
        Result result = Aero_ChunkWorkContract.execute(queue,
                Math.max(1, Integer.getInteger("worldline.chunkContract.batch", 2)));
        WorldlineChunkProbe.contract(result.status.name(), result.accepted, result.remaining);
        WorldlineChunkProbe.endCompile(result.remaining, result.endCurrentFrame());
        return result.endCurrentFrame();
    }
}
