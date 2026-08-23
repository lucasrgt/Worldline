package worldline.m73.probe;

import java.util.HashSet;
import java.util.Set;

/** Bounded real-client state machine for paired activation and measurement. */
public final class WorldlinePairProbe {
  private static final Set<String> rendered = new HashSet<>();
  private static boolean hello, play, triggered;
  private static int warmFrames, windowFrames;
  private static long readyNs, triggerNs;
  private WorldlinePairProbe() {
  }
  public static void hello() {
    hello = true;
    System.out.println("[WorldlinePairContent] packet1");
  }
  public static void play() {
    if (!hello)
      throw new IllegalStateException("M73 play before login");
    play = true;
    System.out.println("[WorldlinePairContent] packet13");
  }
  public static boolean warm() {
    if (!hello || !play)
      return false;
    if (readyNs == 0)
      readyNs = System.nanoTime();
    warmFrames++;
    return warmFrames >= Integer.getInteger("worldline.pair.warmupFrames", 300)
        && System.nanoTime() - readyNs
        >= Long.getLong("worldline.pair.warmupMillis", 5000L) * 1_000_000L;
  }
  public static void trigger() {
    if (triggered)
      throw new IllegalStateException("duplicate M73 trigger");
    triggered = true;
    triggerNs = System.nanoTime();
    System.out.println("[WorldlinePairContent] trigger mode=" + mode() + " nonce=" + nonce()
        + " warmFrames=" + warmFrames + " warmMs=" + ((triggerNs - readyNs) / 1_000_000L)
        + " logger=" + System.getProperty("aero.spikelog.ms") + "/"
        + System.getProperty("aero.spikelog.heartbeatMs") + "/"
        + System.getProperty("aero.spikelog.sync"));
  }
  public static void rendered(int x, int y, int z, int value) {
    if (!triggered)
      throw new IllegalStateException("M73 render before trigger");
    String key = x + ":" + y + ":" + z + ":" + value;
    if (rendered.add(key))
      System.out.println("[WorldlinePairContent] rendered index=" + rendered.size() + " x=" + x
          + " y=" + y + " z=" + z + " nonce=" + value);
  }
  public static boolean window() {
    if (!triggered)
      return false;
    windowFrames++;
    return windowFrames >= Integer.getInteger("worldline.pair.windowFrames", 480)
        && System.nanoTime() - triggerNs
        >= Long.getLong("worldline.pair.windowMillis", 8000L) * 1_000_000L;
  }
  public static void verifyFixture(int received, int applied) {
    int expected = mode().equals("present") ? 16 : 0;
    if (received != expected || applied != expected || rendered.size() != expected)
      throw new IllegalStateException("M73 fixture mismatch received=" + received
          + " applied=" + applied + " rendered=" + rendered.size());
  }
  public static int frames() {
    return windowFrames;
  }
  public static long windowMillis() {
    return (System.nanoTime() - triggerNs) / 1_000_000L;
  }
  public static int rendered() {
    return rendered.size();
  }
  public static String mode() {
    String value = System.getProperty("worldline.pair.mode", "");
    if (!(value.equals("present") || value.equals("absent")))
      throw new IllegalStateException("invalid M73 client arm");
    return value;
  }
  public static int nonce() {
    int value = Integer.getInteger("worldline.pair.nonce", 0);
    if (value <= 0)
      throw new IllegalStateException("invalid M73 pair nonce");
    return value;
  }
}
