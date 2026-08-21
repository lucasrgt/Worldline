package worldline.smoke.furnacefuelsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Raised-stone pads and idle furnace 61:2 placement for three sequential fuels. */
final class FurnaceFuelSetSupport {
    private FurnaceFuelSetSupport() {}

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition furnace(B173WireClient actor, BlockPosition support) throws Exception {
        BlockPosition target = BlockFace.UP.adjacent(support);
        actor.placeHeldBlock(support, BlockFace.UP);
        BlockState idle = new BlockState(61, 2);
        actor.awaitBlock(target, idle);
        require(idle.metadata() == 2 && !idle.equals(new BlockState(61, 0))
                && actor.sustainTicks(5).blockAt(target.x(), target.y(), target.z()).equals(idle),
                "idle furnace 61:2 drift");
        return target;
    }

    static Raised raise(B173WireClient actor, RemoteChunkSnapshot chunk, int cx, int cz)
            throws Exception {
        BlockPosition top = foundation(chunk, cx, cz);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(chunk.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded furnace-fuel-set fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column++;
        }
        BlockPosition east = place(actor, top, BlockFace.EAST, 1);
        BlockPosition west = place(actor, top, BlockFace.WEST, 1);
        return new Raised(top, east, west, column);
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic furnace-fuel-set foundation");
    }

    static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long end = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) return;
            Thread.sleep(100L);
        }
        throw new IllegalStateException("player count drift");
    }

    static String item(RemoteItemStack stack) {
        return stack.legacyId() + "x" + stack.count() + ":" + stack.damage();
    }

    static String cell(BlockPosition position, int id, int metadata) {
        return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + metadata;
    }

    static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte item : digest) hex.append(String.format("%02x", item & 255));
        return hex.toString();
    }

    static boolean water(int id) { return id == 8 || id == 9; }
    static int local(int value, int chunk) { return value - chunk * 16; }
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    static final class Raised {
        final BlockPosition support, east, west;
        final int column;
        Raised(BlockPosition support, BlockPosition east, BlockPosition west, int column) {
            this.support = support; this.east = east; this.west = west; this.column = column;
        }
    }
}
