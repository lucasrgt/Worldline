package worldline.b173server;

import java.io.IOException;
import worldline.api.PlayerPose;

/** Packet13 air moves that let the official server accumulate fallDistance. */
public final class B173Fall {
    private B173Fall() {}

    public static PlayerPose air(B173WireClient actor, double dx, double dy, double dz) {
        try { return actor.channel().move(dx, dy, dz, false); }
        catch (IOException error) { throw new IllegalStateException("fall air move failed", error); }
    }

    public static int awaitDrop(B173WireClient actor, int before) {
        try {
            B173PlayInbound inbound = actor.channel().inbound();
            long end = System.currentTimeMillis() + 10000L; int last = before, stable = 0;
            while (System.currentTimeMillis() < end) {
                inbound.pumpAvailable();
                int now = actor.health();
                if (now < before) {
                    if (now == last) { if (++stable >= 8) return now; }
                    else { last = now; stable = 0; }
                }
                Thread.sleep(50L);
            }
            if (last < before) return last;
            throw new IllegalStateException("fall Packet8 absent health=" + actor.health());
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("fall health wait interrupted", error);
        } catch (IOException error) {
            throw new IllegalStateException("fall health wait failed", error);
        }
    }
}
