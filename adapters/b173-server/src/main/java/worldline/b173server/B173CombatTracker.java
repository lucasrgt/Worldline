package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteCombatStrike;
import worldline.api.RemoteIncomingHit;

/** Correlates Packet18 peer swings and fresh Packet38/8 combat evidence. */
final class B173CombatTracker {
    private final B173EntityIdentityTracker identities; private final int localId; private final String localName;
    private int health = Integer.MIN_VALUE, outgoingTarget = -1, expectedSwing = -1; private boolean localHurt;
    private RemoteCombatStrike outgoing; private RemoteIncomingHit incoming; private worldline.api.RemotePeerSwing swing;
    B173CombatTracker(B173EntityIdentityTracker identities, int localId, String localName) {
        this.identities = identities; this.localId = localId; this.localName = localName; }
    void health(DataInputStream input) throws IOException {
        int next = input.readShort();
        if (localHurt) { if (health >= 1 && health <= 20 && next >= 0 && next < health)
                incoming = new RemoteIncomingHit(localName, localId, health, next); localHurt = false; }
        health = next;
    }
    void status(int entityId, int status) throws IOException {
        if (status != 2) return;
        if (entityId == outgoingTarget) { String target = identities.username(entityId);
            if (target == null) throw new IOException("outgoing target identity absent");
            outgoing = new RemoteCombatStrike(localName, localId, target, entityId); outgoingTarget = -1; }
        if (entityId == localId) localHurt = true;
    }
    void animation(DataInputStream input) throws IOException { int entityId = input.readInt(), code = input.readByte();
        if (entityId != expectedSwing) return; if (code != 1 || swing != null) throw new IOException("peer swing drift");
        String name = identities.username(entityId); if (name == null) throw new IOException("peer swing identity absent");
        swing = new worldline.api.RemotePeerSwing(name, entityId); expectedSwing = -1; }
    void beginOutgoing(int target) { if (target < 0 || outgoingTarget >= 0 || outgoing != null)
        throw new IllegalStateException("outgoing combat request pending"); outgoingTarget = target; }
    RemoteCombatStrike takeOutgoing() { RemoteCombatStrike value = outgoing; outgoing = null; return value; }
    RemoteIncomingHit takeIncoming(int expectedHealth) throws IOException { if (incoming == null) return null;
        if (incoming.healthAfter() != expectedHealth) throw new IOException("incoming health expectation drift: " + incoming.healthAfter() + " != " + expectedHealth);
        RemoteIncomingHit value = incoming; incoming = null; return value; }
    int entityId(String username) { return identities.entityId(username); }
    void expectSwing(String username) { int id = identities.entityId(username); if (id < 0 || expectedSwing >= 0 || swing != null)
        throw new IllegalStateException("peer swing expectation invalid"); expectedSwing = id; }
    worldline.api.RemotePeerSwing takeSwing() { worldline.api.RemotePeerSwing value = swing; swing = null; return value; } int health() { return health; }
}
