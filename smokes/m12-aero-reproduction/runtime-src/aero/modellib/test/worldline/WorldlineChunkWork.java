package aero.modellib.test.worldline;

import aero.modellib.experimental.Aero_ChunkWorkContract.Queue;
import aero.modellib.test.mixin.WorldlineWorldRendererAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.world.DirtyChunkSorter;
import net.minecraft.entity.LivingEntity;

/** Adapts the mapped dirty queue to the explicit Aero work contract. */
public final class WorldlineChunkWork implements Queue {
    private final List<ChunkBuilder> dirty;
    private final LivingEntity camera;
    private final boolean forced;

    public WorldlineChunkWork(WorldRenderer renderer, LivingEntity camera, boolean forced) {
        this.dirty = ((WorldlineWorldRendererAccess) renderer).worldlineDirtyChunks();
        this.camera = camera; this.forced = forced;
    }

    public int size() { return dirty.size(); }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int accept(int limit) {
        List<ChunkBuilder> selected = new ArrayList<>(limit);
        DirtyChunkSorter sorter = new DirtyChunkSorter(camera);
        for (ChunkBuilder chunk : dirty) {
            if (chunk == null || (forced && !chunk.inFrustum)) continue;
            int rank = 0;
            while (rank < selected.size() && sorter.compare(chunk, selected.get(rank)) <= 0) rank++;
            selected.add(rank, chunk);
            if (selected.size() > limit) selected.remove(limit);
        }
        for (ChunkBuilder chunk : selected) {
            chunk.rebuild(); chunk.dirty = false; dirty.remove(chunk);
        }
        return selected.size();
    }

    @Override public int remaining() { return dirty.size(); }
}
