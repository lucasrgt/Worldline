package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteCombatStrike;

/** Exact Packet7 diamond-sword named-player attack request. */
final class B173CombatChannel {
    private final DataOutputStream output; private final B173PlayInbound inbound;
    private final int localId; private final String localName;
    B173CombatChannel(DataOutputStream output, B173PlayInbound inbound, int localId, String localName) {
        this.output = output; this.inbound = inbound; this.localId = localId; this.localName = localName; }
    RemoteCombatStrike attack(String targetName) throws IOException {
        if (targetName == null || targetName.equals(localName)) throw new IllegalArgumentException("invalid combat target");
        int target = inbound.combatEntityId(targetName); if (target < 0) throw new IllegalStateException("combat target absent");
        inbound.beginCombat(target); output.writeByte(7); output.writeInt(localId);
        output.writeInt(target); output.writeByte(1); output.flush();
        return inbound.awaitCombatStrike();
    }
}
