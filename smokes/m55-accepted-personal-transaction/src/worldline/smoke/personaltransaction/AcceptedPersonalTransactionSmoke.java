package worldline.smoke.personaltransaction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.PersonalInventoryTransactionSession;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves two accepted left clicks through exact predictions, ACKs, and a peer. */
public final class AcceptedPersonalTransactionSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|window=0|slot=36"
            + "|button=left|shift=false|actions=1,2|acks=packet106-accepted"
            + "|slot=stone-empty-stone|cursor=empty-stone-empty"
            + "|peer=stone-empty-stone|persisted=1|disconnect=clean";
    private AcceptedPersonalTransactionSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: AcceptedPersonalTransactionSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        PersonalInventoryTransactionSession actor = client(port, actorName, timeout);
        PersonalInventoryTransactionSession observer = client(port, observerName, timeout);
        RemotePersonalTransaction take, place; ServerPlayerState player;
        try {
            server.boot(); server.operator(actorName); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            observer.connect(); observer.synchronizePose(); observer.moveAndObserve(0D, 10D, 0D, 3);
            requirePlayers(server.players(), actorName, observerName);
            actor.look(0F, 90F); acquire(actor, actorName); RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            RemoteInventoryView initial = actor.inventory();
            require(initial.occupiedSlots() == 1 && !initial.slot(36).empty()
                            && initial.slot(36).item().equals(stone),
                    "personal transaction seed drifted: occupied=" + initial.occupiedSlots()
                            + ",slot36=" + (initial.slot(36).empty() ? "empty" : initial.slot(36).item()));
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            take = actor.clickPersonalSlot(36);
            require(take.actionId() == 1 && take.slot() == 36 && take.predicted().equals(stone)
                    && take.cursorBeforeEmpty() && take.cursorAfter().equals(stone)
                    && take.after().slot(36).empty() && actor.inventory().equals(take.after()),
                    "accepted take transition drifted");
            actor.sustainTicks(5); observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            place = actor.clickPersonalSlot(36);
            require(place.actionId() == 2 && place.slot() == 36 && place.predictedEmpty()
                    && place.cursorBefore().equals(stone) && place.cursorAfterEmpty()
                    && place.after().slot(36).item().equals(stone) && actor.inventory().equals(place.after()),
                    "accepted place transition drifted");
            actor.sustainTicks(5); observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            require(initial.slot(36).item().equals(stone) && take.after().slot(36).empty(),
                    "personal transaction snapshots mutated");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 1, "accepted personal transaction persistence drifted");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M55_API=personal-left-click,packet102,packet106,optimistic-commit");
        System.out.println("WORLDLINE_M55_ACTIONS=" + take.actionId() + "," + place.actionId()
                + ";predicted=" + take.predicted() + ",empty;cursor=empty,stone,empty");
        System.out.println("WORLDLINE_M55_STATE=slot36=stone,empty,stone;peer=stone,empty,stone;items="
                + player.inventoryItems());
        System.out.println("WORLDLINE_M55_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M55_SIGNATURE=" + sha256(TRACE));
    }

    private static PersonalInventoryTransactionSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void acquire(PersonalInventoryTransactionSession client, String username) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " 1 1"); client.sustainTicks(40);
        for (int step = 0; step < 15 && client.inventory().occupiedSlots() < 1; step++)
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
