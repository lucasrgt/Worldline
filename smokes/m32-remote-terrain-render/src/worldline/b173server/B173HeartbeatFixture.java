package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/** Proves 40 sustained ticks use the vanilla unchanged-pose packet cadence. */
public final class B173HeartbeatFixture {
  private B173HeartbeatFixture() {
  }

  public static void main(String[] arguments) throws Exception {
    ByteArrayOutputStream inboundBytes = new ByteArrayOutputStream();
    DataOutputStream inbound = new DataOutputStream(inboundBytes);
    inbound.writeByte(13);
    inbound.writeDouble(8.5D);
    inbound.writeDouble(66.62D);
    inbound.writeDouble(65.0D);
    inbound.writeDouble(-3.5D);
    inbound.writeFloat(90F);
    inbound.writeFloat(-15F);
    inbound.writeBoolean(false);
    ByteArrayOutputStream outboundBytes = new ByteArrayOutputStream();
    B173PlayChannel channel = new B173PlayChannel(
        new DataInputStream(new ByteArrayInputStream(inboundBytes.toByteArray())),
        new DataOutputStream(outboundBytes), 1000);
    channel.synchronize();
    outboundBytes.reset();
    channel.sustainTicks(40);
    DataInputStream output =
        new DataInputStream(new ByteArrayInputStream(outboundBytes.toByteArray()));
    int flying = 0, pose = 0;
    for (int tick = 1; tick <= 40; tick++) {
      int packet = output.readUnsignedByte();
      if (tick % 20 != 0) {
        require(packet == 10 && !output.readBoolean(), "flying cadence drift");
        flying++;
        continue;
      }
      require(packet == 13, "pose cadence drift");
      require(output.readDouble() == 8.5D && output.readDouble() == 65.0D
              && output.readDouble() == 66.62D && output.readDouble() == -3.5D,
          "unchanged pose payload drift");
      require(output.readFloat() == 90F && output.readFloat() == -15F && !output.readBoolean(),
          "unchanged rotation payload drift");
      pose++;
    }
    require(output.available() == 0 && flying == 38 && pose == 2, "heartbeat packet count drift");
    System.out.println("WORLDLINE_M32_HEARTBEAT_ORACLE=PASS flying=38 pose=2");
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
