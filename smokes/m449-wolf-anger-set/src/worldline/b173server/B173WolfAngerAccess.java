package worldline.b173server;

import java.nio.file.Path;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Smoke-local Packet7 nonlethal wood-sword attack and Packet38 tame-absent wait. */
public final class B173WolfAngerAccess {
    public static final int TYPE = 95, SWORD = 268, BONE = 352;

    private B173WolfAngerAccess() {}

    public static void retarget(Path directory, BlockPosition spawner) {
        B173SpawnerSeed.wolf(directory, spawner);
    }

    public static RemoteMobSpawn anger(B173WireClient client, BlockPosition spawner, BlockPosition top) {
        if (count(client.inventory(), BONE) > 0)
            throw new IllegalStateException("bone 352 present; wolf-anger-set must not feed bone");
        StringBuilder why = new StringBuilder();
        for (int n = 0; n < 16; n++) {
            RemoteMobSpawn spawn = near(client, spawner, top);
            String result = strikeAndWait(client, spawn);
            if (result == null) return spawn;
            why.append(" n").append(n).append("=e").append(spawn.entityId()).append(':').append(result);
        }
        throw new IllegalStateException("wolf Packet8 hostility absent after bounded Packet7 strikes" + why);
    }

    public static int tame(B173WireClient client, int entity) {
        return client.channel().inbound().mobs().takeTame(entity);
    }

    private static RemoteMobSpawn near(B173WireClient client, BlockPosition spawner, BlockPosition top) {
        for (int n = 0; n < 32; n++) {
            RemoteMobSpawn spawn = client.awaitMobSpawn(TYPE);
            if (spawn.legacyType() != TYPE || spawn.entityId() == client.state().entityId())
                throw new IllegalStateException("wolf Packet24 type 95 identity drift");
            double dx = spawn.x() - (top.x() + 0.5D), dz = spawn.z() - (top.z() + 0.5D);
            if (Math.abs(dx) <= 2.5D && Math.abs(dz) <= 2.5D
                    && Math.abs(spawn.y() - spawner.y()) <= 2D) return spawn;
        }
        throw new IllegalStateException("nearby wolf type 95 absent");
    }

    private static String strikeAndWait(B173WireClient client, RemoteMobSpawn spawn) {
        int entity = spawn.entityId();
        double x = spawn.x(), y = spawn.y(), z = spawn.z();
        close(client, x, y + 1.0D, z - 1.5D, 2.5D);
        int before = client.health();
        if (before < 1 || before > 20)
            throw new IllegalStateException("actor health absent before wolf hostility");
        strike(client, entity);
        if (tame(client, entity) >= 0)
            throw new IllegalStateException("wolf Packet38 status 6/7 after Packet7 without bone");
        if (dead(client, entity)) return "dead-after-strike";
        if (client.health() < before && client.health() > 0) return null;
        for (int n = 0; n < 16; n++) {
            RemoteMobMovement move = client.channel().inbound().mobs().takeMovement(entity);
            if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
            if (dead(client, entity)) return "dead-wait-h" + client.health();
            close(client, x, y + 1.0D, z - 1.2D, 1.8D);
            client.sustainTicks(10);
            if (tame(client, entity) >= 0)
                throw new IllegalStateException("wolf Packet38 status 6/7 during hostility wait");
            if (dead(client, entity)) return "dead-wait-h" + client.health();
            if (client.health() < before && client.health() > 0) return null;
        }
        return "no-packet8-h" + client.health();
    }

    private static void strike(B173WireClient client, int entity) {
        int sword = find(client.inventory(), SWORD);
        if (sword < 36) throw new IllegalStateException("wood sword 268 absent from hotbar");
        client.selectHeldSlot(sword - 36);
        client.attackMob(entity);
        client.sustainTicks(2);
    }

    private static boolean dead(B173WireClient client, int entity) {
        return B173ShearsAccess.peekDeath(client, entity) != null;
    }

    private static void close(B173WireClient client, double x, double y, double z, double range) {
        for (int n = 0; n < 16; n++) {
            PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= range) return;
            double s = Math.min(1D, 9.0D / dist);
            client.moveAndObserve(dx * s, dy * s, dz * s, 2);
        }
        throw new IllegalStateException("movement cap missed arena-contained wolf");
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id) return slot;
        return -1;
    }

    private static int count(RemoteInventoryView view, int id) {
        int total = 0;
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
                total += view.slot(slot).item().count();
        return total;
    }
}
