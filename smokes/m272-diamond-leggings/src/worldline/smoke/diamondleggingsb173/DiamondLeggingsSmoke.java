package worldline.smoke.diamondleggingsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteArmorPiece;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173DiamondLeggingsClick;
import worldline.b173server.B173DiamondLeggingsPacketFixture;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Equips diamond leggings 312 into window slot 7 and proves peer Packet5 slot 2. */
public final class DiamondLeggingsSmoke {
    private static final int ITEM = 312, LEATHER = 300, SOURCE = 36, WINDOW = 7, PACKET5 = 2;
    private DiamondLeggingsSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: DiamondLeggingsSmoke server.jar workspace port seed username observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        RemoteItemStack stack = new RemoteItemStack(ITEM, 1, 0); RemoteArmorSlot legs = RemoteArmorSlot.LEGGINGS;
        B173DiamondLeggingsPacketFixture.verify();
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout);
        B173WireClient observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
        B173WireClient restored = null, witness = null; ServerPlayerState persisted;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 70D, 4.5D,
                    new int[] {0}, new int[] {ITEM}, new int[] {1}, new int[] {0});
            B173PlayerSeed.write(workspace, observerName, 4.5D, 70D, 4.5D);
            actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 1
                    && actor.inventory().slot(SOURCE).item().equals(stack)
                    && actor.inventory().slot(WINDOW).empty(), "diamond-leggings seed drifted");
            observer.connect(); observer.synchronizePose(); observer.moveAndObserve(0D, 10D, 0D, 3);
            B173DiamondLeggingsClick.apply(actor, SOURCE);
            require(actor.inventory().slot(WINDOW).item().equals(stack)
                    && actor.inventory().slot(WINDOW).item().legacyId() != LEATHER
                    && actor.inventory().slot(SOURCE).empty(), "diamond-leggings equip drifted");
            RemoteArmorPiece peer = observer.awaitPeerArmor(new RemoteArmorPiece(actorName, legs, ITEM, 0));
            require(peer.slot() == legs && peer.legacyId() == ITEM && peer.legacyId() != LEATHER
                    && peer.slot().equipmentSlot() == PACKET5, "peer diamond-leggings Packet5 drifted");
            requireArmor(actor.inventory(), stack); actor.close(); observer.close(); awaitPlayers(server, 0);
            server.save(); persisted = server.player(actorName);
            require(persisted.inventoryItems() == 1, "diamond-leggings persistence count drifted");
            restored = new B173WireClient("127.0.0.1", port, actorName, timeout);
            witness = new B173WireClient("127.0.0.1", port, observerName, timeout);
            restored.connect(); restored.synchronizePose(); requireArmor(restored.awaitInventory(), stack);
            witness.connect(); witness.synchronizePose();
            witness.awaitPeerArmor(new RemoteArmorPiece(actorName, legs, ITEM, 0));
            restored.close(); witness.close(); awaitPlayers(server, 0); server.save();
            require(server.player(actorName).inventoryItems() == 1, "restored diamond-leggings count drifted");
            String evidence = "window=" + WINDOW + ":" + ITEM + ",packet5=" + PACKET5 + ":" + ITEM
                    + ",leather=" + LEATHER + ",distinct=true,actions=1,2,persisted=true,clients=4,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=diamond-leggings312|cause=packet102-window0-slot36-to-slot7"
                    + "|wire=packet5-slot2-item312|oracle=live-equip+peer-packet5+fresh-login|" + evidence;
            System.out.println("WORLDLINE_M272_LEGGINGS=" + evidence);
            System.out.println("WORLDLINE_M272_TRACE=" + trace);
            System.out.println("WORLDLINE_M272_SIGNATURE=" + sha256(trace));
        } finally {
            actor.close(); observer.close();
            if (restored != null) restored.close();
            if (witness != null) witness.close();
            server.close();
        }
    }

    private static void requireArmor(RemoteInventoryView view, RemoteItemStack stack) {
        require(!view.slot(WINDOW).empty() && view.slot(WINDOW).item().equals(stack)
                && view.slot(WINDOW).item().legacyId() != LEATHER
                && view.slot(SOURCE).empty() && view.occupiedSlots() == 1, "diamond-leggings window drifted");
    }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count);
    }
    private static String sha256(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message); }
}
