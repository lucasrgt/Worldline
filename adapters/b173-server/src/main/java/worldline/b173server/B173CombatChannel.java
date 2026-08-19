package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteCombatStrike;
import worldline.api.RemoteSwingRequest;

/** Exact Packet7 attack and Packet18 held-item swing requests. */
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
    RemoteSwingRequest swing() throws IOException { B173CombatPacket.swing(output, localId);
        output.flush(); return new RemoteSwingRequest(localName, localId); }
    void attackMob(int target) throws IOException {
        if (target < 0 || target == localId) throw new IllegalArgumentException("invalid mob target");
        output.writeByte(7); output.writeInt(localId); output.writeInt(target); output.writeByte(1); output.flush(); }
}
