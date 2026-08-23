package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import worldline.m74.WorldlinePagedBridge;

/** Primitive timing and page-state records aligned to the M74 census. */
public final class WorldlinePagedTimer {
  static final int CAP = 65536;
  static final long[] renderer = new long[CAP], queue = new long[CAP], flush = new long[CAP];
  static final short[] rendererCalls = new short[CAP], queueCalls = new short[CAP],
                       flushCalls = new short[CAP];
  static final int[] queued = new int[CAP], pageCalls = new int[CAP], direct = new int[CAP],
                     rebuilds = new int[CAP], cached = new int[CAP], evicted = new int[CAP];
  private static boolean armed, capture;
  private static long rendererNow, queueNow, flushNow, rendererStart, queueStart, flushStart;
  private static int rendererCount, queueCount, flushCount;
  static {
    for (int i = 0; i < CAP; i += 256) {
      renderer[i] = queue[i] = flush[i] = 0;
      rendererCalls[i] = queueCalls[i] = flushCalls[i] = 0;
      queued[i] = pageCalls[i] = direct[i] = rebuilds[i] = cached[i] = evicted[i] = 0;
    }
  }
  private WorldlinePagedTimer() {
  }
  static void arm() {
    if (armed)
      throw new IllegalStateException("duplicate M97 arm");
    if (!"1".equals(System.getProperty("aero.becell.maxCachedPages"))
        || !"8".equals(System.getProperty("aero.becell.rebuildsPerFrame"))
        || !"100000".equals(System.getProperty("aero.becell.pageTtlFrames")))
      throw new IllegalStateException("M97 runtime drift");
    armed = true;
  }
  public static void head() {
    if (!armed || WorldlinePagedBridge.sealed() || !WorldlinePagedBridge.running())
      return;
    if (capture)
      throw new IllegalStateException("M97 HEAD without TAIL");
    rendererNow = queueNow = flushNow = rendererStart = queueStart = flushStart = 0;
    rendererCount = queueCount = flushCount = 0;
    capture = true;
  }
  public static void tail() {
    if (!capture)
      return;
    if (rendererStart != 0 || queueStart != 0 || flushStart != 0)
      throw new IllegalStateException("unterminated M97 span");
    int i = WorldlinePagedBridge.count();
    if (i >= CAP || rendererCount > 0xffff || queueCount > 0xffff || flushCount > 0xffff)
      throw new IllegalStateException("M97 counter overflow");
    renderer[i] = rendererNow;
    queue[i] = queueNow;
    flush[i] = flushNow;
    rendererCalls[i] = (short) rendererCount;
    queueCalls[i] = (short) queueCount;
    flushCalls[i] = (short) flushCount;
    queued[i] = Aero_BECellRenderer.queuedLastFrame();
    pageCalls[i] = Aero_BECellRenderer.pageCallsThisFrame();
    direct[i] = Aero_BECellRenderer.directFallbacksThisFrame();
    rebuilds[i] = Aero_BECellRenderer.pageRebuildsThisFrame();
    cached[i] = Aero_BECellRenderer.cachedPageCount();
    evicted[i] = Aero_BECellRenderer.evictedCachedPages();
    capture = false;
  }
  public static void rendererBegin() {
    if (capture)
      rendererStart = begin(rendererStart, "renderer");
  }
  public static void rendererEnd() {
    if (capture) {
      rendererNow = add(rendererNow, end(rendererStart, "renderer"));
      rendererStart = 0;
      rendererCount++;
    }
  }
  public static void queueBegin() {
    if (capture)
      queueStart = begin(queueStart, "queue");
  }
  public static void queueEnd() {
    if (capture) {
      queueNow = add(queueNow, end(queueStart, "queue"));
      queueStart = 0;
      queueCount++;
    }
  }
  public static void flushBegin() {
    if (capture)
      flushStart = begin(flushStart, "flush");
  }
  public static void flushEnd() {
    if (capture) {
      flushNow = add(flushNow, end(flushStart, "flush"));
      flushStart = 0;
      flushCount++;
    }
  }
  private static long begin(long old, String label) {
    if (old != 0)
      throw new IllegalStateException("recursive M97 " + label);
    return System.nanoTime();
  }
  private static long end(long start, String label) {
    if (start == 0)
      throw new IllegalStateException("unstarted M97 " + label);
    long v = System.nanoTime() - start;
    if (v < 0)
      throw new IllegalStateException("negative M97 " + label);
    return v;
  }
  private static long add(long a, long b) {
    return Math.addExact(a, b);
  }
}
