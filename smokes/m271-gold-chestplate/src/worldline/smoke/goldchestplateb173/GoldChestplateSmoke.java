package worldline.smoke.goldchestplateb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteArmorPiece;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173GoldChestplateEquip;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Equips gold chestplate 315 into armor slot 6 and proves peer Packet5 slot 3. */
public final class GoldChestplateSmoke {
    private static final RemoteItemStack GOLD = new RemoteItemStack(315, 1, 0);
    private GoldChestplateSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: GoldChestplateSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173GoldChestplateEquip.verify();
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout);
        B173WireClient observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
        B173WireClient restored = null, witness = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D,
                    new int[] {0}, new int[] {315}, new int[] {1}, new int[] {0});
            B173PlayerSeed.write(workspace, observerName, 4.5D, 60D, 4.5D);
            actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 1 && actor.inventory().slot(36).item().equals(GOLD)
                    && actor.inventory().slot(6).empty(), "gold chestplate seed drifted");
            observer.connect(); observer.synchronizePose(); observer.moveAndObserve(0D, 0D, 0D, 3);
            B173GoldChestplateEquip.apply(actor); requireChest(actor.inventory());
            RemoteArmorPiece peer = observer.awaitPeerArmor(
                    new RemoteArmorPiece(actorName, RemoteArmorSlot.CHESTPLATE, 315, 0));
            require(peer.legacyId() == 315 && peer.slot().containerSlot() == 6
                    && peer.slot().equipmentSlot() == 3, "peer gold chestplate drifted");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save();
            require(server.player(actorName).inventoryItems() == 1, "gold chestplate NBT count drifted");
            restored = new B173WireClient("127.0.0.1", port, actorName, timeout);
            witness = new B173WireClient("127.0.0.1", port, observerName, timeout);
            restored.connect(); restored.synchronizePose(); requireChest(restored.awaitInventory());
            witness.connect(); witness.synchronizePose();
            witness.awaitPeerArmor(new RemoteArmorPiece(actorName, RemoteArmorSlot.CHESTPLATE, 315, 0));
            restored.close(); witness.close(); awaitPlayers(server, 0); server.save();
            String evidence = "window=6:315,packet5=3:315,distinct-from=299,307,persisted=true,clients=4,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=gold-chestplate315|cause=packet102-window0-slot36-to-6"
                    + "|wire=packet5-slot3:315|oracle=live-window6+peer-packet5+fresh-login|" + evidence;
            System.out.println("WORLDLINE_M271_CHESTPLATE=" + evidence);
            System.out.println("WORLDLINE_M271_TRACE=" + trace);
            System.out.println("WORLDLINE_M271_SIGNATURE=" + sha(trace));
        } finally {
            actor.close(); observer.close();
            if (restored != null) restored.close();
            if (witness != null) witness.close();
            server.close();
        }
    }

    private static void requireChest(RemoteInventoryView view) {
        require(!view.slot(6).empty() && view.slot(6).item().equals(GOLD)
                && view.slot(6).item().legacyId() != 299 && view.slot(6).item().legacyId() != 307
                && view.slot(36).empty() && view.occupiedSlots() == 1, "gold chestplate window drifted");
    }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count);
    }
    private static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message); }
}
