package worldline.smoke.netherexitcreatesetb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173WireClient;

/** M382 4x5 obsidian 49 frame plus flint-and-steel 259 portal 90. */
final class NetherExitPortalKit {
    private NetherExitPortalKit() {}

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        for (int x = 4; x <= 10; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
        throw new IllegalStateException("no deterministic nether-exit-create-set foundation");
    }

    static BlockPosition standNether(RemoteWorldView view) {
        for (RemoteChunkSnapshot chunk : view.chunks()) {
            int chunkX = chunk.observation().x() >> 4, chunkZ = chunk.observation().z() >> 4;
            for (int x = 1; x <= 11; x++) for (int z = 2; z <= 14; z++) for (int y = 115; y >= 5; y--) {
                if (!ground(chunk.blockAt(x, y, z).legacyId())) continue;
                boolean clear = true;
                for (int dx = 0; dx <= 3 && clear; dx++) for (int dy = 1; dy <= 5; dy++) {
                    int above = chunk.blockAt(x + dx, y + dy, z).legacyId();
                    if (!air(above) || lava(above)) { clear = false; break; }
                }
                if (clear) return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
            }
        }
        throw new IllegalStateException("no nether-exit-create-set netherrack pad");
    }

    static Raised raise(B173WireClient actor, RemoteChunkSnapshot chunk, int chunkX, int chunkZ)
            throws Exception {
        BlockPosition anchor = foundation(chunk, chunkX, chunkZ);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(chunk.blockAt(local(anchor.x(), chunkX), anchor.y() + 1, local(anchor.z(), chunkZ)).legacyId())) {
            anchor = place(actor, anchor, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded nether-exit-create-set fixture");
        }
        anchor = place(actor, anchor, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        return new Raised(anchor, column + 1);
    }

    static Frame east(B173WireClient actor, BlockPosition support) throws Exception {
        actor.selectHeldSlot(1);
        BlockPosition bottom = place(actor, support, BlockFace.UP, 49);
        BlockPosition cursor = bottom;
        for (int index = 0; index < 3; index++) cursor = place(actor, cursor, BlockFace.EAST, 49);
        BlockPosition left = bottom, right = cursor;
        for (int index = 0; index < 4; index++) {
            left = place(actor, left, BlockFace.UP, 49);
            right = place(actor, right, BlockFace.UP, 49);
        }
        cursor = left;
        for (int index = 0; index < 2; index++) cursor = place(actor, cursor, BlockFace.EAST, 49);
        return new Frame(bottom);
    }

    static void light(B173WireClient actor, Frame frame, int settle) throws Exception {
        actor.selectHeldSlot(2);
        actor.useHeldItemOnBlock(new BlockPosition(frame.bottom.x() + 1, frame.bottom.y(), frame.bottom.z()),
                BlockFace.UP);
        RemoteWorldView active = actor.sustainTicks(settle);
        int portals = 0;
        for (int y = 1; y <= 3; y++) for (int x = 1; x <= 2; x++) {
            BlockState state = active.blockAt(frame.bottom.x() + x, frame.bottom.y() + y, frame.bottom.z());
            require(state.legacyId() == 90, "portal 90 absent at " + frame.bottom + " got " + state);
            portals++;
        }
        require(portals == 6, "portal interior drift");
    }

    static boolean water(int id) { return id == 8 || id == 9; }
    static boolean air(int id) { return id == 0; }
    static boolean lava(int id) { return id == 10 || id == 11; }
    static boolean ground(int id) {
        return id != 0 && !water(id) && !lava(id) && id != 51 && id != 90 && id != 7 && id != 78;
    }
    static int local(int value, int chunk) { return value - chunk * 16; }
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    static final class Raised {
        final BlockPosition top;
        final int column;
        Raised(BlockPosition top, int column) { this.top = top; this.column = column; }
    }

    static final class Frame {
        final BlockPosition bottom;
        Frame(BlockPosition bottom) { this.bottom = bottom; }
        double enterX() { return bottom.x() + 1.5D; }
        double enterY() { return bottom.y() + 1D; }
        double enterZ() { return bottom.z() + 0.5D; }
        String source() { return bottom.x() + ":" + bottom.y() + ":" + bottom.z(); }
    }
}
