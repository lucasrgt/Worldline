package worldline.smoke.chestretrieval;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.ChestRetrievalSession;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChestRetrieval;
import worldline.api.RemoteChestTransfer;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves exact chest-to-player retrieval and both final states after restart. */
public final class ChestRetrievalSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|servers=2|client-sessions=2"
            + "|fixture=single-chest54|seed=store-actions1,2|reopen=same-server"
            + "|retrieve=chest0-to-personal36|actions=1,2-accepted"
            + "|cursor=empty-stone-empty|combined=chest63+personal45"
            + "|close-proofs=personal-actions1,2|restart=clean-new-server"
            + "|final=chest0-empty+personal36-stone|persisted=1|disconnect=clean";
    private ChestRetrievalSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: ChestRetrievalSmoke server.jar workspace port seed actor");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String name = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer first = null, second = null; ChestRetrievalSession actor = null, restored = null;
        RemoteChestRetrieval retrieval; RemoteWindowClosure storeClose, retrievalClose, restartClose;
        BlockPosition target;
        try {
            first = server(jar, workspace, port, seed, timeout); first.boot(); first.operator(name);
            actor = client(port, name, timeout); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            actor.look(0F, 90F); PlayerPose pose = acquireChest(actor, name);
            RemoteWorldView baseline = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4); BlockPosition support = new BlockPosition(
                    (int) Math.floor(pose.x()), (int) Math.floor(pose.y()) - 1, (int) Math.floor(pose.z()));
            target = BlockFace.UP.adjacent(support); require(baseline.blockAt(support.x(), support.y(), support.z())
                    .legacyId() != 0 && replaceable(baseline.blockAt(target.x(), target.y(), target.z())),
                    "chest retrieval anchor drifted");
            require(!actor.moveAndObserve(0D, 3D, 0D, 3).corrected(), "chest retrieval clearance failed");
            actor.placeHeldBlock(support, BlockFace.UP); BlockState chest = worldline.test.WorldlineSmokeAwait.observe(actor,5)
                    .blockAt(target.x(), target.y(), target.z()); require(chest.legacyId() == 54,
                    "retrieval chest placement drifted");
            actor.sendChat("/give " + name + " 1 1");
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            worldline.test.WorldlineSmokeAwait.awaitSlot(actor,actor::inventory,36,stone,40);
            actor.selectHeldSlot(1); RemoteContainerWindow opened = actor.openChest(target, BlockFace.UP);
            require(opened.inventory().slot(54).item().equals(stone) && opened.inventory().slot(0).empty(),
                    "initial chest mapping drifted");
            RemoteChestTransfer stored = actor.storeInOpenChest(36, 0); require(stored.takeAction() == 1
                    && stored.storeAction() == 2 && stored.after().slot(0).item().equals(stone),
                    "retrieval fixture store drifted");
            storeClose = actor.closeWindow(); require(storeClose.proofAction() == 1,
                    "fixture store close proof drifted");
            RemoteContainerWindow reopened = actor.openChest(target, BlockFace.UP); require(reopened.inventory()
                    .slot(0).item().equals(stone) && reopened.inventory().slot(54).empty(),
                    "same-server chest reopen drifted");
            retrieval = actor.retrieveFromOpenChest(0, 36); require(retrieval.takeAction() == 1
                    && retrieval.storeAction() == 2 && retrieval.before().equals(reopened.inventory())
                    && retrieval.after().slot(0).empty() && retrieval.after().slot(54).item().equals(stone)
                    && actor.inventory().slot(36).item().equals(stone), "accepted chest retrieval drifted");
            retrievalClose = actor.closeWindow(); require(retrievalClose.proofAction() == 2
                    && retrievalClose.closedWindow().inventory().equals(retrieval.after()),
                    "post-retrieval close proof drifted");
            actor.close(); actor = null; awaitPlayers(first, 0); first.save();
            require(first.player(name).inventoryItems() == 1, "retrieved player item was not persisted");
            first.close(); first = null;
            second = server(jar, workspace, port, seed, timeout); second.boot(); second.operator(name);
            restored = client(port, name, timeout); restored.connect(); restored.synchronizePose();
            require(restored.awaitInventory().slot(36).item().equals(stone), "restored player stone drifted");
            restored.selectHeldSlot(1); RemoteContainerWindow finalChest = restored.openChest(target, BlockFace.UP);
            require(finalChest.inventory().slot(0).empty() && finalChest.inventory().slot(54).item().equals(stone)
                    && finalChest.inventory().occupiedSlots() == 1, "restored chest/player state drifted");
            restartClose = restored.closeWindow(); require(restartClose.proofAction() == 1,
                    "restart close proof drifted"); restored.close(); restored = null; awaitPlayers(second, 0);
            second.save(); require(second.player(name).inventoryItems() == 1, "restart inventory count drifted");
        } finally { if (actor != null) actor.close(); if (restored != null) restored.close();
            if (first != null) first.close(); if (second != null) second.close(); }
        System.out.println("WORLDLINE_M67_API=chest-retrieval,dual-view-commit,restart-reopen");
        System.out.println("WORLDLINE_M67_RETRIEVAL=actions=" + retrieval.takeAction() + ","
                + retrieval.storeAction() + ";close=" + retrievalClose.proofAction()
                + ";restart-close=" + restartClose.proofAction() + ";player-items=1");
        System.out.println("WORLDLINE_M67_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M67_SIGNATURE=" + sha256(TRACE));
    }

    private static B173DedicatedServer server(Path jar, Path workspace, int port, long seed, Duration timeout) {
        return new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true); }
    private static ChestRetrievalSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static PlayerPose acquireChest(ChestRetrievalSession client, String name) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + name + " 54 1");
        worldline.test.WorldlineSmokeAwait.awaitEntity(client,()->{client.moveAndObserve(0D,-1D,0D,1);
            return client.inventory();},inventory->inventory.occupiedSlots()>=1,"retrieval chest grant",100);
        worldline.test.WorldlineSmokeAwait.observe(client,10); MovementOutcome settled = null; for (int step = 0; step < 100; step++) {
            settled = client.moveAndObserve(0D, -1D, 0D, 2); if (settled.corrected()) break; }
        require(settled != null && settled.corrected(), "ground settlement correction absent"); return settled.resulting(); }
    private static boolean replaceable(BlockState state) { int id = state.legacyId();
        return id == 0 || id == 8 || id == 9 || id == 78; }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
