package worldline.b173server;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteRejectedTransaction;
import worldline.api.ServerPlayerState;

/** Proves reject ACK, full/cursor resync, and a later accepted transaction. */
public final class RejectedTransactionRecoverySmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|window=0|slot=36"
            + "|actions=1-rejected,2-accepted|stale=empty-vs-stone|reject=packet106-false"
            + "|reenable=packet106-true-immediate|resync=packet104+packet103-cursor"
            + "|slot=stone-empty-stone|cursor=empty-stone-empty|peer=stone-empty-stone"
            + "|persisted=1|disconnect=clean";
    private RejectedTransactionRecoverySmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: RejectedTransactionRecoverySmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = client(port, actorName, timeout), observer = client(port, observerName, timeout);
        RemoteRejectedTransaction rejected; RemotePersonalTransaction accepted; ServerPlayerState player;
        try {
            server.boot(); B173PlayerSeed.writeHolding(workspace, actorName, 4.5D, 60D, 4.5D, 1, 1, 0);
            B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
            actor.connect(); actor.synchronizePose();
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0); RemoteInventoryView initial = actor.awaitInventory();
            require(initial.occupiedSlots() == 1 && initial.slot(36).item().equals(stone), "recovery seed drifted");
            observer.connect(); observer.synchronizePose();
            requirePlayers(server.players(), actorName, observerName);
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            rejected = actor.rejectedTakeProbe(36);
            require(rejected.actionId() == 1 && rejected.slot() == 36 && rejected.stalePredictionEmpty()
                    && rejected.before().slot(36).item().equals(stone)
                    && rejected.authoritative().slot(36).empty() && rejected.cursorBeforeEmpty()
                    && rejected.cursorAfter().equals(stone)
                    && actor.inventory().equals(rejected.authoritative()), "rejected recovery drifted");
            worldline.test.WorldlineSmokeAwait.observe(actor,5); observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            accepted = actor.clickPersonalSlot(36);
            require(accepted.actionId() == 2 && accepted.predictedEmpty()
                    && accepted.after().slot(36).item().equals(stone) && accepted.cursorAfterEmpty(),
                    "post-recovery accepted transition drifted");
            worldline.test.WorldlineSmokeAwait.observe(actor,5); observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            require(initial.slot(36).item().equals(stone) && rejected.authoritative().slot(36).empty(),
                    "recovery snapshots mutated");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 1, "recovered inventory persistence drifted");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M56_API=rejected-personal,re-enable-ack,full-cursor-resync");
        System.out.println("WORLDLINE_M56_REJECTED=action=" + rejected.actionId()
                + ";stale=empty;slot=empty;cursor=" + rejected.cursorAfter());
        System.out.println("WORLDLINE_M56_RECOVERED=action=" + accepted.actionId()
                + ";slot=" + accepted.after().slot(36).item() + ";cursor=empty;items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M56_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M56_SIGNATURE=" + sha256(TRACE));
    }

    private static B173WireClient client(int port, String name, Duration timeout) {
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
