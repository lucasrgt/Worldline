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
                intTag(output, "Dimension", 0); list(output, "Inventory", 10, 0);
                shortTag(output, "Health", 20); shortTag(output, "HurtTime", 0);
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
    private static boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
}
