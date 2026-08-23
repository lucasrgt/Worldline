package worldline.smoke.drowningsetb173;

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
import worldline.test.WorldlineSmokeAwait;

/** Submerges the actor in two-deep still water until Packet8 records drowning damage. */
public final class DrowningSetSmoke {
    private DrowningSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) {
            throw new IllegalArgumentException(
                    "usage: DrowningSetSmoke server.jar workspace port seed username chunkX chunkZ");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        require(seed == 17320110707L && user.equals("Drown604") && user.length() <= 16,
                "drowning-set identity drift");
        Duration timeout = Duration.ofSeconds(240);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            run(server, actor, workspace, user, chunkX, chunkZ, seed);
        } finally {
            actor.close();
            server.close();
        }
    }

    private static void run(B173DedicatedServer server, B173WireClient actor, Path workspace,
            String user, int chunkX, int chunkZ, long seed) throws Exception {
        server.boot();
        B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                new int[] {0, 1}, new int[] {1, 9}, new int[] {64, 8}, new int[] {0, 0}, 20);
        actor.connect();
        PlayerPose pose = actor.synchronizePose();
        require(actor.awaitInventory().occupiedSlots() == 2 && actor.awaitHealth(20) == 20,
                "drowning inventory or health drift");
        RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
        BlockPosition top = foundation(initial, chunkX, chunkZ);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ)).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
            column++;
            require(column <= 15, "water column exceeded drowning fixture");
        }
        int lift = 0;
        while (lift < 8) {
            top = place(actor, top, BlockFace.UP, 1);
            pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
            column++;
            lift++;
        }
        BlockPosition north = place(actor, top, BlockFace.NORTH, 1);
        north = place(actor, north, BlockFace.UP, 1);
        north = place(actor, north, BlockFace.UP, 1);
        while (pose.y() > top.y() + 1.01D) {
            pose = actor.moveAndObserve(0D, -1D, 0D, 1).resulting();
        }
        actor.selectHeldSlot(1);
        BlockPosition lower = place(actor, top, BlockFace.UP, 9);
        BlockPosition upper = place(actor, north, BlockFace.SOUTH, 9);
        pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        int eye = WorldlineSmokeAwait.observe(actor, 5)
                .blockAt((int) Math.floor(pose.x()), (int) Math.floor(pose.y() + 1.62D),
                        (int) Math.floor(pose.z())).legacyId();
        require(water(WorldlineSmokeAwait.observe(actor, 1)
                .blockAt(lower.x(), lower.y(), lower.z()).legacyId())
                && water(WorldlineSmokeAwait.observe(actor, 1)
                        .blockAt(upper.x(), upper.y(), upper.z()).legacyId())
                && water(eye) && actor.health() == 20, "pre-drown fixture drift");
        int after = WorldlineSmokeAwait.awaitEntity(actor, actor::health, h -> h.intValue() < 20,
                "drowning health", 800);
        require(after == 18, "drowning Packet8 health drift: 20->" + after);
        RemoteIncomingHit hit = actor.awaitIncomingHit(after);
        require(hit.healthBefore() == 20 && hit.healthAfter() == 18 && hit.damage() == 2,
                "drowning Packet38/8 drift");
        pose = actor.moveAndObserve(0D, 3D, 0D, 4).resulting();
        require(actor.health() == 18, "drowning death is m465 not hurt: " + actor.health());
        require(water(WorldlineSmokeAwait.observe(actor, 1)
                .blockAt(upper.x(), upper.y(), upper.z()).legacyId()),
                "drowning left water 8/9 absent");
        actor.close();
        server.save();
        String evidence = "cause=drown,column=" + column + ",water=" + lower.x() + ":"
                + lower.y() + ":" + lower.z() + "+" + upper.x() + ":" + upper.y() + ":"
                + upper.z() + ",health=20->18,damage=2,status=2,packet8=18,clients=1,"
                + "disconnect=clean";
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|fixture=raised-stone+two-still-water|cause=submerged-eye-air-deplete"
                + "|wire=packet38-status2+packet8-health20->18"
                + "|oracle=drowning-hurt-not-m465-death-not-m307-compound|" + evidence;
        System.out.println("WORLDLINE_M604_SET=" + evidence);
        System.out.println("WORLDLINE_M604_TRACE=" + trace);
        System.out.println("WORLDLINE_M604_SIGNATURE=" + sha(trace));
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
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
        throw new IllegalStateException("no deterministic drowning foundation");
    }

    private static boolean water(int id) { return id == 8 || id == 9; }
    private static int local(int value, int chunk) { return value - chunk * 16; }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
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
