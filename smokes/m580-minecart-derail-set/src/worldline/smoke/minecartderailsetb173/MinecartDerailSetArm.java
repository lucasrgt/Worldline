package worldline.smoke.minecartderailsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.*;
import worldline.b173server.*;

/** Dead-end north-south rail with a wooden-plate landing instead of a continuing rail. */
public final class MinecartDerailSetArm {
    final BlockPosition support, wall, bumper, powered, track, plate, torch;

    private MinecartDerailSetArm(BlockPosition s, BlockPosition w, BlockPosition b,
            BlockPosition p, BlockPosition t, BlockPosition l, BlockPosition o) {
        support = s;
        wall = w;
        bumper = b;
        powered = p;
        track = t;
        plate = l;
        torch = o;
    }

    static MinecartDerailSetArm place(B173WireClient a, BlockPosition top) throws Exception {
        BlockPosition northPad = place(a, top, BlockFace.NORTH, 1);
        BlockPosition wall = place(a, northPad, BlockFace.UP, 1);
        BlockPosition southPad = place(a, top, BlockFace.SOUTH, 1);
        BlockPosition platePad = place(a, southPad, BlockFace.SOUTH, 1);
        BlockPosition bumperPad = place(a, platePad, BlockFace.SOUTH, 1);
        BlockPosition bumper = place(a, bumperPad, BlockFace.UP, 1);
        BlockPosition eastPad = place(a, top, BlockFace.EAST, 1);
        a.selectHeldSlot(1);
        BlockPosition powered = BlockFace.UP.adjacent(top);
        a.placeHeldBlock(top, BlockFace.UP);
        a.awaitBlock(powered, new BlockState(27, 0));
        a.selectHeldSlot(2);
        BlockPosition track = BlockFace.UP.adjacent(southPad);
        a.placeHeldBlock(southPad, BlockFace.UP);
        a.awaitBlock(track, new BlockState(66, 0));
        a.selectHeldSlot(3);
        BlockPosition plate = BlockFace.UP.adjacent(platePad);
        a.placeHeldBlock(platePad, BlockFace.UP);
        a.awaitBlock(plate, new BlockState(72, 0));
        require(powered.x() == track.x() && track.x() == plate.x()
                && track.z() == powered.z() + 1 && plate.z() == track.z() + 1
                && wall.z() == powered.z() - 1 && bumper.z() == plate.z() + 1,
                "dead-end north-south derail track drift");
        return new MinecartDerailSetArm(top, wall, bumper, powered, track, plate,
                BlockFace.UP.adjacent(eastPad));
    }

    RemoteObjectSpawn idleCart(B173WireClient a) throws Exception {
        a.moveAndObserve(-1D, 0D, 0D, 1);
        a.selectHeldSlot(4);
        a.useHeldItemOnBlock(powered, BlockFace.UP);
        RemoteObjectSpawn cart = a.awaitObjectSpawn(10);
        require(cart.type() == 10 && cart.throwerId() == 0 && cart.velocityX() == 0
                && cart.velocityY() == 0 && cart.velocityZ() == 0
                && cart.fixedX() == powered.x() * 32 + 16
                && cart.fixedY() == powered.y() * 32 + 27
                && cart.fixedZ() == powered.z() * 32 + 16
                && cart.fixedZ() != plate.z() * 32 + 16,
                "minecart packet23 type10 spawn bounds drift");
        RemoteWorldView idle = worldline.test.WorldlineSmokeAwait.observe(a, 10);
        require(idle.blockAt(plate.x(), plate.y(), plate.z()).equals(new BlockState(72, 0))
                && idle.blockAt(powered.x(), powered.y(), powered.z())
                        .equals(new BlockState(27, 0))
                && idle.blockAt(track.x(), track.y(), track.z()).equals(new BlockState(66, 0)),
                "unpowered powered-rail launched the cart onto the plate");
        return cart;
    }

    BlockState launch(B173WireClient a) throws Exception {
        a.selectHeldSlot(5);
        a.placeHeldBlock(BlockFace.DOWN.adjacent(torch), BlockFace.UP);
        BlockState placedTorch = new BlockState(76, 5);
        worldline.test.WorldlineSmokeAwait.awaitBlock(a, torch, placedTorch, 10);
        worldline.test.WorldlineSmokeAwait.awaitBlock(a, powered, new BlockState(27, 8), 10);
        require(worldline.test.WorldlineSmokeAwait
                .awaitBlock(a, plate, new BlockState(72, 1), 80)
                .blockAt(plate.x(), plate.y(), plate.z()).equals(new BlockState(72, 1)),
                "moving minecart did not derail onto wooden plate 72");
        return placedTorch;
    }

    void persist(RemoteChunkSnapshot after, int cx, int cz, BlockState torchState) {
        require(at(after, powered, cx, cz).equals(new BlockState(27, 8))
                && at(after, track, cx, cz).equals(new BlockState(66, 0))
                && at(after, plate, cx, cz).equals(new BlockState(72, 1))
                && at(after, torch, cx, cz).equals(torchState)
                && at(after, wall, cx, cz).equals(new BlockState(1, 0))
                && at(after, bumper, cx, cz).equals(new BlockState(1, 0)),
                "persisted minecart-derail-set drift");
    }

    static BlockPosition raise(B173WireClient a, RemoteChunkSnapshot initial, int cx, int cz,
            int[] column) throws Exception {
        BlockPosition top = foundation(initial, cx, cz);
        column[0] = 0;
        a.selectHeldSlot(0);
        while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = place(a, top, BlockFace.UP, 1);
            a.moveAndObserve(0D, 1D, 0D, 1);
            require(++column[0] <= 15, "water column exceeded minecart-derail fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = place(a, top, BlockFace.UP, 1);
            a.moveAndObserve(0D, 1D, 0D, 1);
            column[0]++;
        }
        return top;
    }

    static BlockPosition place(B173WireClient a, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        a.placeHeldBlock(support, face);
        a.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition foundation(RemoteChunkSnapshot c, int cx, int cz) {
        for (int x = 4; x <= 11; x++)
            for (int z = 4; z <= 11; z++)
                for (int y = 126; y >= 1; y--)
                    if (c.blockAt(x, y, z).legacyId() == 3
                            && water(c.blockAt(x, y + 1, z).legacyId()))
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic minecart-derail foundation");
    }

    static BlockState at(RemoteChunkSnapshot c, BlockPosition p, int cx, int cz) {
        return c.blockAt(p.x() - cx * 16, p.y(), p.z() - cz * 16);
    }

    static boolean water(int id) {
        return id == 8 || id == 9;
    }

    static String cell(BlockPosition p) {
        return p.x() + ":" + p.y() + ":" + p.z();
    }

    static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
        long e = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < e) {
            if (s.players().size() == n)
                return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    static String sha(String s) throws Exception {
        byte[] b = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder v = new StringBuilder();
        for (byte x : b)
            v.append(String.format("%02x", x & 255));
        return v.toString();
    }

    static void require(boolean v, String m) {
        if (!v)
            throw new IllegalStateException(m);
    }
}
