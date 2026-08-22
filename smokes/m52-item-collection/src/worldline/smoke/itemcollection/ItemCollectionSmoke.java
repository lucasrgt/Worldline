package worldline.smoke.itemcollection;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.ItemCollectionMultiplayerSession;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemCollection;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves exact dropped-item collection by a separately named protocol actor. */
public final class ItemCollectionSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|spawn=packet21"
            + "|collect=packet22|collector=named-packet20|destroy=packet29"
            + "|local=packet103-stone|peer=packet5-stone|persisted=1|disconnect=clean";
    private ItemCollectionSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: ItemCollectionSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        ItemCollectionMultiplayerSession actor = client(port, actorName, timeout);
        ItemCollectionMultiplayerSession observer = client(port, observerName, timeout);
        RemoteDroppedItem dropped; RemoteItemCollection collection; RemoteInventoryView restored;
        RemoteHeldItem peer; ServerPlayerState player;
        try {
            server.boot(); B173PlayerSeed.writeHolding(workspace, actorName, 4.5D, 60D, 4.5D, 1, 1, 0);
            B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
            actor.connect(); actor.synchronizePose(); actor.awaitInventory();
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            require(actor.inventory().occupiedSlots() == 1 && actor.inventory().slot(36).item().equals(stone),
                    "held seed drifted");
            observer.connect(); observer.synchronizePose(); requirePlayers(server.players(), actorName, observerName);
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            actor.look(0F, 90F); actor.dropHeldItem();
            dropped = observer.awaitDroppedItem(stone); actor.sustainTicks(10);
            require(actor.inventory().occupiedSlots() == 0 && actor.inventory().slot(36).empty(),
                    "actor inventory did not empty after drop");
            observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            for (int step = 0; step < 40 && actor.inventory().occupiedSlots() == 0; step++)
                actor.moveAndObserve(0D, -.5D, 0D, 1);
            actor.sustainTicks(10); require(actor.inventory().occupiedSlots() == 1,
                    "actor did not descend through dropped item");
            collection = observer.awaitItemCollection(dropped, actorName);
            require(collection.collectorEntityId() == actor.state().entityId()
                    && collection.droppedItem().equals(dropped), "named collection correlation drifted");
            restored = actor.inventory(); require(restored.occupiedSlots() == 1
                    && restored.slot(36).item().equals(stone), "collected stone did not restore actor inventory");
            peer = observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 1, "collected inventory was not persisted");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M52_API=item-collection,packet21+22+29,named-collector,terminal");
        System.out.println("WORLDLINE_M52_COLLECTION=" + collection);
        System.out.println("WORLDLINE_M52_RESTORED=" + restored.slot(36).item() + ";peer=" + peer);
        System.out.println("WORLDLINE_M52_PERSISTED=items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M52_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M52_SIGNATURE=" + sha256(TRACE));
    }

    private static ItemCollectionMultiplayerSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
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
