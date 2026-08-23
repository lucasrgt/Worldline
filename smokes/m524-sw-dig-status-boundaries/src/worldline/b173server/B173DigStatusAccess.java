package worldline.b173server;

import java.io.DataOutputStream;
import java.lang.reflect.Field;
import worldline.api.BlockPosition;

/** Smoke-only access to raw Packet14 statuses that the public session does not expose. */
public final class B173DigStatusAccess {
  private B173DigStatusAccess() {
  }

  public static void send(B173WireClient client, BlockPosition position, int status) {
    if (client == null || status < 0 || status > 4) {
      throw new IllegalArgumentException("invalid raw dig request");
    }
    try {
      Field playField = B173WireClient.class.getDeclaredField("play");
      playField.setAccessible(true);
      Object play = playField.get(client);
      Field outputField = B173PlayChannel.class.getDeclaredField("output");
      outputField.setAccessible(true);
      B173Dig.write((DataOutputStream) outputField.get(play), position, status);
    } catch (ReflectiveOperationException | java.io.IOException error) {
      throw new IllegalStateException("raw Packet14 status failed", error);
    }
  }
}
