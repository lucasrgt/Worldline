package worldline.smoke.chesttransfer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.ChestTransferSession;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChestTransfer;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves accepted player-to-chest transfer and tile persistence across restart. */
public final class ChestTransferSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|servers=2|clients=3"
            + "|fixture=single-chest54|mapping=personal36-combined54|chest-actions=1,2-accepted"
            + "|cursor=empty-stone-empty|chest0=empty-stone|personal36=stone-empty"
            + "|close-proof=personal-action1|restart=clean-new-server|reopen=packet100+packet104"
            + "|persisted-chest0=stone|player-items=0|disconnect=clean";
    private ChestTransferSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: ChestTransferSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer first = null, second = null; ChestTransferSession actor = null, observer = null, reopened = null;
        RemoteChestTransfer transfer; RemoteContainerWindow persisted; RemoteWindowClosure firstClose, secondClose;
        BlockPosition target;
        try {
            first = server(jar, workspace, port, seed, timeout); first.boot(); first.operator(actorName);
            actor = client(port, actorName, timeout); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            actor.look(0F, 90F); PlayerPose pose = acquireChest(actor, actorName);
            RemoteWorldView baseline = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4); BlockPosition support = new BlockPosition(
                    (int) Math.floor(pose.x()), (int) Math.floor(pose.y()) - 1, (int) Math.floor(pose.z()));
            target = BlockFace.UP.adjacent(support); require(baseline.blockAt(support.x(), support.y(), support.z())
                    .legacyId() != 0 && replaceable(baseline.blockAt(target.x(), target.y(), target.z())),
                    "chest transfer anchor drifted");
            require(!actor.moveAndObserve(0D, 3D, 0D, 3).corrected(), "chest transfer clearance failed");
            observer = client(port, observerName, timeout); observer.connect(); observer.synchronizePose();
            observer.moveAndObserve(5D, 5D, 0D, 3); observer.moveAndObserve(5D, 5D, 0D, 3);
            requirePlayers(first.players(), actorName, observerName); observer.awaitRemoteChunk(
                    Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16));
            actor.placeHeldBlock(support, BlockFace.UP); BlockState chest = actor.sustainTicks(5)
                    .blockAt(target.x(), target.y(), target.z());
            require(chest.legacyId() == 54 && observer.sustainTicks(5).blockAt(target.x(), target.y(), target.z())
                    .equals(chest), "placed transfer chest diverged");
            actor.sendChat("/give " + actorName + " 1 1"); actor.sustainTicks(40);
            for (int step = 0; step < 10 && actor.inventory().occupiedSlots() < 1; step++)
                actor.moveAndObserve(0D, -1D, 0D, 2);
            actor.sustainTicks(10);
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            require(actor.inventory().slot(36).item().equals(stone), "transfer stone seed drifted");
            actor.selectHeldSlot(1); RemoteContainerWindow opened = actor.openChest(target, BlockFace.UP);
            require(opened.inventory().slot(54).item().equals(stone) && opened.inventory().slot(0).empty(),
                    "combined chest mapping drifted");
            actor.selectHeldSlot(0); observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            transfer = actor.storeInOpenChest(36, 0); require(transfer.takeAction() == 1
                    && transfer.storeAction() == 2 && transfer.stack().equals(stone)
                    && transfer.after().slot(0).item().equals(stone) && actor.inventory().slot(36).empty(),
                    "accepted chest transfer drifted");
            actor.sustainTicks(5); observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            firstClose = actor.closeWindow(); require(firstClose.closedWindow().inventory().equals(transfer.after())
                    && firstClose.proofAction() == 1, "post-transfer close proof drifted");
            actor.close(); observer.close(); awaitPlayers(first, 0); first.save();
            require(first.player(actorName).inventoryItems() == 0, "transferred player inventory persisted items");
            first.close(); first = null;
            second = server(jar, workspace, port, seed, timeout); second.boot(); second.operator(actorName);
            reopened = client(port, actorName, timeout); reopened.connect(); reopened.synchronizePose();
            require(reopened.awaitInventory().occupiedSlots() == 0, "restarted player inventory was not empty");
            require(reopened.awaitRemoteChunk(Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16))
                    .blockAt(target.x(), target.y(), target.z()).legacyId() == 54, "restarted chest block absent");
            persisted = reopened.openChest(target, BlockFace.UP); require(persisted.inventory().slot(0).item()
                    .equals(stone) && persisted.inventory().occupiedSlots() == 1, "persisted chest contents drifted");
            secondClose = reopened.closeWindow(); require(secondClose.proofAction() == 1,
                    "restarted close proof drifted"); reopened.close(); awaitPlayers(second, 0); second.save();
            require(second.player(actorName).inventoryItems() == 0, "restarted player inventory drifted");
        } finally { if (actor != null) actor.close(); if (observer != null) observer.close();
            if (reopened != null) reopened.close(); if (first != null) first.close(); if (second != null) second.close(); }
        System.out.println("WORLDLINE_M59_API=chest-transfer,combined-personal-map,restart-reopen");
        System.out.println("WORLDLINE_M59_TRANSFER=actions=" + transfer.takeAction() + "," + transfer.storeAction()
                + ";close=" + firstClose.proofAction() + ";reopen=" + persisted.inventory().slot(0).item()
                + ";close2=" + secondClose.proofAction() + ";player-items=0");
        System.out.println("WORLDLINE_M59_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M59_SIGNATURE=" + sha256(TRACE));
    }

    private static B173DedicatedServer server(Path jar, Path workspace, int port, long seed, Duration timeout) {
        return new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true); }
    private static ChestTransferSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static PlayerPose acquireChest(ChestTransferSession client, String username) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " 54 1"); client.sustainTicks(40);
        for (int step = 0; step < 100 && client.inventory().occupiedSlots() < 1; step++) client.moveAndObserve(0D, -1D, 0D, 1);
        client.sustainTicks(10); MovementOutcome settled = null; for (int step = 0; step < 100; step++) {
            settled = client.moveAndObserve(0D, -1D, 0D, 2); if (settled.corrected()) break; }
        require(settled != null && settled.corrected(), "ground settlement correction absent"); return settled.resulting(); }
    private static boolean replaceable(BlockState state) { int id = state.legacyId(); return id == 0 || id == 8 || id == 9 || id == 78; }
    private static void requirePlayers(List<String> players, String first, String second) { Set<String> expected = new HashSet<>();
        expected.add(first); expected.add(second); require(players.size() == 2 && new HashSet<>(players).equals(expected), "two-player presence drifted"); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException { long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) { if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
