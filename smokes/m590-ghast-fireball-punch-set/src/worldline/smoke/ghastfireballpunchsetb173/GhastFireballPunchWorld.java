package worldline.smoke.ghastfireballpunchsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Nether ghast-fireball punch fixture: cavern platform, cobble pad, and spawner support. */
final class GhastFireballPunchWorld {
    private GhastFireballPunchWorld() {}

    static void inventory(Path workspace, String user, BlockPosition land) {
        B173PlayerSeed.writeInventory(workspace, user, land.x() + 0.5D, land.y() + 1.0D,
                land.z() + 0.5D, -1, new int[] {0, 1, 2, 3}, new int[] {87, 4, 52, 268},
                new int[] {64, 64, 1, 1}, new int[] {0, 0, 0, 0});
    }

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition site(RemoteWorldView view, BlockPosition top) {
        BlockFace[] faces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST};
        int i = 0;
        while (i < faces.length) {
            BlockPosition support = faces[i].adjacent(top);
            int ccx = Math.floorDiv(support.x(), 16);
            int ccz = Math.floorDiv(support.z(), 16);
            if (view.containsChunk(ccx, ccz)
                    && !air(view.blockAt(support.x(), support.y(), support.z()).legacyId())
                    && !lava(view.blockAt(support.x(), support.y(), support.z()).legacyId())
                    && air(view.blockAt(support.x(), support.y() + 1, support.z()).legacyId())
                    && !lava(view.blockAt(support.x(), support.y() + 1, support.z()).legacyId()))
                return support;
            i++;
        }
        throw new IllegalStateException("no adjacent netherrack spawner support");
    }

    static RemoteWorldView load(B173WireClient actor, BlockPosition land, int targetCx, int targetCz) {
        int lcx = Math.floorDiv(land.x(), 16);
        int lcz = Math.floorDiv(land.z(), 16);
        actor.awaitRemoteChunk(lcx, lcz);
        actor.moveAndObserve(0D, 0D, 0D, 20);
        return actor.awaitRemoteChunk(targetCx, targetCz);
    }

    static int[] pad(B173WireClient actor, RemoteWorldView view, BlockPosition top) throws Exception {
        int netherrack = 0;
        int cobble = 0;
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        int i = 0;
        while (i < faces.length) {
            int id = cobble == 0 ? 4 : 87;
            actor.selectHeldSlot(cobble == 0 ? 1 : 0);
            int added = fill(actor, view, top, faces[i], id);
            if (id == 4) cobble += added;
            else netherrack += added;
            i++;
        }
        return new int[] {netherrack, cobble};
    }

    static int fill(B173WireClient actor, RemoteWorldView view, BlockPosition top, BlockFace face,
            int id) throws Exception {
        BlockPosition placed = face.adjacent(top);
        int ccx = Math.floorDiv(placed.x(), 16);
        int ccz = Math.floorDiv(placed.z(), 16);
        if (!view.containsChunk(ccx, ccz)
                || !air(view.blockAt(placed.x(), placed.y(), placed.z()).legacyId())
                || lava(view.blockAt(placed.x(), placed.y(), placed.z()).legacyId())
                || lava(view.blockAt(placed.x(), placed.y() + 1, placed.z()).legacyId()))
            return 0;
        actor.placeHeldBlock(top, face);
        actor.awaitBlock(placed, new BlockState(id, 0));
        return 1;
    }

    static BlockPosition landing(RemoteChunkSnapshot chunk, int cx, int cz) {
        int x = 1;
        while (x <= 12) {
            int z = 1;
            while (z <= 12) {
                int y = 126;
                while (y >= 1) {
                    if (chunk.blockAt(x, y, z).legacyId() == 87 && air(chunk.blockAt(x, y + 1, z).legacyId())
                            && !lava(chunk.blockAt(x, y + 1, z).legacyId())
                            && !lava(chunk.blockAt(x, y + 2, z).legacyId()))
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic nether landing");
    }

    static BlockPosition cavern(RemoteWorldView view, int targetCx, int targetCz) {
        BlockPosition best = null;
        int bestRun = -1;
        for (RemoteChunkSnapshot chunk : view.chunks()) {
            int ccx = chunk.observation().x() >> 4;
            int ccz = chunk.observation().z() >> 4;
            if (ccx != targetCx || ccz != targetCz) continue;
            int x = 2;
            while (x <= 13) {
                int z = 2;
                while (z <= 13) {
                    int y = 1;
                    while (y < 128) {
                        if (!air(chunk.blockAt(x, y, z).legacyId())) {
                            y++;
                            continue;
                        }
                        int start = y;
                        while (y < 128 && air(chunk.blockAt(x, y, z).legacyId())
                                && !lava(chunk.blockAt(x, y, z).legacyId())) y++;
                        int run = y - start;
                        int floor = start - 1;
                        if (floor >= 1 && chunk.blockAt(x, floor, z).legacyId() == 87
                                && !lava(chunk.blockAt(x, floor, z).legacyId()) && run > bestRun) {
                            bestRun = run;
                            best = new BlockPosition(ccx * 16 + x, floor, ccz * 16 + z);
                        }
                    }
                    z++;
                }
                x++;
            }
        }
        if (best == null || bestRun < 6)
            throw new IllegalStateException("no deterministic nether ghast cavern run="
                    + bestRun + " chunk=" + targetCx + ":" + targetCz);
        return best;
    }

    static boolean air(int id) { return id == 0; }
    static boolean lava(int id) { return id == 10 || id == 11; }

    static int count(RemoteChunkSnapshot chunk, int id) {
        int n = 0;
        int x = 0;
        while (x < 16) {
            int z = 0;
            while (z < 16) {
                int y = 0;
                while (y < 128) {
                    if (chunk.blockAt(x, y, z).legacyId() == id) n++;
                    y++;
                }
                z++;
            }
            x++;
        }
        return n;
    }

    static int sky(RemoteChunkSnapshot chunk) {
        int n = 0;
        int x = 0;
        while (x < 16) {
            int z = 0;
            while (z < 16) {
                int y = 0;
                while (y < 128) {
                    if (chunk.skyLightAt(x, y, z) > 0) n++;
                    y++;
                }
                z++;
            }
            x++;
        }
        return n;
    }

    static void awaitPlayers(B173DedicatedServer server, int n) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == n) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        int i = 0;
        while (i < digest.length) {
            hex.append(String.format("%02x", digest[i] & 255));
            i++;
        }
        return hex.toString();
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
