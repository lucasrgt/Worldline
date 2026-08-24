package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;
import worldline.test.WorldlineSmokeAwait;

/** Smoke-local bounded Packet7 tame plus Packet40/NBT owner-collar metadata. */
public final class B173WolfTameAccess {
    public static final int TYPE = 95, BONE = 352, MAX_BONES = 64;

    private B173WolfTameAccess() {}

    public static void retarget(Path directory, BlockPosition spawner) {
        B173SpawnerSeed.wolf(directory, spawner);
    }

    public static int tameBounded(B173WireClient client, RemoteMobSpawn spawn, int maximum) {
        if (maximum < 1 || maximum > MAX_BONES) throw new IllegalArgumentException("maximum bones");
        int entity = spawn.entityId();
        double x = spawn.x(), y = spawn.y(), z = spawn.z();
        RemoteMobMovement move = client.channel().inbound().mobs().takeMovement(entity);
        if (move != null) { x = move.toX(); y = move.toY(); z = move.toZ(); }
        close(client, x, y, z);
        if (count(client.inventory(), BONE) != maximum)
            throw new IllegalStateException("bone 352 count drift before bounded tame");
        int slot = find(client.inventory(), BONE);
        if (slot < 0) throw new IllegalStateException("bone 352 absent from hotbar");
        client.selectHeldSlot(slot);
        for (int attempts = 1; attempts <= maximum; attempts++) {
            use(client, entity);
            int status = WorldlineSmokeAwait.awaitCheckedEntity(client,
                    () -> Integer.valueOf(client.channel().inbound().mobs().takeTame(entity)),
                    value -> value.intValue() >= 0, "wolf Packet38 status 6/7", 20).intValue();
            if (status != 6 && status != 7)
                throw new IllegalStateException("wolf Packet38 tame status drift " + status);
            awaitBones(client, maximum - attempts);
            if (status == 6) continue;
            int flags = WorldlineSmokeAwait.awaitCheckedEntity(client,
                    () -> Integer.valueOf(client.channel().inbound().mobs().size(entity)),
                    value -> (value.intValue() & 4) != 0, "wolf Packet40 tamed bit", 20).intValue();
            if ((flags & 4) == 0) throw new IllegalStateException("wolf Packet40 tamed bit absent");
            return attempts;
        }
        throw new IllegalStateException("wolf did not tame within " + maximum + " bones");
    }

    private static void awaitBones(B173WireClient client, int expected) {
        WorldlineSmokeAwait.awaitCheckedEntity(client,
                () -> Integer.valueOf(count(client.inventory(), BONE)),
                value -> value.intValue() == expected, "bounded bone consumption", 20);
    }

    public static String owner(Path directory, int chunkX, int chunkZ, String username) {
        if (directory == null || username == null || username.isEmpty())
            throw new IllegalArgumentException("invalid wolf owner probe");
        Path root = directory.toAbsolutePath().normalize();
        Path region = root.resolve("world/region/r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mcr")
                .normalize();
        if (!region.startsWith(root) || !Files.isRegularFile(region))
            throw new IllegalStateException("wolf region absent");
        try (RandomAccessFile file = new RandomAccessFile(region.toFile(), "r")) {
            int index = (chunkX & 31) + (chunkZ & 31) * 32;
            file.seek(index * 4L);
            int loc = file.readInt(), offset = (loc >>> 8) * 4096, sectors = loc & 255;
            if (offset == 0 || sectors < 1) throw new IllegalStateException("wolf chunk absent");
            file.seek(offset);
            int length = file.readInt(), type = file.readUnsignedByte();
            if (length < 2 || type < 1 || type > 2) throw new IllegalStateException("invalid wolf chunk");
            byte[] compressed = new byte[length - 1];
            file.readFully(compressed);
            B173Nbt.Compound tree;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(inflate(compressed, type)))) {
                tree = B173Nbt.read(in);
            }
            return owner(tree, username);
        } catch (IOException error) {
            throw new IllegalStateException("could not read wolf Owner NBT", error);
        }
    }

    private static String owner(B173Nbt.Compound root, String username) {
        Object levelValue = root.entries.get("Level");
        if (!(levelValue instanceof B173Nbt.Compound)) throw new IllegalStateException("chunk Level absent");
        Object entitiesValue = ((B173Nbt.Compound) levelValue).entries.get("Entities");
        if (!(entitiesValue instanceof B173Nbt.ListValue))
            throw new IllegalStateException("chunk Entities absent");
        String found = null;
        int wolves = 0;
        for (Object value : ((B173Nbt.ListValue) entitiesValue).items) {
            if (!(value instanceof B173Nbt.Compound)) continue;
            B173Nbt.Compound entity = (B173Nbt.Compound) value;
            if (!"Wolf".equals(entity.entries.get("id"))) continue;
            wolves++;
            Object owner = entity.entries.get("Owner");
            if (username.equals(owner)) {
                if (found != null) throw new IllegalStateException("duplicate tamed wolf Owner");
                found = username;
            }
        }
        if (wolves < 1) throw new IllegalStateException("persisted Wolf entity absent");
        if (found == null) throw new IllegalStateException("wolf Owner NBT absent");
        return found;
    }

    private static void use(B173WireClient client, int entity) {
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId(), slot = find(inbound.inventory(), BONE);
            if (entity < 0 || entity == local) throw new IllegalArgumentException("invalid wolf target");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("wolf tame requires synchronized play");
            if (slot < 0) throw new IllegalStateException("bone 352 absent from hotbar");
            synchronized (channel.output) {
                channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entity); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("wolf Packet7 failed", error); }
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

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 0; slot <= 8; slot++)
            if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == id)
                return slot;
        return -1;
    }

    private static int count(RemoteInventoryView view, int id) {
        int total = 0;
        for (int slot = 36; slot <= 44; slot++)
            if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
                total += view.slot(slot).item().count();
        return total;
    }

    private static byte[] inflate(byte[] data, int type) throws IOException {
        InputStream source = type == 1 ? new GZIPInputStream(new ByteArrayInputStream(data))
                : new InflaterInputStream(new ByteArrayInputStream(data));
        try (InputStream in = source; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) >= 0) out.write(buffer, 0, count);
            return out.toByteArray();
        }
    }
}
