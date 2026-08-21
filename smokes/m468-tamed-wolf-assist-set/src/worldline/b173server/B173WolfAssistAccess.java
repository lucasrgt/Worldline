package worldline.b173server;

import java.io.IOException;
import java.nio.file.Path;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Smoke-local Packet7 bone tame, owner unsit, one sword 276 strike, and assist wait. */
public final class B173WolfAssistAccess {
    public static final int BONE = 352, SWORD = 276;

    private B173WolfAssistAccess() {}

    public static void retarget(Path directory, BlockPosition sheep, BlockPosition wolf) {
        B173SpawnerSeed.pigAndSheep(directory, sheep);
        B173SpawnerSeed.wolf(directory, wolf);
    }

    public static int tame(B173WireClient client, RemoteMobSpawn spawn) {
        int entity = spawn.entityId();
        double x = spawn.x(), y = spawn.y(), z = spawn.z();
        for (int n = 0; n < 32; n++) {
            RemoteMobMovement move = client.channel().inbound().mobs().takeMovement(entity);
            if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
            close(client, x, y, z);
            int slot = find(client.inventory(), BONE, -1);
            if (slot < 0) throw new IllegalStateException("bone 352 exhausted before Packet38 status 7");
            client.selectHeldSlot(slot);
            use(client, entity, BONE, 0);
            int status = awaitTame(client, entity);
            if (status == 7) return status;
            if (status > 0 && status != 6) throw new IllegalStateException("wolf Packet38 tame status drift " + status);
        }
        throw new IllegalStateException("wolf Packet38 status 7 absent after 32 bones");
    }

    public static void discardPending(B173WireClient client, int type) {
        while (client.channel().inbound().mobs().take(type) != null) {}
    }

    public static void unsit(B173WireClient client, int entity) {
        int slot = find(client.inventory(), SWORD, -1);
        if (slot < 0) throw new IllegalStateException("diamond sword 276 absent before unsit");
        client.selectHeldSlot(slot);
        use(client, entity, SWORD, 0);
        client.sustainTicks(10);
    }

    public static RemoteMobDeath strikeOnceAndAssist(B173WireClient client, RemoteMobSpawn spawn) {
        int entity = spawn.entityId();
        double x = spawn.x(), y = spawn.y(), z = spawn.z();
        int slot = find(client.inventory(), SWORD, -1);
        if (slot < 0) throw new IllegalStateException("diamond sword 276 absent before Packet7");
        int hurts = 0;
        RemoteMobDeath early = null;
        for (int swing = 0; swing < 4 && hurts < 1 && early == null; swing++) {
            RemoteMobMovement move = client.channel().inbound().mobs().takeMovement(entity);
            if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
            close(client, x, y, z);
            client.moveAndObserve(0D, 0D, 0D, 2);
            client.selectHeldSlot(slot);
            use(client, entity, SWORD, 1);
            for (int wait = 0; wait < 10; wait++) {
                client.sustainTicks(1);
                hurts = client.channel().inbound().mobs().hurtCount(entity);
                early = B173ShearsAccess.peekDeath(client, entity);
                if (hurts >= 1 || early != null) break;
            }
        }
        if (early != null) {
            if (hurts >= 2) return early;
            throw new IllegalStateException("target died from the single player Packet7");
        }
        if (hurts < 1) throw new IllegalStateException("target Packet38 status 2 absent after player Packet7");
        for (int n = 0; n < 32; n++) {
            RemoteMobMovement move = client.channel().inbound().mobs().takeMovement(entity);
            if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
            close(client, x, y, z);
            RemoteMobDeath death = B173ShearsAccess.peekDeath(client, entity);
            if (death != null) return death;
            client.sustainTicks(10);
            death = B173ShearsAccess.peekDeath(client, entity);
            if (death != null) return death;
        }
        throw new IllegalStateException("tamed wolf assist death absent after one Packet7");
    }

    private static void use(B173WireClient client, int entity, int item, int button) {
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId(), slot = find(inbound.inventory(), item, -1);
            if (entity < 0 || entity == local) throw new IllegalArgumentException("invalid wolf-assist target");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("wolf assist requires synchronized play");
            if (slot < 0) throw new IllegalStateException("item " + item + " absent from hotbar");
            if (button != 0 && button != 1) throw new IllegalArgumentException("invalid Packet7 button");
            synchronized (channel.output) {
                channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entity); channel.output.writeByte(button); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("wolf-assist Packet7 failed", error); }
    }

    private static int awaitTame(B173WireClient client, int entity) {
        for (int wait = 0; wait < 20; wait++) {
            client.sustainTicks(1);
            int value = client.channel().inbound().mobs().takeTame(entity);
            if (value >= 0) return value;
        }
        return -1;
    }

    private static void close(B173WireClient client, double x, double y, double z) {
        for (int n = 0; n < 9; n++) {
            PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 2.5D) return;
            double s = Math.min(1D, 9D / dist);
            client.moveAndObserve(dx * s, dy * s, dz * s, 4);
        }
    }

    private static int find(RemoteInventoryView view, int id, int damage) {
        for (int slot = 0; slot <= 8; slot++)
            if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == id
                    && (damage < 0 || view.slot(36 + slot).item().damage() == damage))
                return slot;
        return -1;
    }
}
