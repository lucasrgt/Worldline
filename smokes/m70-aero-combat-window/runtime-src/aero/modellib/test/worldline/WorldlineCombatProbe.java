package aero.modellib.test.worldline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Test-only ordered network-event and render/log window probe. */
public final class WorldlineCombatProbe {
    private static final String ATTACKER = System.getProperty("worldline.combat.attacker");
    private static final String VICTIM = System.getProperty("worldline.combat.victim");
    private static int chunks, attackerId = -1, victimId = -1, warmup, post, baseline;
    private static boolean hello, play, ready, armed, swing, event;
    private WorldlineCombatProbe() {}
    public static synchronized void hello() { hello = true; marker("packet1"); }
    public static synchronized void play() { if (!hello) fail("play before login"); play = true; marker("packet13"); }
    public static synchronized void chunk() { if (chunks++ == 0) marker("packet51"); }
    public static synchronized void identity(String name, int id) { if (id < 0) fail("invalid peer id");
        if (ATTACKER.equals(name)) attackerId = bind(attackerId, id); else if (VICTIM.equals(name)) victimId = bind(victimId, id); }
    public static synchronized boolean networkReady() { return hello && play && chunks > 0 && attackerId >= 0 && victimId >= 0; }
    public static synchronized void ready() { if (ready) fail("duplicate ready"); ready = true;
        System.out.println("[WorldlineCombat] ready chunks=" + chunks + " attacker=" + attackerId + " victim=" + victimId); }
    public static synchronized boolean readyState() { return ready; }
    public static synchronized void animation(int id, int code) { if (id != attackerId) return;
        if (!armed || swing || code != 1) fail("attacker swing order drift"); swing = true;
        System.out.println("[WorldlineCombat] swing attacker=" + id + " animation=1"); }
    public static synchronized void status(int id, int code) { if (id != victimId) return;
        if (!armed || !swing || event || code != 2) fail("victim hurt order drift");
        event = true; baseline = lines(); System.out.println("[WorldlineCombat] event victim=" + id
                + " status=2 aeroBaseline=" + baseline); }
    public static synchronized void frame() { if (!ready) return; if (!armed) { warmup++;
            if (warmup >= Integer.getInteger("worldline.combat.warmup", 10)) { armed = true;
                System.out.println("[WorldlineCombat] armed warmup=" + warmup); } return; }
        if (event) post++; }
    public static synchronized boolean complete() { return event
            && post >= Integer.getInteger("worldline.combat.post", 20) && lines() > baseline; }
    public static synchronized int postFrames() { return post; }
    public static synchronized int aeroLines() { return lines() - baseline; }
    private static int bind(int prior, int id) { if (prior >= 0 && prior != id) fail("peer identity drift"); return id; }
    private static void marker(String name) { System.out.println("[WorldlineCombat] " + name); }
    private static void fail(String message) { throw new IllegalStateException(message); }
    private static int lines() { File file = new File(System.getProperty("aero.spikelog.file", "aero.log"));
        if (!file.isFile()) return 0; int count = 0; try (BufferedReader input = new BufferedReader(new FileReader(file))) {
            while (input.readLine() != null) count++; return count;
        } catch (Exception error) { throw new IllegalStateException("could not count Aero log", error); } }
}
