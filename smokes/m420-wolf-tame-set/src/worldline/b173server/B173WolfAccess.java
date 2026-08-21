package worldline.b173server;

import java.io.IOException;
import java.nio.file.Path;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Smoke-local Packet7 bone/dye interact and Packet38 status 6/7 tame wait. */
public final class B173WolfAccess {
    public static final int TYPE = 95, BONE = 352, DYE = 351;

    private B173WolfAccess() {}

    public static void retarget(Path directory, BlockPosition spawner) {
        B173SpawnerSeed.wolf(directory, spawner);
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
            use(client, entity, BONE);
            int status = awaitTame(client, entity);
            if (status == 7) return status;
            if (status != 6) throw new IllegalStateException("wolf Packet38 tame status drift " + status);
        }
        throw new IllegalStateException("wolf Packet38 status 7 absent after 32 bones");
    }

    public static boolean dyeCollar(B173WireClient client, int entity) {
        int before = count(client.inventory(), DYE, 4);
        if (before < 1) throw new IllegalStateException("dye 351:4 absent from hotbar");
        int slot = find(client.inventory(), DYE, 4);
        client.selectHeldSlot(slot);
        B173ShearsAccess.dyeMob(client, entity);
        client.sustainTicks(10);
        return count(client.inventory(), DYE, 4) < before;
    }

    private static void use(B173WireClient client, int entity, int item) {
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId(), slot = find(inbound.inventory(), item, -1);
            if (entity < 0 || entity == local) throw new IllegalArgumentException("invalid wolf target");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("wolf use requires synchronized play");
            if (slot < 0) throw new IllegalStateException("item " + item + " absent from hotbar");
            synchronized (channel.output) {
                channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entity); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("wolf Packet7 failed", error); }
    }

    private static int awaitTame(B173WireClient client, int entity) {
        Integer status = until(client, inbound -> {
            int value = inbound.mobs().takeTame(entity);
            return value < 0 ? null : Integer.valueOf(value);
        }, "wolf Packet38 status 6/7 absent before deadline");
        return status.intValue();
    }

    private static void close(B173WireClient client, double x, double y, double z) {
        for (int n = 0; n < 8; n++) {
            PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
            double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 2.5D) return;
            double s = Math.min(1D, 4D / dist);
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

    private static int count(RemoteInventoryView view, int id, int damage) {
        int total = 0;
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id
                    && view.slot(slot).item().damage() == damage)
                total += view.slot(slot).item().count();
        return total;
    }

    private static <T> T until(B173WireClient client, Take<T> take, String absent) {
        B173PlayInbound inbound = client.channel().inbound();
        Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                try {
                    T value = take.read(inbound);
                    if (value != null) return value;
                    inbound.pumpOne();
                    value = take.read(inbound);
                    if (value != null) return value;
                } catch (IOException error) { throw new IllegalStateException(absent, error); }
            }
            throw new IllegalStateException(absent);
        } finally { pulse.interrupt(); }
    }

    private interface Take<T> { T read(B173PlayInbound inbound) throws IOException; }
}
