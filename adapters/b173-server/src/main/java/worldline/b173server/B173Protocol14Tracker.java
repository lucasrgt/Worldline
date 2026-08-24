package worldline.b173server;

import worldline.api.RemoteMapData;
import worldline.api.RemoteProtocol14Chain;
import worldline.api.RemoteSignText;

/** Bounded ordering tracker for protocol-14 Packet130 and Packet131. */
final class B173Protocol14Tracker {
    private int sequence, signSequence = -1, mapSequence = -1, keepAlivePackets;
    private RemoteSignText sign;
    private RemoteMapData map;

    int next() { return ++sequence; }
    void keepAlive() { keepAlivePackets++; }
    void sign(int at, RemoteSignText value) {
        sign = value; signSequence = at; map = null; mapSequence = -1;
    }
    void map(int at, RemoteMapData value) {
        if (sign != null && at > signSequence) { map = value; mapSequence = at; }
    }
    void reset() {
        sign = null; map = null; signSequence = -1; mapSequence = -1; keepAlivePackets = 0;
    }
    RemoteProtocol14Chain take(RemoteSignText expected) {
        if (sign == null || map == null || !sign.equals(expected)) return null;
        RemoteProtocol14Chain value = new RemoteProtocol14Chain(
                sign, signSequence, map, mapSequence, keepAlivePackets);
        sign = null; map = null; return value;
    }
}
