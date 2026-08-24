package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteObjectMovement;
import worldline.api.RemoteObjectSpawn;

/** Protocol-14 observer for forward motion in a parallel minecart booster. */
public final class B173MinecartBooster {
    private B173MinecartBooster() { }

    public static void push(B173WireClient client, int entityId) {
        if (client == null || entityId < 1 || entityId == client.state().entityId())
            throw new IllegalArgumentException("invalid minecart push target");
        try {
            B173PlayChannel channel = client.channel();
            synchronized (channel.output) {
                channel.output.writeByte(7);
                channel.output.writeInt(client.state().entityId());
                channel.output.writeInt(entityId);
                channel.output.writeByte(1);
                channel.output.flush();
            }
        } catch (IOException error) {
            throw new IllegalStateException("minecart push Packet7 failed", error);
        }
    }

    public static RemoteObjectMovement awaitForward(B173WireClient client,
            RemoteObjectSpawn spawn, int axisX, int axisZ) {
        if (client == null || spawn == null || (axisX == 0) == (axisZ == 0)
                || Math.abs(axisX) > 1 || Math.abs(axisZ) > 1)
            throw new IllegalArgumentException("invalid minecart forward wait");
        B173PlayInbound inbound = client.channel().inbound();
        Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                RemoteObjectMovement movement = inbound.objects().takeMovement(spawn.entityId());
                if (forward(spawn, movement, axisX, axisZ)) return movement;
                inbound.pumpOne();
                movement = inbound.objects().takeMovement(spawn.entityId());
                if (forward(spawn, movement, axisX, axisZ)) return movement;
            }
            throw new IllegalStateException("forward minecart movement absent before deadline");
        } catch (IOException error) {
            throw new IllegalStateException("forward minecart movement absent", error);
        } finally { pulse.interrupt(); }
    }

    private static boolean forward(RemoteObjectSpawn spawn, RemoteObjectMovement movement,
            int axisX, int axisZ) {
        return movement != null && (movement.toFixedX() - spawn.fixedX()) * axisX
                + (movement.toFixedZ() - spawn.fixedZ()) * axisZ > 0;
    }
}
