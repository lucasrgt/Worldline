package worldline.aero.mixin;

import aero.modellib.experimental.Aero_ChunkWorkContract;
import aero.modellib.experimental.Aero_ChunkWorkContract.Result;
import worldline.aero.WorldlineChunkProbe;
import worldline.aero.WorldlineChunkWork;
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
    private static final boolean ADAPTIVE = Boolean.getBoolean("worldline.chunkContract.adaptive");

    @Redirect(method = "renderFrame(FJ)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;compileChunks"
                    + "(Lnet/minecraft/entity/LivingEntity;Z)Z"))
    private boolean worldlineSchedule(WorldRenderer renderer, LivingEntity camera, boolean forced) {
        if (!ENABLED) return renderer.compileChunks(camera, forced);
        long budgetUs = Long.getLong("worldline.chunkContract.budgetUs", 0L);
        WorldlineChunkWork queue = new WorldlineChunkWork(renderer, camera, forced, ADAPTIVE, budgetUs);
        int debt = queue.visibleDebt();
        int limit = ADAPTIVE ? adaptiveLimit(debt)
                : Math.max(1, Integer.getInteger("worldline.chunkContract.batch", 2));
        WorldlineChunkProbe.beginCompile(queue.size(), forced);
        Result result = Aero_ChunkWorkContract.execute(queue, limit);
        WorldlineChunkProbe.contract(result.status.name(), result.accepted, result.remaining,
                limit, debt, queue.visibleAccepted(), queue.budgetStopped());
        WorldlineChunkProbe.endCompile(result.remaining, result.endCurrentFrame());
        return result.endCurrentFrame();
    }

    private static int adaptiveLimit(int debt) {
        int maximum = Math.max(2, Integer.getInteger("worldline.chunkContract.maxBatch", 8));
        if (debt == 0) return maximum;
        if (debt > 1024) return maximum;
        if (debt > 512) return Math.min(maximum, 6);
        if (debt > 128) return Math.min(maximum, 4);
        return Math.min(maximum, 2);
    }
}
