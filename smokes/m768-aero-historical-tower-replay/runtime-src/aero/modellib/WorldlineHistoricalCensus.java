package aero.modellib;

import aero.modellib.render.Aero_AnimationRenderBudget;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import worldline.m768.WorldlineChunkStats;
import worldline.m768.WorldlineHistoricalState;
import worldline.m768.WorldlineRendererStats;

/** Complete allocation-bounded frame census backed by Aero's real counters. */
public final class WorldlineHistoricalCensus {
    private static final String[] METRICS = {
        "phase", "frame.nanos", "tick.nanos", "save.nanos", "save.skipped",
        "chunks.dirty", "chunks.written", "chunkcompile.calls",
        "chunkcompile.maxnanos", "chunkcompile.backlog", "terrain.nanos",
        "aero.prepare.nanos", "aero.enqueue.nanos", "aero.flush.nanos",
        "aero.rebuild.nanos", "entity.nanos", "display.nanos",
        "allocation.bytes", "heap.usedbytes", "gc.count", "gc.nanos",
        "aero.pages.queued", "aero.pages.calls", "aero.pages.rebuilds",
        "aero.pages.cached", "aero.displaylists.live", "aero.displaylists.peak",
        "aero.batches.queued", "aero.batches.flushed", "aero.batches.count",
        "aero.animation.accepted", "aero.animation.rejected",
        "aero.visibility.visiblechunks", "aero.visibility.recentchunks"
    };
    private static final int CAP = Integer.getInteger("worldline.m768.maxFrames", 2000000);
    private static final int WIDTH = METRICS.length + 2;
    private static final int DRAIN_FRAMES = 20;
    private static final long[] ROWS = new long[Math.multiplyExact(CAP, WIDTH)];
    private static boolean pending, active, sealed;
    private static int count, written, stable;
    private static long start, gcCount, gcTime, enqueue, flush;
    private static long enqueueStart, flushStart;
    private static String arm;

    private WorldlineHistoricalCensus() {}

    public static void arm(String value) {
        require(!pending && !active && !sealed, "duplicate census arm");
        arm = value; pending = true;
    }

