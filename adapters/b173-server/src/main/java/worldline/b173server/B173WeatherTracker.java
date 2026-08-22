package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteRainStart;

/** Adapter-owned Packet70Bed game-state tracker for the controlled dry-to-rain transition. */
final class B173WeatherTracker {
    private boolean armed;
    private boolean raining;
    private boolean preArmRainStart;
    private RemoteRainStart start;

    void arm() {
        if (armed) throw new IllegalStateException("weather tracker is already armed");
        if (preArmRainStart) throw new IllegalStateException(
                "pre-arm rain start observed; cannot arm for a fresh transition");
        armed = true;
    }

    boolean armed() { return armed; }
    boolean raining() { return raining; }
    boolean preArmRainStart() { return preArmRainStart; }

    int accept(DataInputStream input) throws IOException {
        int reason = input.readUnsignedByte();
        if (reason > 2) throw new IOException("invalid Packet70 reason " + reason);
        if (!armed) {
            if (reason == 1) preArmRainStart = true;
            return reason;
        }
        if (reason == 1) {
            if (raining) throw new IOException("repeated controlled rain start");
            raining = true;
            start = new RemoteRainStart(RemoteRainStart.RAIN_PACKET_ID,
                    RemoteRainStart.BEGIN_RAIN_REASON, true, true);
            return reason;
        }
        throw new IOException("conflicting controlled Packet70 reason " + reason);
    }

    RemoteRainStart takeStart() {
        RemoteRainStart value = start;
        start = null;
        return value;
    }
}
