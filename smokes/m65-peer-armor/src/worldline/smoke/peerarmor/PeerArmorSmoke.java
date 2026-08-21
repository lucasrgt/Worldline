package worldline.smoke.peerarmor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.ArmorEquipmentSession;
import worldline.api.RemoteArmorEquip;
import worldline.api.RemoteArmorPiece;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;
import worldline.b173server.B173ArmorPacketFixture;

/** Proves full leather equipment through accepted clicks and independent Packet5 observations. */
public final class PeerArmorSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|servers=2|clients=4"
            + "|fixture=leather298,299,300,301|window0=5,6,7,8"
            + "|actions=1,2,3,4,5,6,7,8-accepted|cursor=empty-after-each-pair"
            + "|packet5=4:298,3:299,2:300,1:301|damage=0"
            + "|restart=window0+packet5-bootstrap|persisted=4|disconnect=clean";
    private PeerArmorSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: PeerArmorSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        ServerPlayerState persisted; int action = 1;
        B173DedicatedServer first = server(jar, workspace, port, seed, timeout);
        ArmorEquipmentSession actor = client(port, actorName, timeout), observer = client(port, observerName, timeout);
        try {
            B173ArmorPacketFixture.verify(); first.boot(); first.operator(actorName); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            actor.look(0F, 90F); for (RemoteArmorSlot slot : RemoteArmorSlot.values())
                acquire(actor, actorName, slot.leatherItemId());
            require(actor.inventory().occupiedSlots() == 4, "leather fixture count drifted");
            observer.connect(); observer.synchronizePose(); observer.moveAndObserve(0D, 10D, 0D, 3);
            for (RemoteArmorSlot slot : RemoteArmorSlot.values()) {
                RemoteItemStack stack = new RemoteItemStack(slot.leatherItemId(), 1, 0);
                int source = find(actor.inventory(), stack); require(source == 36 + slot.ordinal(), "leather source drifted");
                RemoteArmorEquip equipped = actor.equipLeatherArmor(source, slot);
                require(equipped.takeAction() == action && equipped.placeAction() == action + 1
                        && equipped.after().slot(slot.containerSlot()).item().equals(stack), "armor equip drifted");
                RemoteArmorPiece peer = observer.awaitPeerArmor(
                        new RemoteArmorPiece(actorName, slot, slot.leatherItemId(), 0));
                require(peer.slot() == slot && peer.legacyId() == slot.leatherItemId(), "peer armor drifted");
                action += 2;
            }
            requireArmor(actor.inventory()); actor.close(); observer.close(); awaitPlayers(first, 0);
            first.save(); persisted = first.player(actorName); require(persisted.inventoryItems() == 4,
                    "leather armor persistence count drifted");
        } finally { actor.close(); observer.close(); first.close(); }
        B173DedicatedServer second = server(jar, workspace, port, seed, timeout);
        ArmorEquipmentSession restored = client(port, actorName, timeout), witness = client(port, observerName, timeout);
        try {
            second.boot(); restored.connect(); restored.synchronizePose(); requireArmor(restored.awaitInventory());
            witness.connect(); witness.synchronizePose();
            for (RemoteArmorSlot slot : RemoteArmorSlot.values()) witness.awaitPeerArmor(
                    new RemoteArmorPiece(actorName, slot, slot.leatherItemId(), 0));
            restored.close(); witness.close(); awaitPlayers(second, 0); second.save();
            require(second.player(actorName).inventoryItems() == 4, "restored leather armor count drifted");
        } finally { restored.close(); witness.close(); second.close(); }
        System.out.println("WORLDLINE_M65_API=leather-armor,personal-transactions,peer-packet5,restart");
        System.out.println("WORLDLINE_M65_ARMOR=actions=1..8;window=5:298,6:299,7:300,8:301;packet5=4:298,3:299,2:300,1:301;items=" + persisted.inventoryItems());
        System.out.println("WORLDLINE_M65_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M65_SIGNATURE=" + sha256(TRACE));
    }

    private static B173DedicatedServer server(Path jar, Path workspace, int port, long seed, Duration timeout) {
        return new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true); }
    private static ArmorEquipmentSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void acquire(ArmorEquipmentSession client, String username, int item) {
        int occupied = client.inventory().occupiedSlots() + 1;
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " " + item + " 1"); client.sustainTicks(40);
        for (int step = 0; step < 15 && client.inventory().occupiedSlots() < occupied; step++)
            client.moveAndObserve(0D, -5D, 0D, 3); client.sustainTicks(10);
    }
    private static int find(RemoteInventoryView view, RemoteItemStack expected) { for (int slot = 9; slot <= 44; slot++)
        if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected)) return slot; return -1; }
    private static void requireArmor(RemoteInventoryView view) { for (RemoteArmorSlot slot : RemoteArmorSlot.values())
        require(!view.slot(slot.containerSlot()).empty() && view.slot(slot.containerSlot()).item()
                .equals(new RemoteItemStack(slot.leatherItemId(), 1, 0)), "leather armor window drifted"); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
