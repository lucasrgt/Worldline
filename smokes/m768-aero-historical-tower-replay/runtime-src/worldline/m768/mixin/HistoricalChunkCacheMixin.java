package worldline.m768.mixin;

import aero.modellib.WorldlineHistoricalCensus;
import java.util.List;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m768.WorldlineChunkStats;

/** Counts dirty loaded chunks and actual vanilla chunk writes. */
@Mixin(ChunkCache.class)
public abstract class HistoricalChunkCacheMixin implements WorldlineChunkStats {
    @Shadow private List chunks;
    @Override public int worldlineDirtyChunks() {
        int count = 0;
        for (Object value : chunks) if (((Chunk) value).dirty) count++;
        return count;
    }
    @Inject(method = "saveChunk(Lnet/minecraft/world/chunk/Chunk;)V", at = @At("RETURN"))
    private void worldlineChunkWritten(Chunk chunk, CallbackInfo callback) {
        WorldlineHistoricalCensus.wroteChunk();
    }
}
