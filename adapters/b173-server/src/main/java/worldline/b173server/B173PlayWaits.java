package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Timed inbound waits extracted from the play pump so mob identity stays adapter-owned. */
final class B173PlayWaits {
    private final B173PlayInbound inbound;
    B173PlayWaits(B173PlayInbound inbound) { this.inbound = inbound; }

    RemoteMobSpawn spawn(int type) throws IOException {
        return until(() -> inbound.mobs().take(type), "expected mob spawn absent before deadline");
    }

    RemoteMobSpawn spawnAny(int[] types) throws IOException {
        if (types == null || types.length < 1) throw new IllegalArgumentException("invalid expected mob types");
        for (int i = 0; i < types.length; i++)
            if (types[i] < 0 || types[i] > 127) throw new IllegalArgumentException("invalid expected mob type");
        return until(() -> {
            for (int type : types) {
                RemoteMobSpawn value = inbound.mobs().take(type);
                if (value != null) return value;
            }
            return null;
        }, "expected hostile spawn absent before deadline");
    }

    RemoteMobMovement movement(int entity) throws IOException {
        return until(() -> inbound.mobs().takeMovement(entity),
                "expected mob movement absent before deadline");
    }

    RemoteMobDeath death(int entity) throws IOException {
        return until(() -> inbound.mobs().takeDeath(entity),
                "expected mob death absent before deadline");
    }

    B173EntityVelocity velocity(int entity) throws IOException {
        return until(() -> inbound.velocities().take(entity),
                "expected Packet28 velocity absent before deadline");
    }

    private <T> T until(java.util.concurrent.Callable<T> take, String absent) throws IOException {
        Thread pulse = inbound.pulse(); long deadline = System.nanoTime() + inbound.timeoutNanos();
        try { for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
            try { T value = take.call(); if (value != null) return value; }
            catch (IOException error) { throw error; }
            catch (Exception error) { throw new IOException(error); }
            inbound.pumpOne();
        } throw new IOException(absent); } finally { pulse.interrupt(); }
    }

    RemoteMobMovement observedMovement() throws IOException {
        return movement(inbound.mobs().observed());
    }

    RemoteMobDeath observedDeath() throws IOException {
        return death(inbound.mobs().observed());
    }

    worldline.api.RemoteObjectSpawn object(int type) throws IOException {
        return until(() -> inbound.objects().take(type), "expected object spawn absent before deadline");
    }

    worldline.api.RemoteObjectSpawn objectFrom(int type, int thrower) throws IOException {
        if (thrower < 1) throw new IllegalArgumentException("invalid expected object thrower");
        return until(() -> inbound.objects().takeFrom(type, thrower), "expected thrown object absent before deadline");
    }

    worldline.api.RemoteBedUse bed() throws IOException {
        return until(() -> inbound.beds().takeSleep(), "expected Packet17 sleep absent before deadline");
    }

    worldline.api.RemoteRainStart rainStart() throws IOException {
        return until(() -> inbound.weather().takeStart(), "expected rain start absent before deadline");
    }

    worldline.api.RemoteNoteEvent note() throws IOException {
        return until(() -> inbound.notes().take(), "expected note event absent before deadline");
    }

    worldline.api.RemoteSignText sign() throws IOException {
        return until(() -> inbound.signs().take(), "expected Packet130 sign text absent before deadline");
    }

    worldline.api.RemotePaintingSpawn painting() throws IOException {
        return until(() -> inbound.paintings().take(), "expected Packet25 painting absent before deadline");
    }

    Integer destroy(int entity) throws IOException {
        if (entity < 0) throw new IllegalArgumentException("invalid painting entity id");
        return until(() -> inbound.paintings().takeDestroy(entity),
                "expected Packet29 painting destroy absent before deadline");
    }

    Integer fuse(int entity) throws IOException { return until(() -> { int value = inbound.mobs().takeFuse(entity); return value < 0 ? null : Integer.valueOf(value); }, "expected creeper Packet40 fuse absent before deadline"); }
}
