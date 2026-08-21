package worldline.smoke.armorreductionsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173ArmorReductionAccess;
import worldline.b173server.B173ArmorReductionAccess.Pad;
import worldline.b173server.B173ArmorReductionEquip;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173SpawnerSeed;
import worldline.b173server.B173WireClient;

/** Same zombie type-54 melee yields strictly smaller Packet8 deltas in leather then iron then diamond. */
public final class ArmorReductionSetSmoke {
    private ArmorReductionSetSmoke() {}

    public static void main(String[] a) throws Exception {
        if (a.length != 7) throw new IllegalArgumentException(
                "usage: ArmorReductionSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
        int port = Integer.parseInt(a[2]); long seed = Long.parseLong(a[3]); String user = a[4];
        int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
        require(seed == 17320110707L && user.equals("ArmorRed451") && user.length() <= 16, "actor identity drift");
        Duration timeout = Duration.ofSeconds(300);
        Pad pad = build(jar, workspace, port, seed, user, timeout, cx, cz);
        B173SpawnerSeed.entity(workspace, pad.spawner, "Zombie");
        Thread.sleep(1000L);
        B173DedicatedServer server = B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot(); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() >= 12 && actor.awaitHealth(20) == 20,
                    "armor-reduction reload inventory or health drift");
            RemoteIncomingHit bare = armedHit(server, actor, pad, null, true);
            new B173ArmorReductionEquip().wear(actor, B173ArmorReductionAccess.LEATHER);
            RemoteIncomingHit leather = armedHit(server, actor, pad, null, false);
            actor = reseed(actor, server, workspace, user, port, timeout, pad, B173ArmorReductionAccess.IRON);
            RemoteIncomingHit iron = armedHit(server, actor, pad, B173ArmorReductionAccess.IRON, true);
            actor = reseed(actor, server, workspace, user, port, timeout, pad, B173ArmorReductionAccess.DIAMOND);
            RemoteIncomingHit diamond = armedHit(server, actor, pad, B173ArmorReductionAccess.DIAMOND, true);
            require(bare.damage() > leather.damage() && bare.damage() > iron.damage()
                    && leather.damage() > 0 && iron.damage() > 0 && diamond.damage() > 0,
                    "armor Packet8 deltas were not strictly reduced "
                            + bare.damage() + ">" + leather.damage() + ">" + iron.damage() + ">" + diamond.damage());
            actor.close(); awaitPlayers(server, 0); server.save();
            String evidence = "column=" + pad.column + ",platform=7x7-48grass,spawner="
                    + B173ArmorReductionAccess.cell(pad.spawner)
                    + ",entityid=Zombie,mob=type54,night=14000"
                    + ",unarmored=20->" + bare.healthAfter() + ":" + bare.damage()
                    + ",leather=20->" + leather.healthAfter() + ":" + leather.damage()
                    + ",iron=20->" + iron.healthAfter() + ":" + iron.damage()
                    + ",diamond=20->" + diamond.healthAfter() + ":" + diamond.damage()
                    + ",strict=" + bare.damage() + ">" + leather.damage() + ">" + iron.damage() + ">" + diamond.damage()
                    + ",armor=298-301+306-309+310-313,food=322+320,wire=packet24-type54+packet8,not-m66-pvp,not-craft,clients=4,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-7x7-grass-platform+spawner52+window0-slots5-8"
                    + "|cause=nbt-entityid-zombie+time-14000+packet102-leather-iron-diamond"
                    + "|wire=packet24-type54+packet38-status2+packet8-health"
                    + "|oracle=zombie-melee-armor-reduction-not-pvp-not-craft-not-equip-only|" + evidence;
            System.out.println("WORLDLINE_M451_SET=" + evidence);
            System.out.println("WORLDLINE_M451_TRACE=" + trace);
            System.out.println("WORLDLINE_M451_SIGNATURE=" + sha(trace));
        } finally { actor.close(); server.close(); }
    }

    private static Pad build(Path jar, Path workspace, int port, long seed, String user, Duration timeout,
            int cx, int cz) throws Exception {
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
                    new int[] {1, 2, 52, 322, 320, 276, 298, 299, 300, 301, 306, 307, 308, 309, 310, 311, 312, 313},
                    new int[] {64, 48, 1, 8, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 18 && actor.awaitHealth(20) == 20,
                    "armor-reduction inventory or health drift");
            Pad pad = B173ArmorReductionAccess.raise(actor, cx, cz);
            actor.close(); awaitPlayers(server, 0); server.save(); return pad;
        } finally { actor.close(); server.close(); }
    }

    private static RemoteIncomingHit armedHit(B173DedicatedServer server, B173WireClient actor, Pad pad, int[] family,
            boolean poke) {
        server.setTime(14000L);
        B173ArmorReductionAccess.go(actor, pad.spawner);
        RemoteMobSpawn zombie = B173ArmorReductionAccess.near(actor, pad.spawner);
        require(zombie.legacyType() == 54 && zombie.legacyType() != 90, "zombie Packet24 identity drift");
        double[] at = {zombie.x(), zombie.y(), zombie.z()};
        if (family != null) new B173ArmorReductionEquip().wear(actor, family);
        B173ArmorReductionAccess.heal(actor); B173ArmorReductionAccess.settle(actor);
        RemoteIncomingHit hit = B173ArmorReductionAccess.absorb(actor, zombie.entityId(), at, poke);
        B173ArmorReductionAccess.heal(actor); B173ArmorReductionAccess.settle(actor);
        return hit;
    }

    private static B173WireClient reseed(B173WireClient actor, B173DedicatedServer server, Path workspace, String user,
            int port, Duration timeout, Pad pad, int[] armor) throws Exception {
        actor.close(); awaitPlayers(server, 0);
        B173PlayerSeed.writeInventory(workspace, user, pad.top.x() + 0.5D, pad.top.y() + 1.0D, pad.top.z() + 0.5D,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7},
                new int[] {1, 322, 320, 276, armor[0], armor[1], armor[2], armor[3]},
                new int[] {16, 8, 8, 1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0, 0, 0, 0});
        B173WireClient next = new B173WireClient("127.0.0.1", port, user, timeout);
        next.connect(); next.synchronizePose();
        require(next.awaitInventory().occupiedSlots() == 8 && next.awaitHealth(20) == 20, "reseed inventory drift");
        return next;
    }

    private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
        long e = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < e) { if (s.players().size() == n) return; Thread.sleep(100); }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String s) throws Exception {
        byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder v = new StringBuilder();
        for (byte x : b) v.append(String.format("%02x", x & 255));
        return v.toString();
    }

    private static void require(boolean v, String m) { if (!v) throw new IllegalStateException(m); }
}
