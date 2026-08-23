package worldline.b173server;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Raised-platform zombie fixture, Packet8 melee absorb, and worn chestplate slot 6. */
public final class B173ArmorDurabilityAccess {
    public static final int CHESTPLATE = 307;
    public static final int SLOT = 6;

    private B173ArmorDurabilityAccess() {}

    public static final class Pad {
        public final BlockPosition top;
        public final BlockPosition spawner;
        public final int column;

        Pad(BlockPosition top, BlockPosition spawner, int column) {
            this.top = top;
            this.spawner = spawner;
            this.column = column;
        }
    }

    public static Pad raise(B173WireClient actor, int cx, int cz) throws Exception {
        RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
        BlockPosition top = foundation(initial, cx, cz);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded armor-durability fixture");
        }
        int lift = 0;
        while (lift < 8) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column++;
            lift++;
        }
        actor.selectHeldSlot(1);
        int r = 1;
        while (r <= 3) {
            int z = -r + 1;
            while (z < r) {
                grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
                grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
                z++;
            }
            int x = -r + 1;
            while (x < r) {
                grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
                grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
                x++;
            }
            grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
            grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
            grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
            grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
            r++;
        }
        actor.selectHeldSlot(0);
        int d = -3;
        while (d <= 3) {
            wall(actor, new BlockPosition(top.x() - 3, top.y(), top.z() + d));
            wall(actor, new BlockPosition(top.x() + 3, top.y(), top.z() + d));
            wall(actor, new BlockPosition(top.x() + d, top.y(), top.z() - 3));
            wall(actor, new BlockPosition(top.x() + d, top.y(), top.z() + 3));
            d++;
        }
        actor.selectHeldSlot(2);
        BlockPosition spawner = place(actor, top, BlockFace.UP, 52);
        worldline.test.WorldlineSmokeAwait.observe(actor, 5);
        return new Pad(top, spawner, column);
    }

    public static RemoteMobSpawn near(B173WireClient actor, BlockPosition spawner) {
        int n = 0;
        while (n < 32) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(54);
            double dx = spawn.x() - (spawner.x() + 0.5D);
            double dz = spawn.z() - (spawner.z() + 0.5D);
            if (dx * dx + dz * dz <= 12.25D && Math.abs(spawn.y() - spawner.y()) <= 2D
                    && spawn.legacyType() == 54 && spawn.entityId() != actor.state().entityId()) {
                return spawn;
            }
            n++;
        }
        throw new IllegalStateException("nearby zombie type 54 absent");
    }

    public static RemoteIncomingHit absorb(B173WireClient actor, int entity, double[] at) {
        int min = 1;
        if (actor.health() < 20) {
            heal(actor);
        }
        approach(actor, at[0], at[1] + 1.0D, at[2] - 1.5D, 2.5D);
        int attempt = 0;
        while (attempt < 8) {
            if (actor.health() < 20) {
                heal(actor);
            }
            if (actor.health() != 20) {
                attempt++;
                continue;
            }
            int n = 0;
            while (n < 80 && actor.health() >= 20) {
                RemoteMobMovement step = actor.channel().inbound().mobs().takeMovement(entity);
                if (step != null) {
                    at[0] = step.toX();
                    at[1] = step.toY();
                    at[2] = step.toZ();
                    approach(actor, at[0], at[1] + 1.0D, at[2] - 1.5D, 1.2D);
                } else {
                    worldline.test.WorldlineSmokeAwait.observe(actor, 1);
                }
                n++;
            }
            int after = actor.health();
            if (after <= 0 || after >= 20) {
                attempt++;
                continue;
            }
            RemoteIncomingHit hit = actor.awaitIncomingHit(after);
            if (hit.healthBefore() == 20 && hit.damage() >= min) {
                return hit;
            }
            heal(actor);
            attempt++;
        }
        throw new IllegalStateException("zombie Packet8 melee absent health=" + actor.health());
    }

    public static RemoteItemStack worn(B173WireClient actor) {
        RemoteInventoryView view = actor.inventory();
        if (view.slot(SLOT).empty() || view.slot(SLOT).item().legacyId() != CHESTPLATE) {
            throw new IllegalStateException("iron chestplate window slot 6 absent");
        }
        return view.slot(SLOT).item();
    }

    public static void heal(B173WireClient actor) {
        if (actor.health() == 0) {
            throw new IllegalStateException("actor died during armor durability");
        }
        int n = 0;
        while (n < 6 && actor.health() < 20) {
            int food = hotbar(actor, 322);
            if (food < 36) {
                food = hotbar(actor, 320);
            }
            require(food >= 36, "heal food 322/320 absent");
            actor.selectHeldSlot(food - 36);
            actor.useSelectedItemInAir();
            worldline.test.WorldlineSmokeAwait.observe(actor, 4);
            n++;
        }
        require(actor.awaitHealth(20) == 20, "heal failed health=" + actor.health());
    }

    public static void go(B173WireClient actor, BlockPosition p) {
        approach(actor, p.x() + 0.5D, p.y() + 0.0D, p.z() - 1.5D, 0.8D);
    }

    public static String cell(BlockPosition p) {
        return p.x() + ":" + p.y() + ":" + p.z() + ":52:0";
    }

    private static void approach(B173WireClient actor, double x, double y, double z, double reach) {
        int step = 0;
        while (step < 16) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x();
            double dy = y - here.y();
            double dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= reach) {
                return;
            }
            double scale = Math.min(1D, 9.0D / dist);
            actor.moveAndObserve(dx * scale, dy * scale, dz * scale, 2);
            step++;
        }
        throw new IllegalStateException("movement cap missed armor-durability target");
    }

    private static int hotbar(B173WireClient actor, int id) {
        RemoteInventoryView view = actor.inventory();
        int slot = 36;
        while (slot <= 44) {
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) {
                return slot;
            }
            slot++;
        }
        return -1;
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static void grass(B173WireClient actor, BlockPosition support, BlockFace face) throws Exception {
        place(actor, support, face, 2);
    }

    private static void wall(B173WireClient actor, BlockPosition floor) throws Exception {
        place(actor, floor, BlockFace.UP, 1);
    }

    private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic armor-durability foundation");
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int v, int c) {
        return v - c * 16;
    }

    private static void require(boolean v, String m) {
        if (!v) {
            throw new IllegalStateException(m);
        }
    }
}
