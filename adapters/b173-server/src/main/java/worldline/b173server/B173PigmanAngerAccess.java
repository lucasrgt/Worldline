package worldline.b173server;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Reusable Nether pigman anger fixture and Packet7/38 pursuit boundary. */
public final class B173PigmanAngerAccess {
    private B173PigmanAngerAccess() {}

    public static B173DedicatedServer server(Path jar, Path directory, int port, long seed, Duration timeout) {
        return B173DedicatedServer.netherMonsters(jar, directory, port, seed, timeout);
    }

    public static void retarget(Path directory, BlockPosition spawner) {
        B173SpawnerSeed.nether(directory, spawner, "PigZombie", false);
        B173SpawnerSeed.nether(directory, spawner, "PigZombie", true);
    }

    public static RemoteMobSpawn near(B173WireClient actor, int type, BlockPosition p) {
        for (int n = 0; n < 32; n++) {
            RemoteMobSpawn spawn = actor.awaitMobSpawn(type);
            double dx = spawn.x() - (p.x() + 0.5D), dz = spawn.z() - (p.z() + 0.5D);
            if (dx * dx + dz * dz <= 100D && Math.abs(spawn.y() - p.y()) <= 6D) return spawn;
        }
        throw new IllegalStateException("nearby type " + type + " absent");
    }

    public static void pad(B173WireClient actor, BlockPosition center) throws Exception {
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
        for (int i = 0; i < faces.length; i++) {
            BlockPosition next = faces[i].adjacent(center);
            if (air(actor, next)) place(actor, center, faces[i], 87);
            BlockPosition far = faces[i].adjacent(next);
            if (air(actor, far)) place(actor, next, faces[i], 87);
        }
    }

    public static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    public static BlockPosition second(B173WireClient actor, BlockPosition first) throws Exception {
        BlockFace[] faces = { BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH };
        for (int i = 0; i < faces.length; i++) {
            if (air(actor, faces[i].adjacent(first))) return place(actor, first, faces[i], 52);
        }
        throw new IllegalStateException("no adjacent air for second pigman spawner");
    }

    public static void provoke(B173WireClient actor, RemoteMobSpawn struck, RemoteMobSpawn neighbor) {
        int hit = struck.entityId(), other = neighbor.entityId();
        close(actor, struck.x(), struck.y(), struck.z());
        strike(actor, hit);
        if (!awaitHurt(actor, hit)) throw new IllegalStateException("struck pigman Packet38 status 2 absent");
        if (peekDeath(actor, hit) != null || peekDeath(actor, other) != null)
            throw new IllegalStateException("pigman death after single Packet7");
        aggro(actor, other);
        if (peekDeath(actor, hit) != null || peekDeath(actor, other) != null)
            throw new IllegalStateException("pigman death during anger");
    }

    public static RemoteMobDeath peekDeath(B173WireClient client, int entity) {
        return client.channel().inbound().mobs().peekDeath(entity);
    }

    private static void strike(B173WireClient actor, int entity) {
        int health = actor.health();
        if (health == 0) throw new IllegalStateException("actor died during pigman anger");
        int sword = find(actor.inventory(), 276);
        if (sword < 36) throw new IllegalStateException("diamond sword lost");
        actor.selectHeldSlot(sword - 36);
        actor.attackMob(entity);
        actor.sustainTicks(10);
    }

    private static void aggro(B173WireClient actor, int other) {
        PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        for (int n = 0; n < 32; n++) {
            if (actor.health() == 0) throw new IllegalStateException("actor died during pigman anger");
            if (actor.health() < 20) return;
            RemoteMobMovement move = actor.awaitMobMovement(other);
            double from = dist(move.fromX(), move.fromZ(), here.x(), here.z());
            double to = dist(move.toX(), move.toZ(), here.x(), here.z());
            if (to + 0.15D < from || actor.health() < 20) return;
            actor.sustainTicks(5);
            here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        }
        throw new IllegalStateException("nearby pigman did not pursue or hurt the actor");
    }

    private static void close(B173WireClient actor, double x, double y, double z) {
        for (int n = 0; n < 8; n++) {
            PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y + 1.0D - here.y(), dz = z - here.z();
            double range = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (range <= 2.5D) return;
            double s = Math.min(1D, 4D / range);
            actor.moveAndObserve(dx * s, dy * s, dz * s, 2);
        }
    }

    private static boolean awaitHurt(B173WireClient client, int entity) {
        B173PlayInbound inbound = client.channel().inbound();
        Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                try {
                    if (inbound.mobs().takeHurt(entity)) return true;
                    inbound.pumpOne();
                    if (inbound.mobs().takeHurt(entity)) return true;
                } catch (IOException error) {
                    throw new IllegalStateException("pigman Packet38 status 2 absent before deadline", error);
                }
            }
            throw new IllegalStateException("pigman Packet38 status 2 absent before deadline");
        } finally { pulse.interrupt(); }
    }

    private static boolean air(B173WireClient actor, BlockPosition p) {
        return actor.awaitRemoteChunk(p.x() >> 4, p.z() >> 4).blockAt(p.x(), p.y(), p.z()).legacyId() == 0;
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }

    private static double dist(double x, double z, double ox, double oz) {
        double dx = x - ox, dz = z - oz;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
