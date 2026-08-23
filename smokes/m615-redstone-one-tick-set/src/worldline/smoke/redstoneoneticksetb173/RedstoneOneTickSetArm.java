package worldline.smoke.redstoneoneticksetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** West sticky 29 with a cloned M142 east wall lever, pulsed for one redstone tick. */
public final class RedstoneOneTickSetArm {
    static final BlockState COBBLE = new BlockState(4, 0);
    static final BlockState PISTON = new BlockState(29, 4);
    static final BlockState LEVER_OFF = new BlockState(69, 1);
    static final BlockState LEVER_ON = new BlockState(69, 9);
    final BlockPosition support, piston, head, pushed, lever;

    private RedstoneOneTickSetArm(BlockPosition support, BlockPosition piston, BlockPosition head,
            BlockPosition pushed, BlockPosition lever) {
        this.support = support; this.piston = piston; this.head = head; this.pushed = pushed;
        this.lever = lever;
    }

    static RedstoneOneTickSetArm place(B173WireClient actor, RemoteChunkSnapshot initial,
            int cx, int cz, int[] column) throws Exception {
        BlockPosition top = raise(actor, initial, cx, cz, column);
        BlockPosition piston = BlockFace.UP.adjacent(top);
        BlockPosition head = BlockFace.WEST.adjacent(piston);
        BlockPosition pushed = BlockFace.WEST.adjacent(head);
        BlockPosition lever = BlockFace.EAST.adjacent(top);
        require(air(initial, cx, cz, piston, head, pushed, lever), "one-tick targets were not initial air");
        actor.look(-90F, 0F);
        worldline.test.WorldlineSmokeAwait.observe(actor, 2);
        actor.selectHeldSlot(1);
        actor.placeHeldBlock(top, BlockFace.UP);
        actor.awaitBlock(piston, PISTON);
        actor.selectHeldSlot(2);
        actor.placeHeldBlock(piston, BlockFace.WEST);
        actor.awaitBlock(head, COBBLE);
        actor.selectHeldSlot(3);
        actor.placeHeldBlock(top, BlockFace.EAST);
        require(worldline.test.WorldlineSmokeAwait.awaitBlock(actor, lever, LEVER_OFF, 8)
                .blockAt(lever.x(), lever.y(), lever.z()).equals(LEVER_OFF), "wall lever 69:1 absent");
        actor.moveAndObserve(2D, 0D, 2D, 2);
        return new RedstoneOneTickSetArm(top, piston, head, pushed, lever);
    }

    void idle(RemoteWorldView live, String label) {
        require(live.blockAt(piston.x(), piston.y(), piston.z()).equals(PISTON)
                && live.blockAt(head.x(), head.y(), head.z()).equals(COBBLE)
                && live.blockAt(pushed.x(), pushed.y(), pushed.z()).legacyId() == 0
                && live.blockAt(lever.x(), lever.y(), lever.z()).equals(LEVER_OFF), label);
    }

    RemoteWorldView pulse(B173WireClient actor) {
        actor.selectHeldSlot(4);
        actor.activateBlock(lever, BlockFace.UP);
        RemoteWorldView on = actor.awaitBlock(lever, LEVER_ON);
        actor.activateBlock(lever, BlockFace.UP);
        worldline.test.WorldlineSmokeAwait.awaitBlock(actor, lever, LEVER_OFF, 8);
        worldline.test.WorldlineSmokeAwait.awaitBlock(actor, pushed, COBBLE, 40);
        RemoteWorldView live = worldline.test.WorldlineSmokeAwait.awaitBlock(actor, piston, PISTON, 20);
        require(on.blockAt(lever.x(), lever.y(), lever.z()).equals(LEVER_ON)
                && live.blockAt(piston.x(), piston.y(), piston.z()).equals(PISTON)
                && live.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(COBBLE)
                && !live.blockAt(head.x(), head.y(), head.z()).equals(COBBLE)
                && live.blockAt(lever.x(), lever.y(), lever.z()).equals(LEVER_OFF),
                "one-tick piston pulse absent on=" + cell(on, piston) + "/" + cell(on, lever)
                        + " live=" + cell(live, piston) + "/" + cell(live, head) + "/" + cell(live, pushed)
                        + "/" + cell(live, lever));
        return live;
    }

    void persist(RemoteChunkSnapshot after, int cx, int cz) {
        require(at(after, piston, cx, cz).equals(PISTON) && at(after, pushed, cx, cz).equals(COBBLE)
                && !at(after, head, cx, cz).equals(COBBLE) && at(after, lever, cx, cz).equals(LEVER_OFF),
                "persisted one-tick drop drift");
    }

    String cells(RemoteWorldView live) {
        BlockState headState = live.blockAt(head.x(), head.y(), head.z());
        return "piston=" + cell(piston) + ":29:4,head=" + cell(head) + ":4:0->" + headState.legacyId()
                + ":" + headState.metadata() + ",pushed=" + cell(pushed) + ":0:0->4:0,lever=" + cell(lever)
                + ":69:1->9->1";
    }

    static String cell(RemoteWorldView live, BlockPosition position) {
        return String.valueOf(live.blockAt(position.x(), position.y(), position.z()));
    }

    static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz,
            int[] column) throws Exception {
        BlockPosition top = foundation(initial, cx, cz);
        column[0] = 0;
        actor.selectHeldSlot(0);
        while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column[0] <= 15, "water column exceeded one-tick fixture");
        }
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 2D, 1);
        column[0]++;
        return top;
    }

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic one-tick foundation");
    }

    static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
        return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
    }

    static boolean air(RemoteChunkSnapshot chunk, int cx, int cz, BlockPosition... cells) {
        for (BlockPosition cell : cells) if (at(chunk, cell, cx, cz).legacyId() != 0) return false;
        return true;
    }

    static boolean water(int id) { return id == 8 || id == 9; }

    static String cell(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z();
    }

    static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte item : digest) text.append(String.format("%02x", item & 255));
        return text.toString();
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
