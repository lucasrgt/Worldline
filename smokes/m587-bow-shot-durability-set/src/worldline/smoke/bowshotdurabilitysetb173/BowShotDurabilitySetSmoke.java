package worldline.smoke.bowshotdurabilitysetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Air-uses seeded bow 261 so Packet23 type 60 fires and Packet103 remaining bow damage is frozen. */
public final class BowShotDurabilitySetSmoke {
    private BowShotDurabilitySetSmoke() {}

    public static void main(String[] a) throws Exception {
        if (a.length != 7)
            throw new IllegalArgumentException(
                    "usage: BowShotDurabilitySetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
        int port = Integer.parseInt(a[2]);
        long seed = Long.parseLong(a[3]);
        String user = a[4];
        int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
        Duration timeout = Duration.ofSeconds(90);
        require(seed == 17320110707L && user.equals("BowDura587") && user.length() <= 16,
                "bow-shot-durability-set identity drift");
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
        BlockPosition top;
        int column;
        RemoteItemStack bow;
        RemoteObjectSpawn arrow;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2},
                    new int[] {1, 261, 262},
                    new int[] {32, 1, 1},
                    new int[] {0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 3
                    && id(actor.inventory(), 37) == 261 && damage(actor.inventory(), 37) == 0
                    && id(actor.inventory(), 38) == 262 && count(actor.inventory(), 38) == 1,
                    "bow-shot-durability-set inventory seed drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            top = foundation(initial, cx, cz);
            column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "water column exceeded bow-shot-durability-set fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                column++;
            }
            worldline.test.WorldlineSmokeAwait.observe(actor, 5);
            actor.selectHeldSlot(1);
            actor.look(0F, 0F);
            actor.useSelectedItemInAir();
            arrow = actor.awaitObjectSpawn(60);
            require(arrow.type() == 60 && (arrow.throwerId() == actor.state().entityId()
                    || arrow.throwerId() == 0), "arrow object spawn drift");
            worldline.test.WorldlineSmokeAwait.observe(actor, 5);
            bow = remaining(actor);
            require(bow.legacyId() == 261 && bow.count() == 1, "held-stack bow remaining damage drift");
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteInventoryView after = reader.awaitInventory();
            require(held(after, 261).equals(bow) && find(after, 262) == null,
                    "persisted held-stack bow durability drift");
            String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + ",bow=" + bow.legacyId()
                    + ":" + bow.damage() + ",arrow=262:1->0,wire=packet23-type60,thrower=actor,persisted=true"
                    + ",clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone-platform+bow261+arrow262"
                    + "|cause=packet15-air-bow261|wire=packet23-type60+packet103-" + bow.legacyId()
                    + ":" + bow.damage()
                    + "|oracle=held-bow-durability-not-m157-peer-or-m332-craft-or-m462-hit|" + evidence;
            System.out.println("WORLDLINE_M587_SET=" + evidence);
            System.out.println("WORLDLINE_M587_TRACE=" + trace);
            System.out.println("WORLDLINE_M587_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) reader.close();
            server.close();
        }
    }

    private static RemoteItemStack remaining(B173WireClient a) {
        return worldline.test.WorldlineSmokeAwait.awaitEntity(a, () -> find(a.inventory(), 261),
                item -> item != null && item.legacyId() == 261 && item.count() == 1,
                "held 261 remaining after shot", 40);
    }

    private static RemoteItemStack held(RemoteInventoryView view, int id) {
        RemoteItemStack item = find(view, id);
        if (item == null) throw new IllegalStateException("persisted " + id + " absent");
        return item;
    }

    private static RemoteItemStack find(RemoteInventoryView view, int id) {
        for (int slot = 0; slot < view.size(); slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
                return view.slot(slot).item();
        return null;
    }

    private static int id(RemoteInventoryView view, int slot) {
        return view.slot(slot).empty() ? -1 : view.slot(slot).item().legacyId();
    }

    private static int damage(RemoteInventoryView view, int slot) {
        return view.slot(slot).empty() ? -1 : view.slot(slot).item().damage();
    }

    private static int count(RemoteInventoryView view, int slot) {
        return view.slot(slot).empty() ? -1 : view.slot(slot).item().count();
    }

    private static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        a.placeHeldBlock(support, face);
        a.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
        for (int x = 4; x <= 11; x++)
            for (int z = 4; z <= 11; z++)
                for (int y = 126; y >= 1; y--)
                    if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic bow-shot-durability-set foundation");
    }

    private static String cell(BlockPosition p, int id, int meta) {
        return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
    }

    private static boolean water(int id) { return id == 8 || id == 9; }
    private static int local(int v, int c) { return v - c * 16; }

    private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
        long e = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < e) {
            if (s.players().size() == n) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String s) throws Exception {
        byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder v = new StringBuilder();
        for (byte x : b) v.append(String.format("%02x", x & 255));
        return v.toString();
    }

    private static void require(boolean v, String m) {
        if (!v) throw new IllegalStateException(m);
    }
}
