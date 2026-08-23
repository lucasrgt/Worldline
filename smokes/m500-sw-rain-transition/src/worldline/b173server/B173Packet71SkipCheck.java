package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/** Focused decoder regression: a Packet71 lightning body must consume exactly 17 bytes. */
public final class B173Packet71SkipCheck {
  private B173Packet71SkipCheck() {
  }

  public static void verify() {
    try {
      byte[] stream = new byte[18];
      Arrays.fill(stream, (byte) 0x2A);
      stream[17] = 0x7E;
      DataInputStream input = new DataInputStream(new ByteArrayInputStream(stream));
      B173InboundPacket.skip(input, 71);
      if (input.readUnsignedByte() != 0x7E)
        throw new AssertionError("Packet71 skipper did not consume the 17-byte body");
    } catch (IOException error) {
      throw new AssertionError("Packet71 skip regression failed", error);
    }
  }
}
