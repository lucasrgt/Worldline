package worldline.smoke.redstoneoreglowsetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** Raised-stone east-floor redstone ore glow fixture and waits. */
public final class RedstoneOreGlowSetArm {
    static final BlockState UNLIT = new BlockState(73, 0);
    static final BlockState GLOW = new BlockState(74, 0);

    private RedstoneOreGlowSetArm() {}

    static BlockPosition raise(B173WireClient actor, RemoteChunkSnapshot initial, int cx,
            int cz, int[] column) throws Exception {
        BlockPosition top = foundation(initial, cx, cz);
        column[0] = 0;
        actor.selectHeldSlot(0);
        while (water(at(initial, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column[0] <= 15, "water column exceeded redstone-ore-glow fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column[0]++;
        }
        return top;
    }

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face,
            int id) throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++)
            for (int z = 4; z <= 11; z++)
                for (int y = 126; y >= 1; y--)
                    if (chunk.blockAt(x, y, z).legacyId() == 3
                            && water(chunk.blockAt(x, y + 1, z).legacyId()))
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic redstone-ore-glow foundation");
    }

    static void awaitOre(B173WireClient actor, BlockPosition ore, BlockState want, int polls,
            String label) {
        RemoteWorldView world = worldline.test.WorldlineSmokeAwait.awaitWorld(actor,
                view -> want.equals(view.blockAt(ore.x(), ore.y(), ore.z())), label, polls);
        BlockState live = world.blockAt(ore.x(), ore.y(), ore.z());
        require(want.equals(live), label + " drift: " + live);
    }

    static void stepOn(B173WireClient actor, BlockPosition ore) {
        int[] step = new int[1];
        worldline.test.WorldlineSmokeAwait.awaitCheckedEntity(actor, () -> {
            actor.moveAndObserve(0D, 1.25D, 0D, 1);
            actor.moveAndObserve(0D, -1.25D, 0D, 1);
            actor.moveAndObserve((step[0]++ % 2 == 0) ? 0.35D : -0.35D, 0D, 0D, 2);
            return worldline.test.WorldlineSmokeAwait.observe(actor, 1)
                    .blockAt(ore.x(), ore.y(), ore.z());
        }, GLOW::equals, "step glow 73:0->74:0", 80);
    }

    static PlayerPose walk(B173WireClient actor, PlayerPose pose, double x, double y,
            double z) {
        PlayerPose here = pose;
        for (int step = 0; step < 32; step++) {
            here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 0.4D) return here;
            double scale = Math.min(1D, 0.8D / dist);
            here = actor.moveAndObserve(dx * scale, dy * scale, dz * scale, 4).resulting();
        }
        throw new IllegalStateException("walk pose drift x=" + here.x() + " y=" + here.y()
                + " z=" + here.z());
    }

    static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position, int cx, int cz) {
        return chunk.blockAt(position.x() - cx * 16, position.y(), position.z() - cz * 16);
    }

    static boolean water(int id) { return id == 8 || id == 9; }

    static String cell(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z();
    }

    static void awaitPlayers(B173DedicatedServer server, int count) {
        new worldline.test.WorldlineAwait(50).awaitEntity(server::players,
                players -> players.size() == count, "player count");
    }

    static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte part : digest) text.append(String.format("%02x", part & 255));
        return text.toString();
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
