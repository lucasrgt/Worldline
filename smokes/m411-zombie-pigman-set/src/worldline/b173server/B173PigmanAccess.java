package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Nether pigman server profile, DIM-1 EntityId rewrite, Packet7 kill, and death peek. */
public final class B173PigmanAccess {
    private B173PigmanAccess() {}

    public static B173DedicatedServer server(Path jar, Path directory, int port, long seed, Duration timeout) {
        return B173DedicatedServer.netherMonsters(jar, directory, port, seed, timeout);
    }

    public static void retarget(Path directory, BlockPosition spawner) {
        B173SpawnerSeed.nether(directory, spawner, "PigZombie");
    }

    public static RemoteMobDeath peekDeath(B173WireClient client, int entity) {
        return B173ShearsAccess.peekDeath(client, entity);
    }

    public static void kill(B173WireClient actor, RemoteMobSpawn spawn) {
        int entity = spawn.entityId();
        close(actor, spawn.x(), spawn.y(), spawn.z());
        for (int hit = 0; hit < 4; hit++) {
            if (peekDeath(actor, entity) != null) break;
            strike(actor, entity);
        }
        for (int hit = 0; hit < 8 && peekDeath(actor, entity) == null; hit++) {
            RemoteMobMovement move = actor.awaitMobMovement(entity);
            close(actor, move.toX(), move.toY(), move.toZ());
            strike(actor, entity);
        }
        RemoteMobDeath death = actor.awaitMobDeath(entity);
        if (death.entityId() != entity || !death.hurtObserved())
            throw new IllegalStateException("pigman death drift");
    }

    private static void close(B173WireClient actor, double x, double y, double z) {
        PlayerPose here = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        actor.moveAndObserve(x - here.x(), y + 1.0D - here.y(), z - here.z(), 2);
    }

    private static void strike(B173WireClient actor, int entity) {
        int health = actor.health();
        if (health == 0) throw new IllegalStateException("actor died during pigman set");
        if (health >= 1 && health <= 16) {
            int food = find(actor.inventory(), 297);
            if (food >= 36) { actor.selectHeldSlot(food - 36); actor.useSelectedItemInAir(); actor.sustainTicks(5); }
        }
        int sword = find(actor.inventory(), 276);
        if (sword < 36) throw new IllegalStateException("diamond sword lost");
        actor.selectHeldSlot(sword - 36);
        actor.attackMob(entity);
        actor.sustainTicks(20);
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }
}
