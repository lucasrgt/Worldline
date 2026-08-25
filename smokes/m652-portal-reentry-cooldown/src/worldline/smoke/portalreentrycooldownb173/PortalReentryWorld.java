package worldline.smoke.portalreentrycooldownb173;

import static worldline.b173server.B173FixtureSupport.*;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Official portal construction and dynamic destination-portal discovery. */
final class PortalReentryWorld {
    private PortalReentryWorld() { }

    static Activation activate(B173WireClient actor, RemoteChunkSnapshot initial,
            int cx, int cz) throws Exception {
        BlockPosition anchor = foundation(initial, cx, cz);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(initial.blockAt(local(anchor.x(), cx), anchor.y() + 1,
                local(anchor.z(), cz)).legacyId())) {
            anchor = place(actor, anchor, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded portal fixture");
        }
        anchor = place(actor, anchor, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
        actor.selectHeldSlot(1);
        BlockPosition bottom = place(actor, anchor, BlockFace.UP, 49);
        List<BlockPosition> frame = new ArrayList<>();
        frame.add(bottom);
        BlockPosition cursor = bottom;
        for (int east = 0; east < 3; east++) {
            cursor = place(actor, cursor, BlockFace.EAST, 49);
            frame.add(cursor);
        }
        BlockPosition left = bottom;
        BlockPosition right = cursor;
        for (int up = 0; up < 4; up++) {
            left = place(actor, left, BlockFace.UP, 49);
            right = place(actor, right, BlockFace.UP, 49);
            frame.add(left);
            frame.add(right);
        }
        cursor = left;
        for (int east = 0; east < 2; east++) {
            cursor = place(actor, cursor, BlockFace.EAST, 49);
            frame.add(cursor);
        }
        require(frame.size() == 14, "source portal frame size drifted");
        actor.selectHeldSlot(2);
        actor.useHeldItemOnBlock(new BlockPosition(bottom.x() + 1,
                bottom.y(), bottom.z()), BlockFace.UP);
        RemoteWorldView active = WorldlineSmokeAwait.awaitWorld(actor,
                world -> sourcePortalCells(world, bottom) == 6,
                "six source portal cells", 20);
        Portal source = new Portal(bottom.x() + 1, bottom.x() + 2,
                bottom.y() + 1, bottom.z(), bottom.z());
        require(frame(active, source) == 14, "source portal frame drifted");
        return new Activation(bottom, column, source);
    }

    static Portal find(RemoteWorldView world, PlayerPose pose) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = 999;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int count = 0;
        for (RemoteChunkSnapshot chunk : world.chunks()) {
            int baseX = chunk.observation().x();
            int baseZ = chunk.observation().z();
            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++)
                for (int y = 0; y < 128; y++)
                    if (chunk.blockAt(x, y, z).legacyId() == 90
                            && Math.abs(baseX + x - pose.x()) < 8D
                            && Math.abs(y - pose.y()) < 8D
                            && Math.abs(baseZ + z - pose.z()) < 8D) {
                        count++;
                        minX = Math.min(minX, baseX + x);
                        maxX = Math.max(maxX, baseX + x);
                        minY = Math.min(minY, y);
                        minZ = Math.min(minZ, baseZ + z);
                        maxZ = Math.max(maxZ, baseZ + z);
                    }
        }
        require(count == 6 && ((maxX - minX == 1 && maxZ == minZ)
                || (maxZ - minZ == 1 && maxX == minX)),
                "near-pose portal geometry drifted: " + count);
        return new Portal(minX, maxX, minY, minZ, maxZ);
    }

    static int frame(RemoteWorldView world, Portal portal) {
        int count = 0;
        if (portal.maxX > portal.minX) {
            for (int x = portal.minX - 1; x <= portal.maxX + 1; x++) {
                if (id(world, x, portal.minY - 1, portal.minZ) == 49)
                    count++;
                if (id(world, x, portal.minY + 3, portal.minZ) == 49)
                    count++;
            }
            for (int y = portal.minY; y <= portal.minY + 2; y++) {
                if (id(world, portal.minX - 1, y, portal.minZ) == 49)
                    count++;
                if (id(world, portal.maxX + 1, y, portal.minZ) == 49)
                    count++;
            }
        } else {
            for (int z = portal.minZ - 1; z <= portal.maxZ + 1; z++) {
                if (id(world, portal.minX, portal.minY - 1, z) == 49)
                    count++;
                if (id(world, portal.minX, portal.minY + 3, z) == 49)
                    count++;
            }
            for (int y = portal.minY; y <= portal.minY + 2; y++) {
                if (id(world, portal.minX, y, portal.minZ - 1) == 49)
                    count++;
                if (id(world, portal.minX, y, portal.maxZ + 1) == 49)
                    count++;
            }
        }
        return count;
    }

    private static int sourcePortalCells(RemoteWorldView world, BlockPosition bottom) {
        int count = 0;
        for (int y = 1; y <= 3; y++)
            for (int x = 1; x <= 2; x++)
                if (id(world, bottom.x() + x, bottom.y() + y, bottom.z()) == 90)
                    count++;
        return count;
    }
    private static int id(RemoteWorldView world, int x, int y, int z) {
        return world.blockAt(x, y, z).legacyId();
    }
    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 10; x++)
            for (int z = 4; z <= 11; z++)
                for (int y = 126; y >= 1; y--)
                if (chunk.blockAt(x, y, z).legacyId() == 3
                        && water(chunk.blockAt(x, y + 1, z).legacyId()))
                    return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic portal foundation");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }

    static final class Activation {
        final BlockPosition bottom;
        final int column;
        final Portal source;
        Activation(BlockPosition bottom, int column, Portal source) {
            this.bottom = bottom;
            this.column = column;
            this.source = source;
        }
    }
    static final class Portal {
        final int minX;
        final int maxX;
        final int minY;
        final int minZ;
        final int maxZ;
        Portal(int minX, int maxX, int minY, int minZ, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
        double insideX() { return minX + 0.5D; }
        double insideZ() { return minZ + 0.5D; }
        double outsideX() { return insideX() + (maxX == minX ? 2.5D : 0D); }
        double outsideZ() { return insideZ() + (maxZ == minZ ? 2.5D : 0D); }
        boolean contains(PlayerPose pose) {
            int x = (int) Math.floor(pose.x());
            int y = (int) Math.floor(pose.y());
            int z = (int) Math.floor(pose.z());
            return x >= minX && x <= maxX && y >= minY && y <= minY + 2
                    && z >= minZ && z <= maxZ;
        }
    }
}
