package worldline.smoke.undeadsunburnsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Raised grass pad, one-block cover roof, and nearby Packet24 waits. */
final class UndeadSunBurnSetSupport {
    private UndeadSunBurnSetSupport() {}

    static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int chunkX,
            int chunkZ, int[] column) throws Exception {
        BlockPosition top = foundation(initial, chunkX, chunkZ);
        column[0] = 0;
        actor.selectHeldSlot(0);
        while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ))
                .legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column[0] <= 15, "water column exceeded undead-sun-burn fixture");
        }
        int lift = 0;
        while (lift < 8) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column[0]++;
            lift++;
        }
        return top;
    }

    static void grassPlatform(B173WireClient actor, BlockPosition top) throws Exception {
        actor.selectHeldSlot(1);
        int radius = 1;
        while (radius <= 3) {
            ring(actor, top, radius, 2);
            radius++;
        }
    }

    static void centerRoof(B173WireClient actor, BlockPosition top) throws Exception {
        actor.selectHeldSlot(0);
        BlockPosition south = new BlockPosition(top.x() - 2, top.y(), top.z() - 2);
        BlockPosition post = place(actor, south, BlockFace.UP, 1);
        post = place(actor, post, BlockFace.UP, 1);
        BlockPosition start = place(actor, post, BlockFace.UP, 1);
        int row = 0;
        while (row < 5) {
            BlockPosition cursor = start;
            if (row > 0) {
                cursor = place(actor, new BlockPosition(start.x(), start.y(), start.z() + row - 1),
                        BlockFace.SOUTH, 1);
            }
            int col = 0;
            while (col < 4) {
                cursor = place(actor, cursor, BlockFace.EAST, 1);
                col++;
            }
            row++;
        }
    }

    static RemoteMobSpawn awaitPad(B173WireClient actor, BlockPosition spawner, double range) {
        int n = 0;
        while (n < 16) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(54);
            double dx = spawn.x() - (spawner.x() + 0.5D);
            double dz = spawn.z() - (spawner.z() + 0.5D);
            if (dx * dx + dz * dz <= range * range && Math.abs(spawn.y() - spawner.y()) <= 1.6D)
                return spawn;
            n++;
        }
        throw new IllegalStateException("pad type54 absent");
    }

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long end = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) return;
            Thread.sleep(100L);
        }
        throw new IllegalStateException("player count drift");
    }

    static String cell(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z() + ":52:0";
    }

    static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < bytes.length) {
            result.append(String.format("%02x", bytes[index] & 255));
            index++;
        }
        return result.toString();
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static void ring(B173WireClient actor, BlockPosition center, int radius, int id)
            throws Exception {
        int z = -radius + 1;
        while (z < radius) {
            place(actor, new BlockPosition(center.x() - radius + 1, center.y(), center.z() + z),
                    BlockFace.WEST, id);
            place(actor, new BlockPosition(center.x() + radius - 1, center.y(), center.z() + z),
                    BlockFace.EAST, id);
            z++;
        }
        int x = -radius + 1;
        while (x < radius) {
            place(actor, new BlockPosition(center.x() + x, center.y(), center.z() - radius + 1),
                    BlockFace.NORTH, id);
            place(actor, new BlockPosition(center.x() + x, center.y(), center.z() + radius - 1),
                    BlockFace.SOUTH, id);
            x++;
        }
        place(actor, new BlockPosition(center.x() - radius, center.y(), center.z() - radius + 1),
                BlockFace.NORTH, id);
        place(actor, new BlockPosition(center.x() - radius, center.y(), center.z() + radius - 1),
                BlockFace.SOUTH, id);
        place(actor, new BlockPosition(center.x() + radius, center.y(), center.z() - radius + 1),
                BlockFace.NORTH, id);
        place(actor, new BlockPosition(center.x() + radius, center.y(), center.z() + radius - 1),
                BlockFace.SOUTH, id);
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
        throw new IllegalStateException("no deterministic undead-sun-burn foundation");
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int value, int chunk) {
        return value - chunk * 16;
    }
}
