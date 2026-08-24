package worldline.smoke.wolftameb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173ShearsAccess;
import worldline.b173server.B173WireClient;
import worldline.b173server.B173WolfTameAccess;
import worldline.test.WorldlineSmokeAwait;

/** Observes bounded Packet7 taming plus Packet40/NBT owner-collar metadata. */
public final class WolfTameSmoke {
    private WolfTameSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 7) {
            throw new IllegalArgumentException(
                    "usage: WolfTameSmoke server.jar workspace port seed username chunkX chunkZ");
        }
        Path jar = Paths.get(args[0]);
        Path workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int chunkX = Integer.parseInt(args[5]);
        int chunkZ = Integer.parseInt(args[6]);
        require(seed == 17320110707L && user.equals("WolfTame618") && user.length() <= 16,
                "wolf-tame identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = B173DedicatedServer.animals(
                jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        BlockPosition top;
        BlockPosition spawner;
        int column;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4}, new int[] {1, 2, 52, 352, 4},
                    new int[] {32, 48, 1, B173WolfTameAccess.MAX_BONES, 1},
                    new int[] {0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 5, "wolf-tame inventory drift");
            WorldlineSmokeAwait.observe(actor, 5);
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ)
                    .chunkAt(chunkX, chunkZ);
            top = foundation(initial, chunkX, chunkZ);
            column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1,
                    local(top.z(), chunkZ)).legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "water column exceeded wolf-tame fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                column++;
            }
            platform(actor, top);
            actor.selectHeldSlot(2);
            spawner = place(actor, top, BlockFace.UP, 52);
            WorldlineSmokeAwait.observe(actor, 5);
            actor.close();
            awaitPlayers(server, 0);
            server.save();
        } finally {
            actor.close();
            server.close();
        }
        Thread.sleep(1000L);
        B173WolfTameAccess.retarget(workspace, spawner);
        server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
        actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() >= 3,
                    "wolf-tame reload inventory drift");
            require(!actor.inventory().slot(39).empty()
                    && actor.inventory().slot(39).item().legacyId() == 352
                    && actor.inventory().slot(39).item().count() == B173WolfTameAccess.MAX_BONES,
                    "wolf-tame reload bone 352 drift");
            WorldlineSmokeAwait.observe(actor, 5);
            RemoteMobSpawn wolf = actor.awaitMobSpawn(95);
            require(wolf.legacyType() == 95 && wolf.entityId() != actor.state().entityId(),
                    "wolf Packet24 type 95 identity drift");
            int attempts = B173WolfTameAccess.tameBounded(
                    actor, wolf, B173WolfTameAccess.MAX_BONES);
            require(attempts >= 1 && attempts <= B173WolfTameAccess.MAX_BONES,
                    "wolf bounded tame attempts drift");
            require(B173ShearsAccess.peekDeath(actor, wolf.entityId()) == null,
                    "Packet38 status 3 death after one-bone tame");
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            String owner = B173WolfTameAccess.owner(workspace, chunkX, chunkZ, user);
            require(owner.equals(user), "wolf Owner NBT drift");
            String evidence = "column=" + column + ",platform=7x7-48grass,spawner="
                    + spawner.x() + ":" + spawner.y() + ":" + spawner.z()
                    + ":52:0,mob=type95,bone=352,bones=bounded<=64,tame=packet38-status7,collar=red,owner="
                    + owner + ",tamed=packet40-bit2,death=no-packet38-status3,clients=1,"
                    + "disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-7x7-grass-platform+wolf-spawner52"
                    + "|cause=bounded-packet7-button0-bone352-until-tamed"
                    + "|wire=packet24-type95+packet38-status7+packet40-tamed-owner"
                    + "+no-packet38-status3"
                    + "|oracle=wolf-type95-bounded-tame-owner-collar-not-m420-dye"
                    + "-not-m449-anger-not-m468-assist-not-m583-sit|" + evidence;
            System.out.println("WORLDLINE_M618_SET=" + evidence);
            System.out.println("WORLDLINE_M618_TRACE=" + trace);
            System.out.println("WORLDLINE_M618_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }

    private static void platform(B173WireClient actor, BlockPosition top) throws Exception {
        actor.selectHeldSlot(1);
        for (int radius = 1; radius <= 3; radius++) {
            for (int z = -radius + 1; z < radius; z++) {
                grass(actor, new BlockPosition(top.x() - radius + 1, top.y(), top.z() + z),
                        BlockFace.WEST);
                grass(actor, new BlockPosition(top.x() + radius - 1, top.y(), top.z() + z),
                        BlockFace.EAST);
            }
            for (int x = -radius + 1; x < radius; x++) {
                grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - radius + 1),
                        BlockFace.NORTH);
                grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + radius - 1),
                        BlockFace.SOUTH);
            }
            grass(actor, new BlockPosition(top.x() - radius, top.y(), top.z() - radius + 1),
                    BlockFace.NORTH);
            grass(actor, new BlockPosition(top.x() - radius, top.y(), top.z() + radius - 1),
                    BlockFace.SOUTH);
            grass(actor, new BlockPosition(top.x() + radius, top.y(), top.z() - radius + 1),
                    BlockFace.NORTH);
            grass(actor, new BlockPosition(top.x() + radius, top.y(), top.z() + radius - 1),
                    BlockFace.SOUTH);
        }
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support,
            BlockFace face, int id) throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static void grass(B173WireClient actor, BlockPosition support, BlockFace face)
            throws Exception {
        place(actor, support, face, 2);
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        for (int x = 4; x <= 11; x++) {
            for (int z = 4; z <= 11; z++) {
                for (int y = 126; y >= 1; y--) {
                    if (chunk.blockAt(x, y, z).legacyId() == 3
                            && water(chunk.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                }
            }
        }
        throw new IllegalStateException("no deterministic wolf-tame foundation");
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int value, int chunk) {
        return value - chunk * 16;
    }

    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte part : digest) text.append(String.format("%02x", part & 255));
        return text.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
