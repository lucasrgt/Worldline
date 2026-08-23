package worldline.smoke.mushroomspreadsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Roofed 7x7 stone pad, striped mushrooms, glass control, bounded random-tick wait. */
public final class MushroomSpreadSetArm {
    static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz,
            int[] column, int[] used) throws Exception {
        BlockPosition top = foundation(initial, cx, cz);
        column[0] = 0;
        while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = stone(a, used, top, BlockFace.UP);
            a.moveAndObserve(0D, 1D, 0D, 1);
            require(++column[0] <= 15, "water column exceeded mushroom-spread fixture");
        }
        int lift = 0;
        while (lift < 8) {
            top = stone(a, used, top, BlockFace.UP);
            a.moveAndObserve(0D, 1D, 0D, 1);
            column[0]++;
            lift++;
        }
        station(a, top.x() + 0.5D, top.y() + 1.1D, top.z() + 0.5D);
        return top;
    }

    static BlockPosition glassFloor(B173WireClient a, BlockPosition top, int[] used)
            throws Exception {
        BlockPosition glass = null;
        int r = 1;
        while (r <= 3) {
            int dx = -r;
            while (dx <= r) {
                int dz = -r;
                while (dz <= r) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                        glass = ringCell(a, top, used, glass, dx, dz);
                    }
                    dz++;
                }
                dx++;
            }
            r++;
        }
        require(glass != null, "glass floor cell missing");
        return glass;
    }

    static BlockPosition ringCell(B173WireClient a, BlockPosition top, int[] used,
            BlockPosition glass, int dx, int dz) throws Exception {
        BlockPosition support;
        BlockFace face;
        if (Math.abs(dx) == Math.max(Math.abs(dx), Math.abs(dz))) {
            support = new BlockPosition(top.x() + dx - (dx > 0 ? 1 : -1), top.y(), top.z() + dz);
            face = dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            support = new BlockPosition(top.x() + dx, top.y(), top.z() + dz - (dz > 0 ? 1 : -1));
            face = dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
        if (dx == 0 && dz == 1) {
            a.selectHeldSlot(6);
            return place(a, support, face, 20);
        }
        stone(a, used, support, face);
        return glass;
    }

    static void walls(B173WireClient a, BlockPosition top, int[] used) throws Exception {
        int dx = -3;
        while (dx <= 3) {
            int dz = -3;
            while (dz <= 3) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 3) {
                    stone(a, used, new BlockPosition(top.x() + dx, top.y(), top.z() + dz),
                            BlockFace.UP);
                }
                dz++;
            }
            dx++;
        }
    }

    static BlockPosition roof(B173WireClient a, BlockPosition top, int[] used) throws Exception {
        station(a, top.x() + 0.5D, top.y() + 3.1D, top.z() + 0.5D);
        int dx = -3;
        while (dx <= 3) {
            int dz = -3;
            while (dz <= 3) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 3) {
                    stone(a, used, new BlockPosition(top.x() + dx, top.y() + 1, top.z() + dz),
                            BlockFace.UP);
                }
                dz++;
            }
            dx++;
        }
        int r = 2;
        while (r >= 0) {
            fillRoofRing(a, top, used, r);
            r--;
        }
        return new BlockPosition(top.x(), top.y() + 2, top.z());
    }

    static void fillRoofRing(B173WireClient a, BlockPosition top, int[] used, int r)
            throws Exception {
        int dx = -r;
        while (dx <= r) {
            int dz = -r;
            while (dz <= r) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                    roofCell(a, top, used, dx, dz, r);
                }
                dz++;
            }
            dx++;
        }
    }

    static void roofCell(B173WireClient a, BlockPosition top, int[] used, int dx, int dz, int r)
            throws Exception {
        BlockPosition support;
        BlockFace face;
        if (Math.abs(dx) == r) {
            support = new BlockPosition(top.x() + dx + (dx > 0 ? 1 : -1), top.y() + 2, top.z() + dz);
            face = dx > 0 ? BlockFace.WEST : BlockFace.EAST;
        } else {
            support = new BlockPosition(top.x() + dx, top.y() + 2, top.z() + dz + (dz > 0 ? 1 : -1));
            face = dz > 0 ? BlockFace.NORTH : BlockFace.SOUTH;
        }
        stone(a, used, support, face);
    }

    static void plant(B173WireClient a, BlockPosition top, BlockPosition[] sources,
            BlockPosition[] targets) throws Exception {
        int s = 0;
        int t = 0;
        int dx = -2;
        while (dx <= 2) {
            int dz = -2;
            while (dz <= 2) {
                int[] counts = plantCell(a, top, sources, targets, s, t, dx, dz);
                s = counts[0];
                t = counts[1];
                dz++;
            }
            dx++;
        }
        require(s == 15 && t == 9, "mushroom-spread pad drift");
    }

    static int[] plantCell(B173WireClient a, BlockPosition top, BlockPosition[] sources,
            BlockPosition[] targets, int s, int t, int dx, int dz) throws Exception {
        BlockPosition floor = new BlockPosition(top.x() + dx, top.y(), top.z() + dz);
        BlockPosition up = BlockFace.UP.adjacent(floor);
        if (dx == 0 && dz == 1) return new int[] {s, t};
        if (((dz + 2) & 1) == 0) {
            int id = ((dx + 2) & 1) == 0 ? 39 : 40;
            a.selectHeldSlot(id == 39 ? 4 : 5);
            sources[s] = place(a, floor, BlockFace.UP, id);
            return new int[] {s + 1, t};
        }
        targets[t] = up;
        return new int[] {s, t + 1};
    }

    static BlockPosition stone(B173WireClient a, int[] used, BlockPosition support, BlockFace face)
            throws Exception {
        a.selectHeldSlot(used[0] / 64);
        used[0]++;
        return place(a, support, face, 1);
    }

    static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        a.placeHeldBlock(support, face);
        a.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static void station(B173WireClient a, double x, double y, double z) throws Exception {
        int n = 0;
        while (n < 16) {
            PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x();
            double dy = y - here.y();
            double dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 1D) return;
            double s = Math.min(1D, 8D / dist);
            a.moveAndObserve(dx * s, dy * s, dz * s, 4);
            n++;
        }
    }

    static BlockPosition foundation(RemoteChunkSnapshot c, int cx, int cz) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (c.blockAt(x, y, z).legacyId() == 3
                            && water(c.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic mushroom-spread foundation");
    }

    static int id(RemoteWorldView v, BlockPosition p) {
        return v.blockAt(p.x(), p.y(), p.z()).legacyId();
    }

    static BlockState at(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
        return c.blockAt(p.x() - cx * 16, p.y(), p.z() - cz * 16);
    }

    static boolean water(int id) {
        return id == 8 || id == 9;
    }

    static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
        long e = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < e) {
            if (s.players().size() == n) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    static String token(BlockPosition p, int id, int meta) {
        return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
    }

    static String sha(String s) throws Exception {
        byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder v = new StringBuilder();
        int i = 0;
        while (i < b.length) {
            v.append(String.format("%02x", b[i] & 255));
            i++;
        }
        return v.toString();
    }

    static void require(boolean v, String m) {
        if (!v) throw new IllegalStateException(m);
    }
}
