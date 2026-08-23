package worldline.b173server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

/** Freezes production Packet18 bytes, including the server-ignored source ID. */
public final class B173SwingPacketFixture {
  private B173SwingPacketFixture() {
  }
  public static void main(String[] arguments) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    B173CombatPacket.swing(new DataOutputStream(bytes), 0x01020304);
    byte[] expected = {18, 1, 2, 3, 4, 1};
    if (!Arrays.equals(expected, bytes.toByteArray()))
      throw new AssertionError("Packet18 bytes drifted");
    System.out.println("WORLDLINE_M69_PACKET18_FIXTURE=PASS bytes=6");
  }
}
