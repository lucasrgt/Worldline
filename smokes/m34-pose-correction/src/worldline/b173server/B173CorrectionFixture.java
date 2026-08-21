package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import worldline.api.PlayerPose;

/** Proves server Packet13 decode, stance normalization, and exact acknowledgement. */
public final class B173CorrectionFixture {
    private B173CorrectionFixture() {}

    public static void main(String[] arguments) throws Exception {
        ByteArrayOutputStream source = new ByteArrayOutputStream(); DataOutputStream packet = new DataOutputStream(source);
        packet.writeDouble(-3.5D); packet.writeDouble(72.62D); packet.writeDouble(71D);
        packet.writeDouble(8.5D); packet.writeFloat(135F); packet.writeFloat(-22.5F); packet.writeBoolean(true);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        B173PlayInbound inbound = new B173PlayInbound(new DataInputStream(
                new ByteArrayInputStream(source.toByteArray())), new DataOutputStream(sink), 1000);
        inbound.skip(13); B173PlayInbound.Correction correction = inbound.takeCorrection();
        require(correction != null && correction.pose.equals(new PlayerPose(-3.5D, 71D, 8.5D, 135F, -22.5F))
                && Math.abs(correction.stance - 1.62D) < 0.000001D, "correction decode drift");
        DataInputStream output = new DataInputStream(new ByteArrayInputStream(sink.toByteArray()));
        require(output.readUnsignedByte() == 13 && output.readDouble() == -3.5D
                && output.readDouble() == 71D && output.readDouble() == 72.62D
                && output.readDouble() == 8.5D && output.readFloat() == 135F
                && output.readFloat() == -22.5F && !output.readBoolean() && output.available() == 0,
                "correction acknowledgement drift");
        System.out.println("WORLDLINE_M34_CORRECTION_ORACLE=PASS");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
