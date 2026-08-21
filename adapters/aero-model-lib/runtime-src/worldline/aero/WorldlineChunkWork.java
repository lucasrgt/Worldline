package worldline.aero;

import aero.modellib.experimental.Aero_ChunkWorkContract.Queue;
import worldline.aero.mixin.WorldlineWorldRendererAccess;
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
    private final boolean visibleFirst;
    private final long budgetNs;
    private int visibleAccepted;
    private boolean budgetStopped;

    public WorldlineChunkWork(WorldRenderer renderer, LivingEntity camera, boolean forced,
            boolean visibleFirst, long budgetUs) {
        this.dirty = ((WorldlineWorldRendererAccess) renderer).worldlineDirtyChunks();
        this.camera = camera; this.forced = forced; this.visibleFirst = visibleFirst;
        this.budgetNs = Math.max(0L, budgetUs) * 1000L;
    }

    public int size() { return dirty.size(); }
    public int visibleDebt() {
        int result = 0;
        for (ChunkBuilder chunk : dirty) if (chunk != null && chunk.inFrustum) result++;
        return result;
    }
    public int visibleAccepted() { return visibleAccepted; }
    public boolean budgetStopped() { return budgetStopped; }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int accept(int limit) {
        List<ChunkBuilder> selected = new ArrayList<>(limit);
        DirtyChunkSorter sorter = new DirtyChunkSorter(camera);
        boolean requireVisible = visibleFirst && visibleDebt() > 0;
        for (ChunkBuilder chunk : dirty) {
            if (chunk == null || ((forced || requireVisible) && !chunk.inFrustum)) continue;
            int rank = 0;
            while (rank < selected.size() && sorter.compare(chunk, selected.get(rank)) <= 0) rank++;
            selected.add(rank, chunk);
            if (selected.size() > limit) selected.remove(limit);
        }
        long start = System.nanoTime(); int accepted = 0;
        for (ChunkBuilder chunk : selected) {
            if (accepted > 0 && budgetNs > 0L && System.nanoTime() - start >= budgetNs) {
                budgetStopped = true; break;
            }
            chunk.rebuild(); chunk.dirty = false; dirty.remove(chunk);
            accepted++; if (chunk.inFrustum) visibleAccepted++;
        }
        return accepted;
    }

    @Override public int remaining() { return dirty.size(); }
}
