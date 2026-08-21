package worldline.m74;

/** Login/warmup state and primitive renderer-identity mask. */
public final class WorldlineCensusProbe {
    private static boolean hello, play, triggered; private static int warmFrames, mask; private static long readyNs;
    private WorldlineCensusProbe() {}
    public static void hello() { hello = true; System.out.println("[WorldlineCensus] packet1"); }
    public static void play() { if (!hello) throw new IllegalStateException("M74 play before login"); play = true; System.out.println("[WorldlineCensus] packet13"); }
    public static boolean warm() { if (!hello || !play) return false; if (readyNs == 0) readyNs = System.nanoTime(); warmFrames++;
        return warmFrames >= Integer.getInteger("worldline.census.warmupFrames", 300)
                && System.nanoTime() - readyNs >= Long.getLong("worldline.census.warmupMillis", 5000L) * 1_000_000L; }
    public static void trigger() { if (triggered) throw new IllegalStateException("duplicate M74 trigger"); triggered = true;
        System.out.println("[WorldlineCensus] trigger mode=" + mode() + " nonce=" + nonce() + " warmFrames=" + warmFrames
                + " logger=" + System.getProperty("aero.spikelog")); }
    public static void rendered(int x, int y, int z, int nonce) { int index = WorldlineCensusSync.index(x, y, z, nonce);
        if (index < 0) throw new IllegalStateException("invalid M74 rendered identity"); mask |= 1 << index; WorldlineFrameCensus.contentCall(index); }
    public static int mask() { return mask; }
    public static String mode() { String value = System.getProperty("worldline.census.mode", "");
        if (!(value.equals("present") || value.equals("absent"))) throw new IllegalStateException("invalid M74 mode"); return value; }
    public static int nonce() { int value = Integer.getInteger("worldline.census.nonce", 0); if (value <= 0) throw new IllegalStateException("invalid M74 nonce"); return value; }
}
