package worldline.smoke.portalpairsetb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173WireClient;

/** Raised stone pad plus two east-facing 4x5 obsidian frames in one 8:1 cell. */
final class PortalPairFrames {
    private PortalPairFrames() {}

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
        throw new IllegalStateException("no deterministic portal-pair foundation");
    }

    static Raised raise(B173WireClient actor, RemoteChunkSnapshot chunk, int chunkX, int chunkZ)
            throws Exception {
        BlockPosition anchor = foundation(chunk, chunkX, chunkZ);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(chunk.blockAt(local(anchor.x(), chunkX), anchor.y() + 1, local(anchor.z(), chunkZ)).legacyId())) {
            anchor = place(actor, anchor, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded portal-pair fixture");
        }
        anchor = place(actor, anchor, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        return new Raised(anchor, column + 1);
    }

    static Frame east(B173WireClient actor, BlockPosition support) throws Exception {
        actor.selectHeldSlot(1);
        return eastFromBottom(actor, place(actor, support, BlockFace.UP, 49));
    }

    static Frame eastFromBottom(B173WireClient actor, BlockPosition bottom) throws Exception {
        actor.selectHeldSlot(1);
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

    static Frame pairNeighbor(B173WireClient actor, PortalPairTravel.Portal landed) throws Exception {
        BlockPosition origin = bottomOf(landed);
        boolean eastFacing = landed.maxX > landed.minX;
        BlockFace[] dirs = eastFacing
                ? new BlockFace[] {BlockFace.SOUTH, BlockFace.NORTH}
                : new BlockFace[] {BlockFace.EAST, BlockFace.WEST};
        RemoteWorldView view = actor.sustainTicks(1);
        Exception last = null;
        for (int i = 0; i < dirs.length; i++) {
            BlockFace dir = dirs[i];
            BlockPosition beside = dir.adjacent(origin);
            if (eastFacing && cell(beside.z()) != cell(origin.z())) continue;
            if (!eastFacing && cell(beside.x()) != cell(origin.x())) continue;
            try { return seatNeighbor(actor, origin, beside, dir, view); }
            catch (Exception error) { last = error; }
        }
        if (last != null) throw last;
        throw new IllegalStateException("no 8:1 neighbor cell from " + origin);
    }

    static Frame seatNeighbor(B173WireClient actor, BlockPosition origin, BlockPosition beside, BlockFace dir,
            RemoteWorldView view) throws Exception {
        int id = view.blockAt(beside.x(), beside.y(), beside.z()).legacyId();
        actor.selectHeldSlot(0);
        BlockPosition pad;
        if (id == 0 || water(id)) {
            pad = place(actor, origin, dir, 1);
            actor.selectHeldSlot(1);
            BlockPosition next = dir.adjacent(pad);
            int nextId = actor.sustainTicks(1).blockAt(next.x(), next.y(), next.z()).legacyId();
            if (nextId == 0 || water(nextId)) return eastFromBottom(actor, place(actor, pad, dir, 49));
            return eastFromBottom(actor, place(actor, pad, BlockFace.UP, 49));
        }
        require(id != 90 && id != 49 && id != 7, "neighbor beside is portal/obsidian/bedrock id=" + id);
        pad = place(actor, beside, BlockFace.UP, 1);
        actor.selectHeldSlot(1);
        return eastFromBottom(actor, place(actor, pad, BlockFace.UP, 49));
    }

    static BlockPosition bottomOf(PortalPairTravel.Portal portal) {
        if (portal.maxX > portal.minX) return new BlockPosition(portal.minX - 1, portal.minY - 1, portal.minZ);
        return new BlockPosition(portal.minX, portal.minY - 1, portal.minZ - 1);
    }

    static void light(B173WireClient actor, Frame frame) throws Exception {
        actor.selectHeldSlot(2);
        actor.useHeldItemOnBlock(new BlockPosition(frame.bottom.x() + 1, frame.bottom.y(), frame.bottom.z()),
                BlockFace.UP);
    }

    static void requireLit(RemoteWorldView view, Frame frame) {
        int portals = 0;
        for (int y = 1; y <= 3; y++) for (int x = 1; x <= 2; x++) {
            BlockState state = view.blockAt(frame.bottom.x() + x, frame.bottom.y() + y, frame.bottom.z());
            require(state.legacyId() == 90, "portal 90 absent at pair frame " + frame.bottom + " got " + state);
            portals++;
        }
        require(portals == 6, "pair frame interior drift");
    }

    static int cell(int value) { return Math.floorDiv(value, 8); }

    static boolean water(int id) { return id == 8 || id == 9; }

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
        int cellX() { return cell(bottom.x() + 1); }
        int cellZ() { return cell(bottom.z()); }
        String source() { return bottom.x() + ":" + bottom.y() + ":" + bottom.z(); }
    }
}
