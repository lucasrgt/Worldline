package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/** Writes one minimal official-format player NBT before its first login. */
public final class B173PlayerSeed {
    private B173PlayerSeed() {}

    public static void write(Path serverDirectory, String username, double x, double y, double z) {
        write(serverDirectory, username, x, y, z, 0, new int[0], new int[0], new int[0], new int[0], 20);
    }

    /** Writes one empty player in an exact vanilla dimension. */
    public static void writeDimension(Path serverDirectory, String username, double x, double y, double z,
            int dimension) {
        if (dimension != 0 && dimension != -1) throw new IllegalArgumentException("invalid player dimension");
        write(serverDirectory, username, x, y, z, dimension, new int[0], new int[0], new int[0], new int[0], 20);
    }

    /** Writes one player whose selected hotbar slot contains an exact legacy block stack. */
    public static void writeHolding(Path serverDirectory, String username, double x, double y, double z,
            int legacyId, int count, int damage) {
        if (legacyId < 1 || legacyId > 255 || count < 1 || count > 64 || damage < 0 || damage > 32767)
            throw new IllegalArgumentException("invalid held player seed");
        write(serverDirectory, username, x, y, z, 0, new int[] {0}, new int[] {legacyId},
                new int[] {count}, new int[] {damage}, 20);
    }

    /** Writes exact main-inventory slots and optional vanilla armor slots 100-103. */
    public static void writeInventory(Path serverDirectory, String username, double x, double y, double z,
            int[] slots, int[] legacyIds, int[] counts, int[] damages) {
        writeInventory(serverDirectory, username, x, y, z, slots, legacyIds, counts, damages, 20);
    }

    /** Writes exact main-inventory slots and an official Health short. */
    public static void writeInventory(Path serverDirectory, String username, double x, double y, double z,
            int[] slots, int[] legacyIds, int[] counts, int[] damages, int health) {
        inventory(serverDirectory, username, x, y, z, 0, slots, legacyIds, counts, damages, health);
    }

    /** Writes exact main-inventory slots into an exact vanilla dimension. */
    public static void writeInventory(Path serverDirectory, String username, double x, double y, double z,
            int dimension, int[] slots, int[] legacyIds, int[] counts, int[] damages) {
        inventory(serverDirectory, username, x, y, z, dimension, slots, legacyIds, counts, damages, 20);
    }

    private static void inventory(Path serverDirectory, String username, double x, double y, double z, int dimension,
            int[] slots, int[] legacyIds, int[] counts, int[] damages, int health) {
        if ((dimension != 0 && dimension != -1) || slots == null || legacyIds == null || counts == null
                || damages == null || slots.length != legacyIds.length || slots.length != counts.length
                || slots.length != damages.length || slots.length > 40 || health < 1 || health > 20)
            throw new IllegalArgumentException("invalid player inventory seed");
        for (int index = 0; index < slots.length; index++) {
            if (!slot(slots[index]) || legacyIds[index] < 1 || legacyIds[index] > 32767
                    || counts[index] < 1 || counts[index] > 64 || damages[index] < 0 || damages[index] > 32767)
                throw new IllegalArgumentException("invalid player inventory item");
            for (int prior = 0; prior < index; prior++) if (slots[prior] == slots[index])
                throw new IllegalArgumentException("duplicate player inventory slot");
        }
        write(serverDirectory, username, x, y, z, dimension, slots.clone(), legacyIds.clone(), counts.clone(),
                damages.clone(), health);
    }

    private static void write(Path serverDirectory, String username, double x, double y, double z, int dimension,
            int[] slots, int[] legacyIds, int[] counts, int[] damages, int health) {
        if (serverDirectory == null || username == null
                || !username.matches("[A-Za-z0-9_]{1,16}") || !finite(x) || !finite(y) || !finite(z))
            throw new IllegalArgumentException("invalid player seed");
        Path root = serverDirectory.toAbsolutePath().normalize();
        Path players = root.resolve("world/players").normalize();
        Path target = players.resolve(username + ".dat").normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("player seed escaped server directory");
        try { Files.createDirectories(players);
            try (DataOutputStream output = new DataOutputStream(
                    new GZIPOutputStream(Files.newOutputStream(target)))) {
                output.writeByte(10); output.writeUTF("");
                list(output, "Pos", 6, 3); output.writeDouble(x); output.writeDouble(y); output.writeDouble(z);
                list(output, "Motion", 6, 3); output.writeDouble(0); output.writeDouble(0); output.writeDouble(0);
                list(output, "Rotation", 5, 2); output.writeFloat(0); output.writeFloat(0);
                floating(output, "FallDistance", 0); shortTag(output, "Fire", -20);
                shortTag(output, "Air", 300); byteTag(output, "OnGround", 0);
                intTag(output, "Dimension", dimension); list(output, "Inventory", 10, slots.length);
                for (int index = 0; index < slots.length; index++) { shortTag(output, "id", legacyIds[index]);
                    byteTag(output, "Count", counts[index]); shortTag(output, "Damage", damages[index]);
                    byteTag(output, "Slot", slots[index]); output.writeByte(0); }
                shortTag(output, "Health", health); shortTag(output, "HurtTime", 0);
                shortTag(output, "DeathTime", 0); shortTag(output, "AttackTime", 0);
                intTag(output, "Score", 0); output.writeByte(0);
            }
        } catch (IOException error) { throw new IllegalStateException("could not write player seed", error); }
    }

    private static void list(DataOutputStream output, String name, int type, int size) throws IOException {
        output.writeByte(9); output.writeUTF(name); output.writeByte(type); output.writeInt(size); }
    private static void floating(DataOutputStream output, String name, float value) throws IOException {
        output.writeByte(5); output.writeUTF(name); output.writeFloat(value); }
    private static void shortTag(DataOutputStream output, String name, int value) throws IOException {
        output.writeByte(2); output.writeUTF(name); output.writeShort(value); }
    private static void byteTag(DataOutputStream output, String name, int value) throws IOException {
        output.writeByte(1); output.writeUTF(name); output.writeByte(value); }
    private static void intTag(DataOutputStream output, String name, int value) throws IOException {
        output.writeByte(3); output.writeUTF(name); output.writeInt(value); }
    private static boolean slot(int value) { return (value >= 0 && value <= 35) || (value >= 100 && value <= 103); }
    private static boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
}
