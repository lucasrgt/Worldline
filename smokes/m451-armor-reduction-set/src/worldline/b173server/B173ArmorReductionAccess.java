package worldline.b173server;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Raised-platform zombie fixture plus one Packet8 melee absorb and food heal. */
public final class B173ArmorReductionAccess {
    public static final int[] LEATHER = {298, 299, 300, 301};
    public static final int[] IRON = {306, 307, 308, 309};
    public static final int[] DIAMOND = {310, 311, 312, 313};
    private B173ArmorReductionAccess() {}

    public static final class Pad {
        public final BlockPosition top, spawner; public final int column;
        Pad(BlockPosition top, BlockPosition spawner, int column) {
            this.top = top; this.spawner = spawner; this.column = column; }
    }

    public static Pad raise(B173WireClient actor, int cx, int cz) throws Exception {
        RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
        BlockPosition top = foundation(initial, cx, cz); int column = 0;
        actor.selectHeldSlot(0);
        while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1); actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded armor-reduction fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = place(actor, top, BlockFace.UP, 1); actor.moveAndObserve(0D, 1D, 0D, 1); column++;
        }
        actor.selectHeldSlot(1);
        for (int r = 1; r <= 3; r++) {
            for (int z = -r + 1; z < r; z++) {
                grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
                grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
            }
            for (int x = -r + 1; x < r; x++) {
                grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
                grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
            }
            grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
            grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
            grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
            grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        }
        actor.selectHeldSlot(0);
        for (int d = -3; d <= 3; d++) {
            wall(actor, new BlockPosition(top.x() - 3, top.y(), top.z() + d));
            wall(actor, new BlockPosition(top.x() + 3, top.y(), top.z() + d));
            wall(actor, new BlockPosition(top.x() + d, top.y(), top.z() - 3));
            wall(actor, new BlockPosition(top.x() + d, top.y(), top.z() + 3));
        }
        actor.selectHeldSlot(2); BlockPosition spawner = place(actor, top, BlockFace.UP, 52);
        actor.sustainTicks(5); return new Pad(top, spawner, column);
    }

    public static RemoteMobSpawn near(B173WireClient actor, BlockPosition spawner) {
        for (int n = 0; n < 32; n++) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(54);
            double dx = spawn.x() - (spawner.x() + 0.5D), dz = spawn.z() - (spawner.z() + 0.5D);
            if (dx * dx + dz * dz <= 12.25D && Math.abs(spawn.y() - spawner.y()) <= 2D
                    && spawn.legacyType() == 54 && spawn.entityId() != actor.state().entityId())
                return spawn;
        }
        throw new IllegalStateException("nearby zombie type 54 absent");
    }

    public static RemoteIncomingHit absorb(B173WireClient actor, int entity, double[] at, boolean poke) {
        int min = 1;
        if (actor.health() < 20) heal(actor);
        approach(actor, at[0], at[1] + 1.0D, at[2] - 1.5D, 2.5D);
        if (poke) poke(actor, entity);
        for (int attempt = 0; attempt < 8; attempt++) {
            if (actor.health() < 20) heal(actor);
            if (actor.health() != 20) continue;
            for (int n = 0; n < 80 && actor.health() >= 20; n++) {
                RemoteMobMovement step = actor.channel().inbound().mobs().takeMovement(entity);
                if (step != null) {
                    at[0] = step.toX(); at[1] = step.toY(); at[2] = step.toZ();
                    approach(actor, at[0], at[1] + 1.0D, at[2] - 1.5D, 1.2D);
                } else actor.sustainTicks(1);
            }
            int after = actor.health();
            if (after <= 0 || after >= 20) continue;
            RemoteIncomingHit hit = actor.awaitIncomingHit(after);
            if (hit.healthBefore() == 20 && hit.damage() >= min) return hit;
            heal(actor);
        }
        throw new IllegalStateException("zombie Packet8 melee absent health=" + actor.health());
    }

    private static void poke(B173WireClient actor, int entity) {
        int sword = hotbar(actor, 276); require(sword >= 36, "diamond sword 276 absent");
        actor.selectHeldSlot(sword - 36); actor.attackMob(entity); actor.sustainTicks(5);
    }

    public static void heal(B173WireClient actor) {
        if (actor.health() == 0) throw new IllegalStateException("actor died during armor reduction");
        for (int n = 0; n < 6 && actor.health() < 20; n++) {
            int food = hotbar(actor, 322); if (food < 36) food = hotbar(actor, 320);
            require(food >= 36, "heal food 322/320 absent");
            actor.selectHeldSlot(food - 36); actor.useSelectedItemInAir(); actor.sustainTicks(4);
        }
        require(actor.awaitHealth(20) == 20, "heal failed health=" + actor.health());
    }

    public static void settle(B173WireClient actor) { actor.sustainTicks(10); }

    public static void go(B173WireClient actor, BlockPosition p) {
        approach(actor, p.x() + 0.5D, p.y() + 0.0D, p.z() - 1.5D, 0.8D);
    }

    public static void retreat(B173WireClient actor, BlockPosition top) {
        approach(actor, top.x() + 0.5D, top.y() + 1.0D, top.z() + 2.5D, 1.2D);
    }

    public static String cell(BlockPosition p) { return p.x() + ":" + p.y() + ":" + p.z() + ":52:0"; }

    private static void approach(B173WireClient actor, double x, double y, double z, double reach) {
        for (int step = 0; step < 16; step++) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= reach) return;
            double scale = Math.min(1D, 9.0D / dist);
            actor.moveAndObserve(dx * scale, dy * scale, dz * scale, 2);
        }
    }

    private static int hotbar(B173WireClient actor, int id) {
        RemoteInventoryView view = actor.inventory();
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face); actor.awaitBlock(target, new BlockState(id, 0)); return target;
    }

    private static void grass(B173WireClient actor, BlockPosition support, BlockFace face) throws Exception {
        place(actor, support, face, 2);
    }

    private static void wall(B173WireClient actor, BlockPosition floor) throws Exception {
        place(actor, floor, BlockFace.UP, 1);
    }

    private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic armor-reduction foundation");
    }

    private static boolean water(int id) { return id == 8 || id == 9; }
    private static int local(int v, int c) { return v - c * 16; }
    private static void require(boolean v, String m) { if (!v) throw new IllegalStateException(m); }
}
