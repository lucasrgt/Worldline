package aero.modellib.test.worldline;

/** Test-only state machine for matched control/event observation windows. */
public final class WorldlinePairedProbe {
    private static final String ARM = System.getProperty("worldline.pair.arm");
    private static final String ATTACKER = System.getProperty("worldline.pair.attacker");
    private static final String VICTIM = System.getProperty("worldline.pair.victim");
    private static final String TRIGGER = System.getProperty("worldline.pair.trigger");
    private static int chunks, attackerId = -1, victimId = -1, warmupFrames, windowFrames;
    private static long readyNanos, triggerNanos;
    private static boolean hello, play, ready, armed, triggered, swing, hurt, complete;
    private WorldlinePairedProbe() {}
    public static synchronized void hello() { hello = true; marker("packet1"); }
    public static synchronized void play() { if (!hello) fail("play before login"); play = true; marker("packet13"); }
    public static synchronized void chunk() { if (chunks++ == 0) marker("packet51"); }
    public static synchronized void identity(String name, int id) { if (id < 0) fail("invalid peer id");
        if (ATTACKER.equals(name)) attackerId = bind(attackerId, id); else if (VICTIM.equals(name)) victimId = bind(victimId, id); }
    public static synchronized boolean networkReady() { return hello && play && chunks > 0 && attackerId >= 0 && victimId >= 0; }
    public static synchronized void ready() { if (ready) fail("duplicate ready"); ready = true; readyNanos = System.nanoTime();
        System.out.println("[WorldlinePair] ready chunks=" + chunks + " attacker=" + attackerId + " victim=" + victimId); }
    public static synchronized boolean readyState() { return ready; }
    public static synchronized void chat(String message) { if (!TRIGGER.equals(message)) return;
        if (!armed || triggered) fail("trigger order drift"); triggered = true; triggerNanos = System.nanoTime();
        System.out.println("[WorldlinePair] trigger arm=" + ARM); }
    public static synchronized void animation(int id, int code) { if (id != attackerId) return;
        if (!"event".equals(ARM) || !triggered || swing || code != 1 || windowFrames != 0) fail("swing drift");
        swing = true; System.out.println("[WorldlinePair] swing attacker=" + id + " animation=1"); }
    public static synchronized void status(int id, int code) { if (id != victimId) return;
        if (!"event".equals(ARM) || !swing || hurt || code != 2 || windowFrames != 0) fail("hurt drift");
        hurt = true; System.out.println("[WorldlinePair] hurt victim=" + id + " status=2"); }
    public static synchronized void frame() { long now = System.nanoTime(); if (!ready) return;
        if (!armed) { warmupFrames++; if (warmupFrames >= integer("worldline.pair.warmupFrames")
                && elapsed(readyNanos, now) >= integer("worldline.pair.warmupSeconds")) { armed = true;
                System.out.println("[WorldlinePair] armed warmupFrames=" + warmupFrames); } return; }
        if (!triggered) return; windowFrames++; boolean eventReady = "control".equals(ARM) || hurt;
        if (!complete && eventReady && windowFrames >= integer("worldline.pair.windowFrames")
                && elapsed(triggerNanos, now) >= integer("worldline.pair.windowSeconds")) complete = true; }
    public static synchronized boolean complete() { return complete; }
    public static synchronized int windowFrames() { return windowFrames; }
    public static synchronized String arm() { return ARM; }
    private static long elapsed(long start, long now) { return (now - start) / 1_000_000_000L; }
    private static int integer(String name) { return Integer.getInteger(name, -1); }
    private static int bind(int prior, int id) { if (prior >= 0 && prior != id) fail("identity drift"); return id; }
    private static void marker(String value) { System.out.println("[WorldlinePair] " + value); }
    private static void fail(String value) { throw new IllegalStateException(value); }
}
