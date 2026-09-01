package worldline.b173server;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;

/** Adds a second closed random-tick deck above the lower arena roof. */
final class B173MushroomRandomTickUpperDeck {
    private B173MushroomRandomTickUpperDeck() {
    }

    static void build(B173WireClient client, int[] used) throws Exception {
        walls(client, used);
        roof(client, used);
    }

    private static void walls(B173WireClient client, int[] used) throws Exception {
        for (int level = 5; level <= 7; level++) {
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 3) {
                    B173MushroomRandomTickStructure.stone(client, used,
                            B173MushroomRandomTickStructure.at(dx, level - 1, dz),
                            BlockFace.UP);
                }
            }
        }
    }

    private static void roof(B173WireClient client, int[] used) throws Exception {
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) == 3) {
                B173MushroomRandomTickStructure.stone(client, used,
                        B173MushroomRandomTickStructure.at(dx, 7, dz), BlockFace.UP);
            }
        }
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
        BlockPosition support;
        BlockFace face;
        if (Math.abs(dx) == radius) {
            support = B173MushroomRandomTickStructure.at(
                    dx + (dx >= 0 ? 1 : -1), 8, dz);
            face = dx >= 0 ? BlockFace.WEST : BlockFace.EAST;
        } else {
            support = B173MushroomRandomTickStructure.at(
                    dx, 8, dz + (dz >= 0 ? 1 : -1));
            face = dz >= 0 ? BlockFace.NORTH : BlockFace.SOUTH;
        }
        B173MushroomRandomTickStructure.stone(client, used, support, face);
    }
}
