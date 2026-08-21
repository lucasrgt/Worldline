package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;

/** Shared exact protocol-14 combat request encoders. */
final class B173CombatPacket {
    private B173CombatPacket() {}
    static void swing(DataOutputStream output, int entityId) throws IOException {
        if (entityId < 0) throw new IllegalArgumentException("invalid swing entity");
        output.writeByte(18); output.writeInt(entityId); output.writeByte(1);
    }
}
