package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteRainStart;

/** Public live rain-start boundary kept out of the capped play client. */
public final class B173RainAccess {
    private B173RainAccess() {}

    public static void arm(B173WireClient client) {
        client.channel().inbound().weather().arm();
    }

    public static RemoteRainStart awaitRainStart(B173WireClient client) {
        try { return client.channel().awaitRainStart(); }
        catch (IOException error) { throw new IllegalStateException("rain start receive failed", error); }
    }
}
