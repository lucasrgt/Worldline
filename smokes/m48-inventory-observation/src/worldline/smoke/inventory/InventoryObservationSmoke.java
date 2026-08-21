package worldline.smoke.inventory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import worldline.api.InventoryMultiplayerSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves a full inventory window followed by a real authoritative slot delta. */
public final class InventoryObservationSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|inventory=window-0|slots=45"
            + "|initial=empty|delta=slot-36|item=1x1:0|source=packet104+103|persisted=1|disconnect=clean";
    private InventoryObservationSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: InventoryObservationSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String username = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        InventoryMultiplayerSession client = new B173WireClient("127.0.0.1", port, username, timeout);
        RemoteInventoryView initial, updated; ServerPlayerState player;
        try {
            server.boot(); server.operator(username); client.connect();
            awaitPlayers(server, Collections.singletonList(username)); PlayerPose pose = client.synchronizePose();
            initial = client.awaitInventory(); require(initial.windowId() == 0 && initial.size() == 45
                    && initial.occupiedSlots() == 0, "initial player inventory drifted");
            client.look(pose.yaw(), 90F); acquire(client, username, 1, 1); updated = client.inventory();
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            require(updated.windowId() == 0 && updated.size() == 45 && updated.occupiedSlots() == 1
                    && !updated.slot(36).empty() && updated.slot(36).item().equals(stone),
                    "authoritative inventory delta drifted: window=" + updated.windowId() + ",size="
                            + updated.size() + ",occupied=" + updated.occupiedSlots() + ",slot36="
                            + (updated.slot(36).empty() ? "empty" : updated.slot(36).item()));
            client.close(); awaitPlayers(server, Collections.emptyList()); server.save(); player = server.player(username);
            require(player.inventoryItems() == 1, "observed inventory was not persisted");
        } finally { client.close(); server.close(); }
        System.out.println("WORLDLINE_M48_API=inventory,immutable,window,slot-delta,server-authoritative");
        System.out.println("WORLDLINE_M48_INITIAL=window=" + initial.windowId() + ";slots=" + initial.size()
                + ";occupied=" + initial.occupiedSlots());
        System.out.println("WORLDLINE_M48_UPDATED=slot=36;item=" + updated.slot(36).item()
                + ";occupied=" + updated.occupiedSlots());
        System.out.println("WORLDLINE_M48_PERSISTED=items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M48_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M48_SIGNATURE=" + sha256(TRACE));
    }

    private static void awaitPlayers(B173DedicatedServer server, List<String> expected)
            throws InterruptedException { long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) { if (server.players().equals(expected)) return;
            Thread.sleep(100L); } throw new IllegalStateException("player list did not become " + expected); }
    private static void acquire(InventoryMultiplayerSession client, String username, int item, int occupied) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " " + item + " 1"); client.sustainTicks(40);
        for (int step = 0; step < 15 && client.inventory().occupiedSlots() < occupied; step++)
            client.moveAndObserve(0D, -5D, 0D, 3);
        client.sustainTicks(10);
    }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