    public static void beforeFrame(Minecraft game) {
        if (pending) {
            stable = backlog(game) == 0 ? stable + 1 : 0;
            if (stable < DRAIN_FRAMES) return;
            pending = false; active = true; start = System.nanoTime();
            gcCount = Aero_FrameSpikeLogger.gcCollectionCount();
            gcTime = Aero_FrameSpikeLogger.gcCollectionTimeMillis();
            resetOwnTimers();
            System.out.println("[WorldlineM768] frame-boundary-retained arm=" + arm);
        }
        if (!active) return;
        require(count < CAP, "M768 frame census capacity exceeded");
        long now = System.nanoTime(), currentGcCount = Aero_FrameSpikeLogger.gcCollectionCount();
        long currentGcTime = Aero_FrameSpikeLogger.gcCollectionTimeMillis();
        int base = count * WIDTH, column = 0;
        ROWS[base + column++] = count; ROWS[base + column++] = now - start;
        ROWS[base + column++] = WorldlineHistoricalState.phase();
        ROWS[base + column++] = nonnegative(now - Aero_FrameSpikeLogger.frameStartNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.clientTickNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.worldSaveNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.worldSaveSkipped());
        ROWS[base + column++] = dirty(game);
        ROWS[base + column++] = written; written = 0;
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.chunkCompileCalls());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.chunkCompileMaxNanos());
        ROWS[base + column++] = backlog(game);
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.terrainRenderNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.aeroPrepareNanos());
        ROWS[base + column++] = enqueue;
        ROWS[base + column++] = flush;
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.cellRebuildNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.entityRenderNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.displayUpdateNanos());
        ROWS[base + column++] = nonnegative(Aero_FrameSpikeLogger.frameAllocatedBytes());
        Runtime runtime = Runtime.getRuntime();
        ROWS[base + column++] = runtime.totalMemory() - runtime.freeMemory();
        ROWS[base + column++] = delta(currentGcCount, gcCount);
        ROWS[base + column++] = delta(currentGcTime, gcTime) * 1_000_000L;
        ROWS[base + column++] = nonnegative(Aero_BECellRenderer.queuedLastFrame());
        ROWS[base + column++] = nonnegative(Aero_BECellRenderer.pageCallsThisFrame());
        ROWS[base + column++] = nonnegative(Aero_BECellRenderer.pageRebuildsThisFrame());
        ROWS[base + column++] = nonnegative(Aero_BECellRenderer.cachedPageCount());
        ROWS[base + column++] = nonnegative(Aero_DisplayListBudget.liveLists());
        ROWS[base + column++] = nonnegative(Aero_DisplayListBudget.peakLiveLists());
        ROWS[base + column++] = nonnegative(Aero_AnimatedBatcher.queuedThisFrame());
        ROWS[base + column++] = nonnegative(Aero_AnimatedBatcher.flushedInstancesThisFrame());
        ROWS[base + column++] = nonnegative(Aero_AnimatedBatcher.flushedBatchesThisFrame());
        ROWS[base + column++] = nonnegative(Aero_AnimationRenderBudget.acceptedThisFrame());
        ROWS[base + column++] = nonnegative(Aero_AnimationRenderBudget.rejectedThisFrame());
        ROWS[base + column++] = nonnegative(Aero_ChunkVisibility.visibleChunkCount());
        ROWS[base + column++] = nonnegative(Aero_ChunkVisibility.recentChunkCount());
        require(column == WIDTH, "M768 metric width drift");
        gcCount = currentGcCount; gcTime = currentGcTime; count++; resetOwnTimers();
    }

    public static void seal() {
        require(active && !sealed && count > 0, "invalid census seal");
        active = false; sealed = true;
        long minimum = Long.getLong("worldline.m768.minimumMillis", 600000L) * 1_000_000L;
        require(ROWS[(count - 1) * WIDTH + 1] >= minimum, "retained census shorter than minimum");
        try {
            Path path = Path.of(System.getProperty("worldline.m768.artifact"));
            Files.createDirectories(path.toAbsolutePath().getParent());
            String hash = WorldlineHistoricalArtifact.write(path, METRICS, ROWS, count, WIDTH);
            System.out.println("[WorldlineM768] census arm=" + arm + " frames=" + count
                    + " elapsedNs=" + ROWS[(count - 1) * WIDTH + 1] + " bytes=" + Files.size(path)
                    + " sha256=" + hash);
        } catch (Exception error) {
            throw new IllegalStateException("M768 census write failed", error);
        }
    }

    public static void wroteChunk() { if (active) written++; }
    public static boolean active() { return active; }
    public static void enqueueBegin() { enqueueStart = begin(enqueueStart); }
    public static void enqueueEnd() { enqueue += end(enqueueStart); enqueueStart = 0L; }
    public static void flushBegin() { flushStart = begin(flushStart); }
    public static void flushEnd() { flush += end(flushStart); flushStart = 0L; }

    private static int dirty(Minecraft game) {
        if (game.world == null || !(game.world.getChunkSource() instanceof WorldlineChunkStats)) return 0;
        return ((WorldlineChunkStats) game.world.getChunkSource()).worldlineDirtyChunks();
    }

    private static int backlog(Minecraft game) {
        return game.worldRenderer instanceof WorldlineRendererStats
                ? ((WorldlineRendererStats) game.worldRenderer).worldlineCompileBacklog() : 0;
    }

    private static long begin(long prior) {
        if (!active) return 0L;
        require(prior == 0L, "recursive M768 timer"); return System.nanoTime();
    }

    private static long end(long began) {
        if (!active) return 0L;
        require(began != 0L, "unstarted M768 timer"); return System.nanoTime() - began;
    }

    private static void resetOwnTimers() {
        require(enqueueStart == 0L && flushStart == 0L,
                "unterminated M768 timer");
        enqueue = flush = 0L;
    }

    private static long delta(long current, long prior) { return current >= prior ? current - prior : 0L; }
    private static long nonnegative(long value) { return value < 0L ? 0L : value; }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
