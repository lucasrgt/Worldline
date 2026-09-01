package worldline.b173server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Gameplay-authored 7x7 dark arena shared by both mushroom species. */
final class B173MushroomRandomTickStructure {
    static final BlockState STONE = new BlockState(1, 0);
    static final BlockPosition CENTER = new BlockPosition(4, 71, 4);
    static final BlockPosition CONTROL = new BlockPosition(4, 72, 5);
    static final BlockPosition LIGHT_PROBE = new BlockPosition(2, 72, 2);
    private B173MushroomRandomTickStructure() { }

    static void build(B173WireClient client, BlockPosition center) throws Exception {
        int[] used = {17}; floor(client, center, used); walls(client, center, used);
        roof(client, center, used); B173MushroomRandomTickUpperDeck.build(client, used);
        torch(client);
        require(center.equals(CENTER) && used[0] <= 320, "mushroom arena geometry drift");
    }
    static void sealLowerDoorway(B173WireClient client) throws Exception {
        place(client, 4, at(0, 0, -3), BlockFace.UP, 1);
        place(client, 4, at(0, 1, -3), BlockFace.UP, 1);
    }
    static List<BlockPosition> sourceSupports() {
        List<BlockPosition> result = new ArrayList<BlockPosition>();
        addSourceSupports(result, 0);
        addSourceSupports(result, 4);
        return Collections.unmodifiableList(result);
    }
    private static void addSourceSupports(List<BlockPosition> result, int dy) {
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (((dz + 2) & 1) == 0) result.add(at(dx, dy, dz));
        }
    }
    static List<BlockPosition> targets() {
        List<BlockPosition> result = new ArrayList<BlockPosition>();
        addTargets(result, 1, true);
        addTargets(result, 5, false);
        return Collections.unmodifiableList(result);
    }
    private static void addTargets(List<BlockPosition> result, int dy, boolean control) {
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            BlockPosition target = at(dx, dy, dz);
            if (((dz + 2) & 1) != 0 && (!control || !target.equals(CONTROL))) {
                result.add(target);
            }
        }
    }
    private static void floor(B173WireClient client, BlockPosition center, int[] used)
            throws Exception {
        for (int radius = 1; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                        floorCell(client, center, used, dx, dz);
                    }
                }
            }
        }
    }
    private static void floorCell(B173WireClient client, BlockPosition center, int[] used,
            int dx, int dz) throws Exception {
        BlockPosition support; BlockFace face;
        if (Math.abs(dx) == Math.max(Math.abs(dx), Math.abs(dz))) {
            support = at(dx - Integer.signum(dx), 0, dz);
            face = dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            support = at(dx, 0, dz - Integer.signum(dz));
            face = dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
        if (dx == 0 && dz == 1) place(client, 6, support, face, 20);
        else stone(client, used, support, face);
    }
    private static void walls(B173WireClient client, BlockPosition center, int[] used)
            throws Exception {
        for (int level = 1; level <= 3; level++) {
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 3
                        && !(dx == 0 && dz == -3)) {
                    stone(client, used, at(dx, level - 1, dz), BlockFace.UP);
                }
            }
        }
    }
    private static void roof(B173WireClient client, BlockPosition center, int[] used)
            throws Exception {
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) == 3 && !(dx == 0 && dz == -3)) {
                stone(client, used, at(dx, 3, dz), BlockFace.UP);
            }
        }
        stone(client, used, at(-1, 4, -3), BlockFace.EAST);
        for (int radius = 2; radius >= 0; radius--) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                        roofCell(client, used, dx, dz, radius);
                    }
                }
            }
        }
    }
    private static void roofCell(B173WireClient client, int[] used, int dx, int dz,
            int radius) throws Exception {
        BlockPosition support; BlockFace face;
        if (Math.abs(dx) == radius) {
            support = at(dx + (dx >= 0 ? 1 : -1), 4, dz);
            face = dx >= 0 ? BlockFace.WEST : BlockFace.EAST;
        } else {
            support = at(dx, 4, dz + (dz >= 0 ? 1 : -1));
            face = dz >= 0 ? BlockFace.NORTH : BlockFace.SOUTH;
        }
        stone(client, used, support, face);
    }
    private static void torch(B173WireClient client) throws Exception {
        BlockPosition support = at(-3, 3, -2);
        BlockPosition target = BlockFace.EAST.adjacent(support);
        client.selectHeldSlot(7);
        client.placeHeldBlock(support, BlockFace.EAST);
        client.awaitBlock(target, new BlockState(50, 1));
    }
    static BlockPosition stone(B173WireClient client, int[] used,
            BlockPosition support, BlockFace face) throws Exception {
        int slot = used[0]++ / 64; require(slot <= 4, "mushroom arena stone budget drift");
        return place(client, slot, support, face, 1);
    }
    private static BlockPosition place(B173WireClient client, int slot,
            BlockPosition support, BlockFace face, int id) throws Exception {
        client.selectHeldSlot(slot); BlockPosition target = face.adjacent(support);
        client.placeHeldBlock(support, face); client.awaitBlock(target, new BlockState(id, 0));
        return target;
    }
    static BlockPosition at(int dx, int dy, int dz) {
        return new BlockPosition(CENTER.x() + dx, CENTER.y() + dy, CENTER.z() + dz);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
