package worldline.smoke.boatcurrentpushb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Raised east-flowing water channel for one boat current observation. */
public final class BoatCurrentPushChannel {
    final BlockPosition source, flow, south;
    final int column, fluid, meta;

    private BoatCurrentPushChannel(BlockPosition source, BlockPosition flow, BlockPosition south,
            int column, int fluid, int meta) {
        this.source = source; this.flow = flow; this.south = south;
        this.column = column; this.fluid = fluid; this.meta = meta;
    }

    static BoatCurrentPushChannel build(B173WireClient actor, RemoteChunkSnapshot initial,
            int cx, int cz) throws Exception {
        BlockPosition top = foundation(initial, cx, cz); int column = 0; actor.selectHeldSlot(0);
        while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1); actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded boat-current fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = place(actor, top, BlockFace.UP, 1); actor.moveAndObserve(0D, 1D, 0D, 1); column++;
        }
        BlockPosition e1 = place(actor, top, BlockFace.EAST, 1);
        BlockPosition e2 = place(actor, e1, BlockFace.EAST, 1);
        BlockPosition e3 = place(actor, e2, BlockFace.EAST, 1);
        BlockPosition e4 = place(actor, e3, BlockFace.EAST, 1);
        BlockPosition[] floor = new BlockPosition[] {top, e1, e2, e3, e4};
        for (int index = 0; index < floor.length; index++) {
            BlockPosition north = place(actor, floor[index], BlockFace.NORTH, 1);
            BlockPosition south = place(actor, floor[index], BlockFace.SOUTH, 1);
            place(actor, north, BlockFace.UP, 1); place(actor, south, BlockFace.UP, 1);
        }
        BlockPosition west = place(actor, top, BlockFace.WEST, 1);
        place(actor, west, BlockFace.UP, 1); place(actor, e4, BlockFace.UP, 1);
        BlockPosition westWall = BlockFace.UP.adjacent(west);
        station(actor, westWall.x() + 0.5D, westWall.y() + 1.1D, westWall.z() + 0.5D);
        actor.selectHeldSlot(2);
        BlockPosition source = place(actor, top, BlockFace.UP, 9);
        worldline.test.WorldlineSmokeAwait.observe(actor, 5);
        actor.selectHeldSlot(1);
        BlockPosition gate = place(actor, e1, BlockFace.UP, 3);
        worldline.test.WorldlineSmokeAwait.observe(actor, 5);
        actor.selectHeldSlot(4); actor.beginBreak(gate); Thread.sleep(3000L); actor.finishBreak(gate);
        actor.awaitBlock(gate, new BlockState(0, 0));
        BlockPosition flow = BlockFace.UP.adjacent(e2);
        BlockState flowing = worldline.test.WorldlineSmokeAwait.awaitBlock(actor, flow,
                new BlockState(8, 2), 80).blockAt(flow.x(), flow.y(), flow.z());
        return new BoatCurrentPushChannel(source, flow,
                BlockFace.UP.adjacent(BlockFace.SOUTH.adjacent(e2)), column,
                flowing.legacyId(), flowing.metadata());
    }

    static void station(B173WireClient actor, double x, double y, double z) {
        for (int n = 0; n < 48; n++) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 0.6D) return;
            double scale = Math.min(1D, 1D / dist);
            actor.moveAndObserve(dx * scale, dy * scale, dz * scale, 2);
        }
        PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        require((x - here.x()) * (x - here.x()) + (z - here.z()) * (z - here.z()) <= 2.25D
                && Math.abs(y - here.y()) <= 3D,
                "station pose drift x=" + here.x() + " y=" + here.y() + " z=" + here.z());
    }

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++)
            for (int y = 126; y >= 1; y--)
                if (chunk.blockAt(x, y, z).legacyId() == 3
                        && water(chunk.blockAt(x, y + 1, z).legacyId()))
                    return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic boat-current foundation");
    }

    static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
        return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
    }

    static boolean water(int id) { return id == 8 || id == 9; }
    static String cell(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z();
    }

    static void awaitPlayers(B173DedicatedServer server, int n) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == n) return; Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) hex.append(String.format("%02x", b & 255));
        return hex.toString();
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
