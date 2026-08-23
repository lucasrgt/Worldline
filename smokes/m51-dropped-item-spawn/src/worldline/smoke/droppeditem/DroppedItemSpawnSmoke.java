package worldline.smoke.droppeditem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.DroppedItemMultiplayerSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves the server-authoritative item entity created by a held-item drop. */
public final class DroppedItemSpawnSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|initial=stone"
            + "|action=drop-current|spawn=packet21|stack=1x1:0|position=near-actor"
            + "|velocity=bounded-nonzero|local=empty|peer=empty|persisted=0|disconnect=clean";
    private DroppedItemSpawnSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: DroppedItemSpawnSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        DroppedItemMultiplayerSession actor = client(port, actorName, timeout);
        DroppedItemMultiplayerSession observer = client(port, observerName, timeout);
        RemoteInventoryView after; RemoteHeldItem empty; RemoteDroppedItem dropped; ServerPlayerState player;
        try {
            server.boot(); B173PlayerSeed.writeHolding(workspace, actorName, 4.5D, 60D, 4.5D, 1, 1, 0);
            B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
            actor.connect(); PlayerPose actorPose = actor.synchronizePose(); actor.awaitInventory();
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            require(actor.inventory().occupiedSlots() == 1 && actor.inventory().slot(36).item().equals(stone),
                    "held seed drifted");
            observer.connect(); observer.synchronizePose(); requirePlayers(server.players(), actorName, observerName);
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0)); actor.dropHeldItem();
            dropped = observer.awaitDroppedItem(stone); requireSpawn(dropped, actorPose);
            after = worldline.test.WorldlineSmokeAwait.awaitEntity(actor, actor::inventory,
                    view -> view.occupiedSlots() == 0, "dropped local slot", 10); require(after.occupiedSlots() == 0
                    && after.slot(36).empty(), "local dropped slot did not become empty");
            empty = observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName)); require(empty.empty(), "peer hand not empty");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 0, "dropped inventory was persisted as occupied");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M51_API=dropped-item,packet21,immutable,server-authoritative");
        System.out.println("WORLDLINE_M51_DROP=" + dropped);
        System.out.println("WORLDLINE_M51_EMPTY=local=" + after.slot(36).empty() + ";peer=" + empty.empty());
        System.out.println("WORLDLINE_M51_PERSISTED=items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M51_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M51_SIGNATURE=" + sha256(TRACE));
    }

    private static DroppedItemMultiplayerSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void requireSpawn(RemoteDroppedItem item, PlayerPose actor) {
        double dx = item.x() - actor.x(), dy = item.y() - actor.y(), dz = item.z() - actor.z();
        double speed = item.velocityX() * item.velocityX() + item.velocityY() * item.velocityY()
                + item.velocityZ() * item.velocityZ();
        require(item.entityId() > 0 && Math.abs(dx) <= 1D && dy >= -1D && dy <= 3D
                && Math.abs(dz) <= 1D, "dropped-item position was not near actor");
        require(speed > 0D && speed < 3D, "dropped-item launch velocity drifted");
    }
    private static void requirePlayers(List<String> players, String first, String second) {
        Set<String> expected = new HashSet<>(); expected.add(first); expected.add(second);
        require(players.size() == 2 && new HashSet<>(players).equals(expected), "two-player presence drifted"); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count);
    }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
