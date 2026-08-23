package worldline.smoke.lavadamageb173;

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
import worldline.api.RemoteIncomingHit;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineAwait;
import worldline.test.WorldlineSmokeAwait;

/** Places still lava 11:0 and proves standing in it repeats Packet8 damage while alive. */
public final class LavaDamageSmoke {
    private LavaDamageSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 7) {
            throw new IllegalArgumentException(
                    "usage: LavaDamageSmoke server.jar workspace port seed username chunkX chunkZ");
        }
        Path jar = Paths.get(args[0]);
        Path workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int cx = Integer.parseInt(args[5]);
        int cz = Integer.parseInt(args[6]);
        require(seed == 17320110707L && user.equals("LavaDmg605") && user.length() <= 16,
                "lava-damage identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1}, new int[] {1, 327}, new int[] {48, 1}, new int[] {0, 0});
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 2 && actor.awaitHealth(20) == 20,
                    "lava-damage inventory or health drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            BlockPosition top = foundation(initial, cx, cz);
            int column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz))
                    .legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "water column exceeded lava-damage fixture");
            }
            int lift = 0;
            while (lift < 8) {
                top = place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                column++;
                lift++;
            }
            BlockFace[] walls = new BlockFace[] {
                    BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            int wall = 0;
            while (wall < walls.length) {
                place(actor, place(actor, top, walls[wall], 1), BlockFace.UP, 1);
                wall++;
            }
            while (pose.y() > top.y() + 1.01D) {
                pose = actor.moveAndObserve(0D, -1D, 0D, 1).resulting();
            }
            pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
            pose = actor.moveAndObserve(0D, 0D, 1D, 1).resulting();
            BlockPosition lava = BlockFace.UP.adjacent(top);
            require(WorldlineSmokeAwait.observe(actor, 5).blockAt(lava.x(), lava.y(), lava.z())
                    .equals(new BlockState(0, 0)) && actor.health() == 20, "empty basin drift");
            actor.selectHeldSlot(1);
            actor.look(180F, 70F);
            actor.useHeldItemOnBlock(top, BlockFace.UP);
            actor.useSelectedItemInAir();
            actor.awaitBlock(lava, new BlockState(11, 0));
            require(WorldlineSmokeAwait.observe(actor, 5).blockAt(lava.x(), lava.y(), lava.z())
                    .equals(new BlockState(11, 0)) && actor.health() == 20,
                    "pre-lava still-lava drift");
            pose = actor.moveAndObserve(lava.x() + 0.5D - pose.x(), lava.y() - pose.y(),
                    lava.z() + 0.5D - pose.z(), 2).resulting();
            int first = dropTo(actor, 20, 16, "lava-first");
            int second = dropTo(actor, first, 13, "lava-repeat");
            actor.moveAndObserve(0D, 2D, 1D, 3);
            require(actor.health() == second && second == 13 && second > 0,
                    "post-leave lava health drift: " + actor.health());
            require(WorldlineSmokeAwait.observe(actor, 1).blockAt(lava.x(), lava.y(), lava.z())
                    .equals(new BlockState(11, 0)), "still lava 11:0 drifted after leave");
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            require(server.player(user).health() == 13, "persisted lava-damage health drift");
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            require(reader.awaitHealth(13) == 13, "fresh-login lava-damage health drift");
            String evidence = "column=" + column + ",floor=" + cell(top, 1)
                    + ",lava=" + cell(lava, 11)
                    + ",health=20->16->13,damage=4+3,hits=2,status=2,alive=true,"
                    + "persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone-basin+still-lava11"
                    + "|cause=packet15-lava-bucket327+stand-in-lava"
                    + "|wire=packet53-lava11+packet38-status2+packet8-health20->16->13"
                    + "|oracle=repeated-lava-damage-alive-not-m138-flow-not-m465-death-typically-fire|"
                    + evidence;
            System.out.println("WORLDLINE_M605_DAMAGE=" + evidence);
            System.out.println("WORLDLINE_M605_TRACE=" + trace);
            System.out.println("WORLDLINE_M605_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) reader.close();
            server.close();
        }
    }

    private static int dropTo(B173WireClient actor, int before, int expect, String name)
            throws Exception {
        int after = WorldlineSmokeAwait.awaitEntity(actor, actor::health, value -> value < before,
                name + " health", 200);
        require(after == expect, name + " Packet8 health drift: " + before + "->" + after);
        RemoteIncomingHit hit = actor.awaitIncomingHit(after);
        require(hit.healthBefore() == before && hit.healthAfter() == after
                && hit.damage() == before - after, name + " Packet38/8 drift");
        return after;
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face,
            int id) throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot snapshot, int cx, int cz) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (snapshot.blockAt(x, y, z).legacyId() == 3
                            && water(snapshot.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic lava-damage foundation");
    }

    private static String cell(BlockPosition position, int id) {
        return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":0";
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int value, int chunk) {
        return value - chunk * 16;
    }

    private static void awaitPlayers(B173DedicatedServer server, int n) {
        new WorldlineAwait(50).awaitEntity(server::players,
                names -> names.size() == n, "player count");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        int index = 0;
        while (index < digest.length) {
            hex.append(String.format("%02x", digest[index] & 255));
            index++;
        }
        return hex.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
