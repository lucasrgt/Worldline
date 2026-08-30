package worldline.m772;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.chunk.Chunk;

/** Allocation-tolerant capture outside the measured save and render paths. */
public final class AutosaveProbe {
    private static final long[] FRAMES = new long[200_000];
    private static final Chunk[] OBSERVED_CHUNKS = new Chunk[2_048];
    private static final boolean[] OBSERVED_BEFORE = new boolean[2_048];
    private static final Chunk[] SAVED_CHUNKS = new Chunk[2_048];
    private static boolean active;
    private static int frameCount, nonForcedCalls, maxNonForcedBatch;
    private static int totalNonForcedWritten, forcedCalls, forcedBefore, forcedAfter;
    private static long saveStart, maxNonForcedNanos;
    private static int saveBefore, observedCount, savedCount;

    private AutosaveProbe() {}

    public static void start() {
        active = true;
        frameCount = 0;
        nonForcedCalls = 0;
        maxNonForcedBatch = 0;
        totalNonForcedWritten = 0;
        forcedCalls = 0;
        forcedBefore = 0;
        forcedAfter = 0;
        savedCount = 0;
        maxNonForcedNanos = 0L;
    }

    public static void frame(long nanos) {
        if (active && nanos >= 0L && frameCount < FRAMES.length) FRAMES[frameCount++] = nanos;
    }

    public static void beginSave(boolean force, List<Chunk> chunks) {
        if (!active) return;
        saveBefore = dirty(chunks);
        observedCount = Math.min(chunks.size(), OBSERVED_CHUNKS.length);
        for (int index = 0; index < observedCount; index++) {
            OBSERVED_CHUNKS[index] = chunks.get(index);
            OBSERVED_BEFORE[index] = OBSERVED_CHUNKS[index].dirty;
        }
        saveStart = System.nanoTime();
    }

    public static void endSave(boolean force, List<Chunk> chunks) {
        if (!active || saveStart == 0L) return;
        long elapsed = System.nanoTime() - saveStart;
        int after = dirty(chunks);
        int written = Math.max(0, saveBefore - after);
        if (force) {
            forcedCalls++;
            forcedBefore = saveBefore;
            forcedAfter = after;
        } else {
            nonForcedCalls++;
            totalNonForcedWritten += written;
            maxNonForcedBatch = Math.max(maxNonForcedBatch, written);
            maxNonForcedNanos = Math.max(maxNonForcedNanos, elapsed);
            for (int index = 0; index < observedCount; index++) {
                if (OBSERVED_BEFORE[index] && !OBSERVED_CHUNKS[index].dirty) {
                    remember(OBSERVED_CHUNKS[index]);
                }
            }
        }
        saveStart = 0L;
    }

    public static void finish(Path metrics, Path frames, String arm) throws IOException {
        active = false;
        String text = "arm=" + arm + "\nnon.forced.calls=" + nonForcedCalls
            + "\nnon.forced.max.batch=" + maxNonForcedBatch
            + "\nnon.forced.total.written=" + totalNonForcedWritten
            + "\nnon.forced.unique.chunks=" + savedCount
            + "\nnon.forced.max.nanos=" + maxNonForcedNanos
            + "\nforced.calls=" + forcedCalls + "\nforced.before=" + forcedBefore
            + "\nforced.after=" + forcedAfter + "\nframes=" + frameCount + "\n";
        Files.createDirectories(metrics.toAbsolutePath().getParent());
        Files.writeString(metrics, text, StandardCharsets.UTF_8);
        List<String> rows = new ArrayList<>(frameCount);
        for (int index = 0; index < frameCount; index++) rows.add(Long.toString(FRAMES[index]));
        Files.write(frames, rows, StandardCharsets.UTF_8);
    }

    private static int dirty(List<Chunk> chunks) {
        int count = 0;
        for (Chunk chunk : chunks) if (chunk != null && chunk.dirty) count++;
        return count;
    }

    private static void remember(Chunk chunk) {
        for (int index = 0; index < savedCount; index++) {
            if (SAVED_CHUNKS[index] == chunk) {
                return;
            }
        }
        if (savedCount < SAVED_CHUNKS.length) {
            SAVED_CHUNKS[savedCount++] = chunk;
        }
    }
}
