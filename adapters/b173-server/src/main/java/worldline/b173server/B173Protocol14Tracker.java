package worldline.b173server;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteMapData;
import worldline.api.RemoteProtocol14Chain;
import worldline.api.RemoteSignText;

/** Bounded ordering tracker for protocol-14 Packet130 and Packet131. */
final class B173Protocol14Tracker {
    private int sequence, signSequence = -1, mapSequence = -1, keepAlivePackets;
    private final ArrayDeque<RemoteMapData> maps = new ArrayDeque<>();
    private RemoteSignText sign;
    private RemoteMapData map;

    int next() { return ++sequence; }
    void keepAlive() { keepAlivePackets++; }
    void sign(int at, RemoteSignText value) {
        sign = value; signSequence = at; map = null; mapSequence = -1;
    }
    void map(int at, RemoteMapData value) {
        if (maps.size() == 512) maps.removeFirst();
        maps.addLast(value);
        if (sign != null && at > signSequence) { map = value; mapSequence = at; }
    }
    void reset() {
        sign = null; map = null; maps.clear();
        signSequence = -1; mapSequence = -1; keepAlivePackets = 0;
    }
    List<RemoteMapData> drainMaps() {
        List<RemoteMapData> values = new ArrayList<>(maps);
        maps.clear(); return values;
    }
    RemoteProtocol14Chain take(RemoteSignText expected) {
        if (sign == null || map == null || !sign.equals(expected)) return null;
        RemoteProtocol14Chain value = new RemoteProtocol14Chain(
                sign, signSequence, map, mapSequence, keepAlivePackets);
        sign = null; map = null; return value;
    }
}
