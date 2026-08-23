package worldline.smoke.peerswing;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.*;
import worldline.api.PeerSwingSession;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePeerSwing;
import worldline.api.RemoteSwingRequest;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatWeather;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves one named peer Packet18 animation after one isolated local request. */
public final class PeerSwingSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|actor=packet20-named"
            + "|held=packet5-sword276:0|request=packet18-local-id-animation1"
            + "|peer=packet18-named-id-animation1|acceptance=not-claimed|damage=not-claimed|disconnect=clean";
    private PeerSwingSmoke() {}
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: PeerSwingSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]); int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]); String actorName = arguments[4], observerName = arguments[5];
        Duration timeout = Duration.ofSeconds(90); B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true);
        PeerSwingSession observer = client(port, observerName, timeout), actor = client(port, actorName, timeout);
        ExecutorService executor = Executors.newSingleThreadExecutor(); RemoteSwingRequest request; RemotePeerSwing swing;
        try {
            server.boot(); server.save(); server.operator(actorName);
            B173LevelDatWeather.Weather world = B173LevelDatWeather.read(workspace.resolve("world/level.dat"));
            double x = world.spawnX() + 0.5D, y = world.spawnY() + 20D, z = world.spawnZ() + 0.5D;
            B173PlayerSeed.writeHolding(workspace, actorName, x, y, z, 276, 1, 0);
            B173PlayerSeed.write(workspace, observerName, x + 3D, y, z);
            observer.connect(); observer.synchronizePose();
            require(observer.awaitInventory().occupiedSlots() == 0, "observer inventory drifted");
            actor.connect(); actor.synchronizePose(); require(actor.awaitInventory().occupiedSlots() == 1
                    && actor.inventory().slot(36).item().equals(new RemoteItemStack(276, 1, 0)),
                    "actor sword seed drifted");
            actor.look(0F, 90F); actor.selectHeldSlot(0);
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 276, 0));
            Future<RemotePeerSwing> pending = executor.submit(() -> observer.awaitPeerSwing(actorName));
            request = actor.swingHeldItem(); swing = pending.get(20, TimeUnit.SECONDS);
            require(request.username().equals(actorName) && request.entityId() == actor.state().entityId()
                    && swing.username().equals(actorName) && swing.entityId() == request.entityId()
                    && request.animation() == 1 && swing.animation() == 1, "swing evidence drifted");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); require(server.player(actorName).inventoryItems() == 1,
                    "actor sword persistence drifted");
        } finally { executor.shutdownNow(); actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M69_API=peer-swing,packet18,named-observation");
        System.out.println("WORLDLINE_M69_SWING=entity=" + request.entityId() + ";animation=" + swing.animation());
        System.out.println("WORLDLINE_M69_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M69_SIGNATURE=" + sha256(TRACE));
    }
    private static PeerSwingSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
