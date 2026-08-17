package worldline.smoke.helditem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.HeldItemMultiplayerSession;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves held-slot selection through an independent peer equipment update. */
public final class HeldItemPeerSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|inventory=stone,dirt"
            + "|selection=hotbar-1|wire=packet16|peer=packet20+5|held=dirt:0|persisted=2|disconnect=clean";
    private HeldItemPeerSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: HeldItemPeerSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        HeldItemMultiplayerSession actor = client(port, actorName, timeout);
        HeldItemMultiplayerSession observer = client(port, observerName, timeout);
        RemoteInventoryView inventory; RemoteHeldItem held; ServerPlayerState player;
        try {
            server.boot(); server.operator(actorName); actor.connect(); actor.synchronizePose();
            inventory = actor.awaitInventory(); require(inventory.occupiedSlots() == 0, "actor inventory was not empty");
            actor.look(0F, 90F);
            acquire(actor, actorName, 1, 1); acquire(actor, actorName, 3, 2); inventory = actor.inventory();
            require(inventory.occupiedSlots() == 2
                    && inventory.slot(36).item().equals(new RemoteItemStack(1, 1, 0))
                    && inventory.slot(37).item().equals(new RemoteItemStack(3, 1, 0)),
                    "hotbar seed drifted: occupied=" + inventory.occupiedSlots() + ",slot36="
                            + slot(inventory, 36) + ",slot37=" + slot(inventory, 37));
            observer.connect(); observer.synchronizePose(); requirePlayers(server.players(), actorName, observerName);
            actor.selectHeldSlot(1); RemoteHeldItem expected = new RemoteHeldItem(actorName, 3, 0);
            held = observer.awaitPeerHeldItem(expected); require(held.equals(expected), "peer held item drifted");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 2, "selected inventory was not persisted");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M49_API=held-slot,packet16,named-peer,packet5,server-authoritative");
        System.out.println("WORLDLINE_M49_HOTBAR=slot36=" + inventory.slot(36).item()
                + ";slot37=" + inventory.slot(37).item());
        System.out.println("WORLDLINE_M49_PEER=" + held);
        System.out.println("WORLDLINE_M49_PERSISTED=items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M49_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M49_SIGNATURE=" + sha256(TRACE));
    }

    private static HeldItemMultiplayerSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static String slot(RemoteInventoryView inventory, int index) {
        return inventory.slot(index).empty() ? "empty" : inventory.slot(index).item().toString(); }
    private static void acquire(HeldItemMultiplayerSession client, String username, int item, int occupied) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " " + item + " 1"); client.sustainTicks(40);
        for (int step = 0; step < 15 && client.inventory().occupiedSlots() < occupied; step++)
            client.moveAndObserve(0D, -5D, 0D, 3);
        client.sustainTicks(10);
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
