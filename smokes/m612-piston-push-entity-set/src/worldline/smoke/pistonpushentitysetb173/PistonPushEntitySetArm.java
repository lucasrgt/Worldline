package worldline.smoke.pistonpushentitysetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** West-facing piston 33 with air head, floors under the item path, and a side lever. */
public final class PistonPushEntitySetArm {
    final BlockPosition support, piston, head, pushed, floor, landing, stand, lever;
    PlayerPose pose;

    private PistonPushEntitySetArm(BlockPosition support, BlockPosition piston, BlockPosition head,
            BlockPosition pushed, BlockPosition floor, BlockPosition landing, BlockPosition stand,
            BlockPosition lever, PlayerPose pose) {
        this.support = support; this.piston = piston; this.head = head; this.pushed = pushed;
        this.floor = floor; this.landing = landing; this.stand = stand; this.lever = lever;
        this.pose = pose;
    }

    static PistonPushEntitySetArm place(B173WireClient actor, RemoteChunkSnapshot initial,
            int cx, int cz, int[] column) throws Exception {
        BlockPosition support = raise(actor, initial, cx, cz, column);
        PlayerPose pose = lastPose(actor);
        BlockPosition floor = place(actor, support, BlockFace.WEST, 1);
        BlockPosition landing = place(actor, floor, BlockFace.WEST, 1);
        place(actor, landing, BlockFace.WEST, 1);
        BlockPosition stand = place(actor, place(actor, support, BlockFace.SOUTH, 1), BlockFace.SOUTH, 1);
        BlockPosition piston = BlockFace.UP.adjacent(support);
        BlockPosition head = BlockFace.WEST.adjacent(piston);
        BlockPosition pushed = BlockFace.WEST.adjacent(head);
        BlockPosition lever = BlockFace.EAST.adjacent(support);
        require(at(initial, piston, cx, cz).legacyId() == 0
                && at(initial, head, cx, cz).legacyId() == 0
                && at(initial, pushed, cx, cz).legacyId() == 0
                && at(initial, lever, cx, cz).legacyId() == 0,
                "piston-push-entity targets were not initial air");
        return new PistonPushEntitySetArm(support, piston, head, pushed, floor, landing, stand, lever, pose);
    }

    RemoteDroppedItem dropCobble(B173WireClient actor) throws Exception {
        pose = walk(actor, pose, head.x() + 0.5D, support.y() + 1.0D, head.z() + 0.5D);
        actor.look(0F, 90F);
        actor.selectHeldSlot(3);
        actor.dropHeldItem();
        RemoteDroppedItem spawn = actor.awaitDroppedItem(PistonPushEntitySetSmoke.COBBLE);
        require(spawn.item().equals(PistonPushEntitySetSmoke.COBBLE)
                && spawn.item().count() == 1
                && spawn.x() >= head.x() && spawn.x() < head.x() + 1
                && spawn.z() >= head.z() && spawn.z() < head.z() + 1,
                "cobble Packet21 was not dropped in the air head cell");
        pose = walk(actor, pose, stand.x() + 0.5D, support.y() + 1.0D, stand.z() + 0.5D);
        return spawn;
    }

    void installPiston(B173WireClient actor) throws Exception {
        actor.look(-90F, 0F);
        actor.selectHeldSlot(1);
        actor.placeHeldBlock(support, BlockFace.UP);
        BlockState placed = worldline.test.WorldlineSmokeAwait.awaitBlock(
                actor, piston, new BlockState(33, 4), 5)
                .blockAt(piston.x(), piston.y(), piston.z());
        require(placed.equals(new BlockState(33, 4)),
                "west piston 33 absent: " + placed + " at " + cell(piston));
        actor.selectHeldSlot(2);
        actor.placeHeldBlock(support, BlockFace.EAST);
        require(worldline.test.WorldlineSmokeAwait.awaitBlock(actor, lever, new BlockState(69, 1), 5)
                .blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 1)),
                "lever absent for piston-push-entity");
        actor.selectHeldSlot(4);
    }

    RemoteWorldView extend(B173WireClient actor, int ticks) throws Exception {
        actor.activateBlock(lever, BlockFace.UP);
        RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, ticks);
        require(live.blockAt(lever.x(), lever.y(), lever.z()).equals(new BlockState(69, 9))
                && live.blockAt(piston.x(), piston.y(), piston.z()).equals(new BlockState(33, 12))
                && live.blockAt(head.x(), head.y(), head.z()).equals(new BlockState(34, 4))
                && live.blockAt(pushed.x(), pushed.y(), pushed.z()).equals(new BlockState(0, 0)),
                "piston 33 entity-push extend absent: "
                + live.blockAt(piston.x(), piston.y(), piston.z()) + "/"
                + live.blockAt(head.x(), head.y(), head.z()));
        return live;
    }

    void persist(RemoteChunkSnapshot after, int cx, int cz) {
        require(at(after, piston, cx, cz).equals(new BlockState(33, 12))
                && at(after, head, cx, cz).equals(new BlockState(34, 4))
                && at(after, pushed, cx, cz).equals(new BlockState(0, 0))
                && at(after, floor, cx, cz).equals(new BlockState(1, 0))
                && at(after, landing, cx, cz).equals(new BlockState(1, 0))
                && at(after, stand, cx, cz).equals(new BlockState(1, 0)),
                "fresh piston-push-entity persist drift");
    }

    static void displaced(RemoteDroppedItem spawn, RemoteDroppedItem after) {
        require(after.item().equals(PistonPushEntitySetSmoke.COBBLE)
                && after.item().count() == 1
                && after.x() <= spawn.x() - 0.5D
                && after.x() < spawn.x()
                && after.x() < Math.floor(spawn.x()),
                "item-entity coordinates did not move west: " + fixed(spawn) + "->" + fixed(after));
    }

    static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx, int cz,
            int[] column) throws Exception {
        BlockPosition top = foundation(initial, cx, cz);
        column[0] = 0;
        actor.selectHeldSlot(0);
        while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column[0] <= 15, "water column exceeded piston-push-entity fixture");
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

    static PlayerPose walk(B173WireClient actor, PlayerPose pose, double x, double y, double z) {
        for (int step = 0; step < 24; step++) {
            double dx = clamp(x - pose.x()), dy = clamp(y - pose.y()), dz = clamp(z - pose.z());
            if (dx * dx + dy * dy + dz * dz < 0.04D) return pose;
            MovementOutcome move = actor.moveAndObserve(dx, dy, dz, 2);
            pose = move.resulting();
        }
        require((x - pose.x()) * (x - pose.x()) + (z - pose.z()) * (z - pose.z()) < 0.36D,
                "walk to " + x + ":" + y + ":" + z + " failed at " + pose.x() + ":" + pose.y()
                + ":" + pose.z());
        return pose;
    }

    static PlayerPose lastPose(B173WireClient actor) {
        return actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic piston-push-entity foundation");
    }

    static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
        return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
    }

    static boolean water(int id) { return id == 8 || id == 9; }
    static double clamp(double value) { return Math.max(-1D, Math.min(1D, value)); }
    static String cell(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z();
    }
    static String fixed(RemoteDroppedItem item) {
        return Math.round(item.x() * 32D) + ":" + Math.round(item.y() * 32D) + ":"
                + Math.round(item.z() * 32D);
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
