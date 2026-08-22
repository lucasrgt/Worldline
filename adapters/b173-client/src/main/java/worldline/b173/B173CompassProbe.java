package worldline.b173;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.TextureCompassFX;
import net.minecraft.src.TextureFX;
import net.minecraft.src.World;

/** Runs compass texture probes through the mapped Beta 1.7.3 client root. */
public final class B173CompassProbe {
    private B173CompassProbe() {}

    public static String trace(B173Runtime runtime) {
        require(runtime != null, "runtime is required");
        B173Boundaries.Client client = runtime.backend().client();
        World world = client.theWorld; EntityPlayerSP player = client.thePlayer;
        ChunkCoordinates spawn = world.getSpawnPoint(); double y = player.posY;
        String east0 = sample(client, player, spawn.x + 16.5D, y, spawn.z + 0.5D, 0F);
        String east180 = sample(client, player, spawn.x + 16.5D, y, spawn.z + 0.5D, 180F);
        String west0 = sample(client, player, spawn.x - 15.5D, y, spawn.z + 0.5D, 0F);
        String west180 = sample(client, player, spawn.x - 15.5D, y, spawn.z + 0.5D, 180F);
        require(!east0.equals(east180) && !west0.equals(west180),
                "official compass yaw response is absent");
        require(!east0.equals(west0) && !east180.equals(west180),
                "official compass position response is absent");
        return "spawn=" + spawn.x + ":" + spawn.y + ":" + spawn.z
                + ",east0=" + east0 + ",east180=" + east180
                + ",west0=" + west0 + ",west180=" + west180;
    }

    private static String sample(B173Boundaries.Client client, EntityPlayerSP player,
            double x, double y, double z, float yaw) {
        player.setLocationAndAngles(x, y, z, yaw, 0F);
        TextureCompassFX texture = new TextureCompassFX(client); texture.onTick();
        return "i" + bits(texture, "field_4229_i") + "j" + bits(texture, "field_4228_j")
                + "p" + digest(bytes(texture));
    }

    private static String bits(TextureCompassFX texture, String name) {
        try { Field field = TextureCompassFX.class.getDeclaredField(name); field.setAccessible(true);
            return Long.toHexString(Double.doubleToLongBits(field.getDouble(texture)));
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot read compass state " + name, error); }
    }

    private static byte[] bytes(TextureCompassFX texture) {
        try { Field field = TextureFX.class.getDeclaredField("imageData"); field.setAccessible(true);
            return (byte[]) field.get(texture);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot read compass pixels", error); }
    }

    private static String digest(byte[] value) {
        try { byte[] hash = MessageDigest.getInstance("SHA-256").digest(value);
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte item : hash) result.append(String.format("%02x", item & 255)); return result.toString();
        } catch (java.security.NoSuchAlgorithmException error) { throw new AssertionError(error); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
