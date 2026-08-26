package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteRainStart;
import worldline.api.RemoteRainStop;

/** Public live rain-transition boundary kept out of the capped play client. */
public final class B173RainAccess {
    private B173RainAccess() {}

    public static void arm(B173WireClient client) {
        client.channel().inbound().weather().arm();
    }

    public static RemoteRainStart awaitRainStart(B173WireClient client) {
        try { return client.channel().awaitRainStart(); }
        catch (IOException error) { throw new IllegalStateException("rain start receive failed", error); }
    }

    public static void armStop(B173WireClient client) {
        client.channel().inbound().weather().armStop();
    }

    public static RemoteRainStop awaitRainStop(B173WireClient client) {
        B173PlayInbound inbound = client.channel().inbound();
        Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                RemoteRainStop value = inbound.weather().takeStop();
                if (value != null) return value;
                inbound.pumpOne();
            }
            throw new IOException("expected rain stop absent before deadline");
        } catch (IOException error) {
            throw new IllegalStateException("rain stop receive failed", error);
        } finally {
            pulse.interrupt();
        }
    }
}
