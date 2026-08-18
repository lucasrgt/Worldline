package worldline.m74;

/** Primitive per-frame direct timers aligned one-for-one with the M74 census. */
public final class WorldlineStageTimer {
    static final long[] renderer = new long[WorldlineFrameCensus.CAP], queue = new long[WorldlineFrameCensus.CAP], flush = new long[WorldlineFrameCensus.CAP];
    static final short[] rendererCalls = new short[WorldlineFrameCensus.CAP], queueCalls = new short[WorldlineFrameCensus.CAP], flushCalls = new short[WorldlineFrameCensus.CAP];
    private static boolean armed, capture; private static long rendererNow, queueNow, flushNow, rendererStart, queueStart, flushStart;
    private static int rendererCount, queueCount, flushCount;
    static { for (int i = 0; i < WorldlineFrameCensus.CAP; i += 256) { renderer[i] = queue[i] = flush[i] = 0L;
        rendererCalls[i] = queueCalls[i] = flushCalls[i] = 0; } }
    private WorldlineStageTimer() {}
    public static boolean censusArmed() { return WorldlineFrameCensus.armed; }
    public static void armAligned() { if (armed) throw new IllegalStateException("duplicate M77 arm");
        if (WorldlineFrameCensus.running) { if (WorldlineFrameCensus.count != 0 || WorldlineFrameCensus.pending || WorldlineFrameCensus.sealed)
                throw new IllegalStateException("late M77 timing setup"); WorldlineFrameCensus.running = false;
            WorldlineFrameCensus.start = WorldlineFrameCensus.last = WorldlineFrameCensus.end = 0L; } armed = true; }
    public static void head() { if (!armed || WorldlineFrameCensus.sealed) return; if (!WorldlineFrameCensus.running) return;
        if (capture) throw new IllegalStateException("M77 HEAD without TAIL"); rendererNow = queueNow = flushNow = 0L;
        rendererCount = queueCount = flushCount = 0; rendererStart = queueStart = flushStart = 0L; capture = true; }
    public static void tail() { if (!capture) return; if (rendererStart != 0 || queueStart != 0 || flushStart != 0)
            throw new IllegalStateException("unterminated M77 span"); int i = WorldlineFrameCensus.count;
        if (i >= WorldlineFrameCensus.CAP || rendererCount > 0xffff || queueCount > 0xffff || flushCount > 0xffff)
            throw new IllegalStateException("M77 counter overflow"); renderer[i] = rendererNow; queue[i] = queueNow; flush[i] = flushNow;
        rendererCalls[i] = (short) rendererCount; queueCalls[i] = (short) queueCount; flushCalls[i] = (short) flushCount; capture = false; }
    public static void rendererBegin() { if (capture) rendererStart = begin(rendererStart, "renderer"); }
    public static void rendererEnd() { if (capture) { rendererNow = add(rendererNow, end(rendererStart, "renderer")); rendererStart = 0L; rendererCount++; } }
    public static void queueBegin() { if (capture) queueStart = begin(queueStart, "queue"); }
    public static void queueEnd() { if (capture) { queueNow = add(queueNow, end(queueStart, "queue")); queueStart = 0L; queueCount++; } }
    public static void flushBegin() { if (capture) flushStart = begin(flushStart, "flush"); }
    public static void flushEnd() { if (capture) { flushNow = add(flushNow, end(flushStart, "flush")); flushStart = 0L; flushCount++; } }
    private static long begin(long start, String label) { if (start != 0) throw new IllegalStateException("recursive M77 " + label); return System.nanoTime(); }
    private static long end(long start, String label) { if (start == 0) throw new IllegalStateException("unstarted M77 " + label); long value = System.nanoTime() - start;
        if (value < 0) throw new IllegalStateException("negative M77 " + label); return value; }
    private static long add(long left, long right) { return Math.addExact(left, right); }
}
