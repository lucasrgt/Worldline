package worldline.m74;

/** Fixed primitive recorder; its hot path performs no retained allocation or I/O. */
public final class WorldlineFrameCensus {
  static final int CAP = 65536, MIN = Integer.getInteger("worldline.census.minimumFrames", 720);
  static final long MIN_NS = Long.getLong("worldline.census.minimumMillis", 12000L) * 1_000_000L;
  static final long[] delta = new long[CAP];
  static final int[] renders = new int[CAP], lists = new int[CAP], visible = new int[CAP],
                     calls = new int[CAP];
  static final short[] state = new short[CAP], masks = new short[CAP];
  static int count, pendingCalls;
  static long start, last, end;
  static boolean armed, running, pending, sealed, written;
  static {
    for (int i = 0; i < CAP; i += 256) {
      delta[i] = 0;
      renders[i] = lists[i] = visible[i] = calls[i] = 0;
      state[i] = masks[i] = 0;
    }
  }
  private WorldlineFrameCensus() {
  }
  public static void arm() {
    if (armed || running || sealed)
      throw new IllegalStateException("duplicate M74 arm");
    armed = true;
  }
  public static void head() {
    if (!armed || sealed)
      return;
    long now = System.nanoTime();
    if (!running) {
      running = true;
      start = last = now;
      return;
    }
    if (!pending)
      throw new IllegalStateException("M74 HEAD without TAIL");
    if (count == CAP)
      throw new IllegalStateException("M74 census overflow");
    long value = now - last;
    if (value <= 0)
      throw new IllegalStateException("nonpositive M74 interval");
    delta[count] = value;
    calls[count] = pendingCalls;
    count++;
    pendingCalls = 0;
    pending = false;
    last = now;
    if (count >= MIN && now - start >= MIN_NS) {
      end = now;
      sealed = true;
    }
  }
  public static void tail(int atRest, int listCalls, int chunks, int packed, int mask) {
    if (!running || sealed)
      return;
    if (pending)
      throw new IllegalStateException("duplicate M74 TAIL");
    if (atRest < 0 || listCalls < 0 || chunks < 0 || packed < 0 || mask < 0 || mask > 0xffff)
      throw new IllegalStateException("negative M74 counter");
    renders[count] = atRest;
    lists[count] = listCalls;
    visible[count] = chunks;
    state[count] = (short) packed;
    masks[count] = (short) mask;
    pending = true;
  }
  public static void contentCall(int index) {
    if (index < 0 || index > 15)
      throw new IllegalStateException("invalid M74 render identity");
    if (running && !sealed)
      pendingCalls++;
  }
  public static boolean sealed() {
    return sealed;
  }
  public static int count() {
    return count;
  }
  public static long elapsed() {
    return end - start;
  }
}
