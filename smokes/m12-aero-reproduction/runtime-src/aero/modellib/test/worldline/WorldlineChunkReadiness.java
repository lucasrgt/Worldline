package aero.modellib.test.worldline;

import aero.modellib.test.mixin.WorldlineChunkBuilderAccess;
import aero.modellib.test.mixin.WorldlineWorldRendererAccess;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;

/** Tracks dirty age and current visible-chunk readiness in rendered frames. */
public final class WorldlineChunkReadiness {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.chunkReadiness.enabled");
    private static final Map<ChunkBuilder, Integer> DIRTY_SINCE = new IdentityHashMap<>();
    private static ChunkBuilder[] previous;
    private static int frame;

    private WorldlineChunkReadiness() {}

    public static void observe(WorldRenderer renderer) {
        if (!ENABLED || renderer == null) return;
        WorldlineWorldRendererAccess access = (WorldlineWorldRendererAccess) renderer;
        ChunkBuilder[] chunks = access.worldlineChunks();
        List<ChunkBuilder> queue = access.worldlineDirtyChunks();
        if (chunks == null || queue == null) return;
        if (chunks != previous) { DIRTY_SINCE.clear(); previous = chunks; frame = 0; }
        frame++;
        int dirty = 0, visible = 0, visibleDirty = 0, visibleReady = 0;
        int oldest = 0, oldestVisible = 0;
        for (ChunkBuilder chunk : chunks) {
            if (chunk == null) continue;
            if (chunk.inFrustum) visible++;
            if (chunk.dirty) {
                dirty++;
                Integer since = DIRTY_SINCE.get(chunk);
                if (since == null) { since = frame; DIRTY_SINCE.put(chunk, since); }
                int age = frame - since;
                oldest = Math.max(oldest, age);
                if (chunk.inFrustum) { visibleDirty++; oldestVisible = Math.max(oldestVisible, age); }
            } else {
                DIRTY_SINCE.remove(chunk);
                if (chunk.inFrustum && ((WorldlineChunkBuilderAccess) chunk).worldlineBuilt())
                    visibleReady++;
            }
        }
        WorldlineChunkProbe.readiness(queue.size(), dirty, visible, visibleDirty, visibleReady,
                oldest, oldestVisible);
    }
}
