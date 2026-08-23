import java.lang.reflect.Field;
import java.security.MessageDigest;

/** Runs compass texture probes through the official obfuscated client root. */
final class OfficialCompassProbe {
  private OfficialCompassProbe() {
  }

  static String trace(net.minecraft.client.Minecraft client, fd world, dc player) {
    br spawn = world.u();
    double y = player.aN;
    String east0 = sample(client, player, spawn.a + 16.5D, y, spawn.c + 0.5D, 0F);
    String east180 = sample(client, player, spawn.a + 16.5D, y, spawn.c + 0.5D, 180F);
    String west0 = sample(client, player, spawn.a - 15.5D, y, spawn.c + 0.5D, 0F);
    String west180 = sample(client, player, spawn.a - 15.5D, y, spawn.c + 0.5D, 180F);
    require(!east0.equals(east180) && !west0.equals(west180),
        "official compass yaw response is absent");
    require(!east0.equals(west0) && !east180.equals(west180),
        "official compass position response is absent");
    return "spawn=" + spawn.a + ":" + spawn.b + ":" + spawn.c + ",east0=" + east0
        + ",east180=" + east180 + ",west0=" + west0 + ",west180=" + west180;
  }

  private static String sample(
      net.minecraft.client.Minecraft client, dc player, double x, double y, double z, float yaw) {
    player.c(x, y, z, yaw, 0F);
    av texture = new av(client);
    texture.a();
    return "i" + bits(texture, "i") + "j" + bits(texture, "j") + "p" + digest(bytes(texture));
  }

  private static String bits(av texture, String name) {
    try {
      Field field = av.class.getDeclaredField(name);
      field.setAccessible(true);
      return Long.toHexString(Double.doubleToLongBits(field.getDouble(texture)));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException("cannot read compass state " + name, error);
    }
  }

  private static byte[] bytes(av texture) {
    try {
      Field field = aw.class.getDeclaredField("a");
      field.setAccessible(true);
      return (byte[]) field.get(texture);
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException("cannot read compass pixels", error);
    }
  }

  private static String digest(byte[] value) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256").digest(value);
      StringBuilder result = new StringBuilder(hash.length * 2);
      for (byte item : hash)
        result.append(String.format("%02x", item & 255));
      return result.toString();
    } catch (java.security.NoSuchAlgorithmException error) {
      throw new AssertionError(error);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
