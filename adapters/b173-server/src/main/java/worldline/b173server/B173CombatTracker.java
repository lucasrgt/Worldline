package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteCombatStrike;
import worldline.api.RemoteIncomingHit;

/** Correlates fresh Packet38 hurt status with outgoing target or local Packet8 health. */
final class B173CombatTracker {
    private final B173EntityIdentityTracker identities; private final int localId; private final String localName;
    private int health = Integer.MIN_VALUE, outgoingTarget = -1; private boolean localHurt;
    private RemoteCombatStrike outgoing; private RemoteIncomingHit incoming;
    B173CombatTracker(B173EntityIdentityTracker identities, int localId, String localName) {
        this.identities = identities; this.localId = localId; this.localName = localName; }
    void health(DataInputStream input) throws IOException {
        int next = input.readShort();
        if (localHurt) { if (health != 20 || next != 18) throw new IOException("armored combat health drift");
            incoming = new RemoteIncomingHit(localName, localId, health, next); localHurt = false; }
        health = next;
    }
    void status(DataInputStream input) throws IOException {
        int entityId = input.readInt(), status = input.readByte(); if (status != 2) return;
        if (entityId == outgoingTarget) { String target = identities.username(entityId);
            if (target == null) throw new IOException("outgoing target identity absent");
            outgoing = new RemoteCombatStrike(localName, localId, target, entityId); outgoingTarget = -1; }
        if (entityId == localId) { if (health != 20 || localHurt) throw new IOException("local hurt baseline drift");
            localHurt = true; }
    }
    void beginOutgoing(int target) { if (target < 0 || outgoingTarget >= 0 || outgoing != null)
        throw new IllegalStateException("outgoing combat request pending"); outgoingTarget = target; }
    RemoteCombatStrike takeOutgoing() { RemoteCombatStrike value = outgoing; outgoing = null; return value; }
    RemoteIncomingHit takeIncoming(int expectedHealth) throws IOException { if (incoming == null) return null;
        if (incoming.healthAfter() != expectedHealth) throw new IOException("incoming health expectation drift");
        RemoteIncomingHit value = incoming; incoming = null; return value; }
    int entityId(String username) { return identities.entityId(username); }
}
