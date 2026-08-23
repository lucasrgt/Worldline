package worldline.smoke.fallwatercancelsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173Fall;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Walks off a damaging-height column into still water so Packet8 fall damage stays absent. */
public final class FallWaterCancelSetSmoke {
    private FallWaterCancelSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) {
            throw new IllegalArgumentException(
                    "usage: FallWaterCancelSetSmoke server.jar workspace port seed username chunkX chunkZ");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        require(seed == 17320110707L && user.equals("FallWater586") && user.length() <= 16,
                "fall-water-cancel identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1}, new int[] {1, 9}, new int[] {64, 8}, new int[] {0, 0});
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 2 && actor.awaitHealth(20) == 20,
                    "fall-water-cancel inventory or health drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            BlockPosition top = foundation(initial, chunkX, chunkZ);
            int column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ))
                    .legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "water column exceeded fall-water-cancel fixture");
            }
            top = place(actor, top, BlockFace.UP, 1);
            pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
            column++;
            BlockPosition pad = place(actor, top, BlockFace.EAST, 1);
            BlockFace[] walls = {
                    BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
            };
            for (BlockFace wall : walls) {
                place(actor, place(actor, pad, wall, 1), BlockFace.UP, 1);
            }
            actor.selectHeldSlot(1);
            BlockPosition pool = place(actor, pad, BlockFace.UP, 9);
            require(water(WorldlineSmokeAwait.observe(actor, 5)
                    .blockAt(pool.x(), pool.y(), pool.z()).legacyId()), "still-water pool drift");
            actor.selectHeldSlot(0);
            double waterY = pool.y() + 0.2D;
            int lift = 0;
            while (lift < 8) {
                top = place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                column++;
                lift++;
            }
            pose = walkOff(actor, pose, top, pad);
            int drop = (int) Math.round(pose.y() - waterY);
            require(drop >= 6 && over(pose, pad) && actor.health() == 20,
                    "water-fall height drift drop=" + drop);
            pose = land(actor, pose, waterY);
            require(water(WorldlineSmokeAwait.observe(actor, 1)
                    .blockAt(pool.x(), pool.y(), pool.z()).legacyId())
                    && over(pose, pad) && actor.health() == 20,
                    "water landing Packet8 drift health=" + actor.health());
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            require(server.player(user).health() == 20, "persisted fall-water-cancel health drift");
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            require(reader.awaitHealth(20) == 20, "fresh-login fall-water-cancel health drift");
            String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":"
                    + top.z() + ":1:0,pool=" + pool.x() + ":" + pool.y() + ":" + pool.z()
                    + ":9:0,drop=" + drop + ",water=20->20,packet8=absent,status=0,persisted=true,"
                    + "clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+east-still-water9+drop" + drop
                    + "|cause=packet13-ungrounded-walk-off+water-landing"
                    + "|wire=packet8-absent-health20"
                    + "|oracle=fall-water-cancel-no-packet8+fresh-login|" + evidence;
            System.out.println("WORLDLINE_M586_SET=" + evidence);
            System.out.println("WORLDLINE_M586_TRACE=" + trace);
            System.out.println("WORLDLINE_M586_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
    }

    private static PlayerPose walkOff(B173WireClient actor, PlayerPose pose, BlockPosition top,
            BlockPosition pad) throws Exception {
        pose = actor.moveAndObserve(top.x() + 0.5D - pose.x(), (top.y() + 1.0D) - pose.y(),
                top.z() + 0.5D - pose.z(), 20).resulting();
        return actor.moveAndObserve(pad.x() + 0.5D - pose.x(), 0D, pad.z() + 0.5D - pose.z(), 1)
                .resulting();
    }

    private static PlayerPose land(B173WireClient actor, PlayerPose pose, double y)
            throws Exception {
        while (pose.y() > y + 0.01D) {
            pose = B173Fall.air(actor, 0D, Math.max(y - pose.y(), -0.5D), 0D);
            WorldlineSmokeAwait.observe(actor, 1);
        }
        WorldlineSmokeAwait.observe(actor, 5);
        actor.moveBy(0D, 0D, 0D);
        WorldlineSmokeAwait.observe(actor, 20);
        return pose;
    }

    private static boolean over(PlayerPose pose, BlockPosition block) {
        return Math.abs(pose.x() - (block.x() + 0.5D)) < 0.2D
                && Math.abs(pose.z() - (block.z() + 0.5D)) < 0.2D;
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face,
            int id) throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (chunk.blockAt(x, y, z).legacyId() == 3
                            && water(chunk.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic fall-water-cancel foundation");
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int value, int chunk) {
        return value - chunk * 16;
    }

    private static void awaitPlayers(B173DedicatedServer server, int count) {
        long end = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) {
                return;
            }
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte next : digest) {
            hex.append(String.format("%02x", next & 255));
        }
        return hex.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
