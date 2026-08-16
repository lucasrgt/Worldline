package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineChunkProbe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.world.DirtyChunkSorter;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Measures vanilla's queue and optionally processes one real bounded batch. */
@Mixin(WorldRenderer.class)
public abstract class WorldlineChunkProbeMixin {
    @Shadow private List<ChunkBuilder> dirtyChunks;

    @Inject(method = "compileChunks(Lnet/minecraft/entity/LivingEntity;Z)Z", at = @At("HEAD"),
            cancellable = true)
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void worldlineCompileBegin(LivingEntity entity, boolean forced,
            CallbackInfoReturnable<Boolean> callback) {
        WorldlineChunkProbe.beginCompile(dirtyChunks.size(), forced);
        if (!Boolean.getBoolean("worldline.chunkPolicy.enabled")) return;
        int budget = Math.max(1, Integer.getInteger("worldline.chunkPolicy.batch", 2));
        List<ChunkBuilder> candidates = new ArrayList<>(budget);
        DirtyChunkSorter sorter = new DirtyChunkSorter(entity);
        for (ChunkBuilder chunk : dirtyChunks) {
            if (chunk == null || (forced && !chunk.inFrustum)) continue;
            int rank = 0;
            while (rank < candidates.size() && sorter.compare(chunk, candidates.get(rank)) <= 0) rank++;
            candidates.add(rank, chunk);
            if (candidates.size() > budget) candidates.remove(budget);
        }
        int built = 0;
        for (ChunkBuilder chunk : candidates) {
            chunk.rebuild(); chunk.dirty = false; dirtyChunks.remove(chunk); built++;
        }
        WorldlineChunkProbe.policy(built, dirtyChunks.size());
        WorldlineChunkProbe.endCompile(dirtyChunks.size(), true);
        callback.setReturnValue(Boolean.TRUE);
    }

    @Inject(method = "compileChunks(Lnet/minecraft/entity/LivingEntity;Z)Z", at = @At("RETURN"))
    private void worldlineCompileEnd(LivingEntity entity, boolean forced,
            CallbackInfoReturnable<Boolean> callback) {
        WorldlineChunkProbe.endCompile(dirtyChunks.size(), callback.getReturnValue());
    }

    @Inject(method = "markDirty(IIIIII)V", at = @At("HEAD"))
    private void worldlineMarked(int x1, int y1, int z1, int x2, int y2, int z2,
            CallbackInfo callback) { WorldlineChunkProbe.marked(); }

    @Inject(method = "sortChunks(III)V", at = @At("HEAD"))
    private void worldlineSorted(int x, int y, int z, CallbackInfo callback) {
        WorldlineChunkProbe.sorted();
    }

    @Inject(method = "notifyAmbientDarknessChanged()V", at = @At("HEAD"))
    private void worldlineAmbient(CallbackInfo callback) { WorldlineChunkProbe.ambient(); }

    @Inject(method = "reload()V", at = @At("HEAD"))
    private void worldlineReloaded(CallbackInfo callback) { WorldlineChunkProbe.reloaded(); }
}
