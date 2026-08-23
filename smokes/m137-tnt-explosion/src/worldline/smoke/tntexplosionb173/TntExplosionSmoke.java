package worldline.smoke.tntexplosionb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** Builds, ignites and persists one isolated official TNT explosion. */
public final class TntExplosionSmoke {
    private TntExplosionSmoke() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 7) throw new IllegalArgumentException("usage: TntExplosionSmoke server.jar workspace port seed username fixtureTicks fuseTicks");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]); int port = Integer.parseInt(args[2]); long seed = Long.parseLong(args[3]);
        String username = args[4]; int fixtureTicks = Integer.parseInt(args[5]), fuseTicks = Integer.parseInt(args[6]); Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true); ExplosionSession actor = new B173WireClient("127.0.0.1", port, username, timeout);
        B173WireClient reader = null; BlockPosition top, tnt; RemoteExplosion explosion; int column = 0;
        try {
            server.boot(); B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2}, new int[] {1, 46, 259}, new int[] {64, 1, 1}, new int[] {0, 0, 0});
            actor.connect(); actor.synchronizePose(); require(actor.awaitInventory().occupiedSlots() == 3, "TNT inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(0, 0).chunkAt(0, 0); top = foundation(initial); actor.selectHeldSlot(0);
            while (water(initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) { top = place(actor, top, BlockFace.UP, 1); actor.moveAndObserve(0, 1, 0, 1); column++; require(column <= 15, "water column exceeded stack"); }
            for (int index = 0; index < 6; index++) { top = place(actor, top, BlockFace.UP, 1); actor.moveAndObserve(0, 1, 0, 1); column++; }
            actor.selectHeldSlot(1); tnt = place(actor, top, BlockFace.UP, 46); RemoteWorldView before = worldline.test.WorldlineSmokeAwait.observe(actor,fixtureTicks);
            require(before.blockAt(top.x(), top.y(), top.z()).equals(new BlockState(1, 0)) && before.blockAt(tnt.x(), tnt.y(), tnt.z()).equals(new BlockState(46, 0)), "TNT baseline drift");
            actor.selectHeldSlot(2); actor.useHeldItemOnBlock(tnt, BlockFace.UP); actor.moveAndObserve(10D, 0D, 0D, 4); worldline.test.WorldlineSmokeAwait.observe(actor,fuseTicks); explosion = actor.awaitExplosion();
            require(explosion.strength() == 4F && Math.abs(explosion.x() - (tnt.x() + 0.5D)) < 2D && Math.abs(explosion.y() - (tnt.y() + 0.5D)) < 4D && Math.abs(explosion.z() - (tnt.z() + 0.5D)) < 2D, "Packet60 center/strength drift: " + explosion.x() + ":" + explosion.y() + ":" + explosion.z() + ":" + explosion.strength());
            RemoteWorldView after = worldline.test.WorldlineSmokeAwait.observe(actor,1); require(explosion.destroyed().contains(top) && after.blockAt(top.x(), top.y(), top.z()).equals(new BlockState(0, 0)) && after.blockAt(tnt.x(), tnt.y(), tnt.z()).equals(new BlockState(0, 0)), "TNT destruction drift");
            actor.close(); awaitPlayers(server, 0); server.save(); reader = new B173WireClient("127.0.0.1", port, username, timeout); reader.connect(); reader.synchronizePose();
            RemoteWorldView persisted = reader.awaitRemoteChunk(0, 0); require(persisted.blockAt(top.x(), top.y(), top.z()).equals(new BlockState(0, 0)) && persisted.blockAt(tnt.x(), tnt.y(), tnt.z()).equals(new BlockState(0, 0)), "fresh explosion persistence drift");
        } finally { actor.close(); if (reader != null) reader.close(); server.close(); }
        String evidence = "column=" + column + ",origin=" + tnt.x() + ":" + tnt.y() + ":" + tnt.z() + ",strength=4,destroyed=positive+support,support=1:0->0:0,tnt=46:0->0:0,persisted=air";
        String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=isolated-stone-column+tnt46+flint259|cause=packet15-ignite|fuse=" + fuseTicks + "ticks|wire=protocol14-packet60-center+strength+relative-destroyed-cells-no-motion-fields|cache=packet60-destroyed-cells-to-air|" + evidence + "|disconnect=clean";
        System.out.println("WORLDLINE_M137_EXPLOSION=" + evidence); System.out.println("WORLDLINE_M137_TRACE=" + trace); System.out.println("WORLDLINE_M137_SIGNATURE=" + sha(trace));
    }
    private static BlockPosition place(ExplosionSession actor, BlockPosition support, BlockFace face, int id) { BlockPosition target = face.adjacent(support); actor.placeHeldBlock(support, face); actor.awaitBlock(target, new BlockState(id, 0)); return target; }
    private static BlockPosition foundation(RemoteChunkSnapshot q) { for (int x = 4; x <= 10; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--) if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId())) return new BlockPosition(x, y, z); throw new IllegalStateException("no deterministic TNT foundation"); }
    private static boolean water(int id) { return id == 8 || id == 9; }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception { long end = System.currentTimeMillis() + 5000; while (System.currentTimeMillis() < end) { if (server.players().size() == count) return; Thread.sleep(100); } throw new IllegalStateException("player count drift"); }
    private static String sha(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder(); for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
}
