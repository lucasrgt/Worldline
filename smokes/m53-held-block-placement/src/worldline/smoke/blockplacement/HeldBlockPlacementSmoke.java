package worldline.smoke.blockplacement;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockFace;
import worldline.api.BlockPlacementMultiplayerSession;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves one server-authoritative held-stone placement through two remote caches. */
public final class HeldBlockPlacementSmoke {
    private static final BlockState STONE = new BlockState(1, 0);
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|held=stone"
            + "|action=packet16+15|face=up|target=replaceable|world=packet53-stone|caches=2-immutable"
            + "|local=packet103-empty|peer=packet5-empty|persisted=0|disconnect=clean";
    private HeldBlockPlacementSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: HeldBlockPlacementSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        BlockPlacementMultiplayerSession actor = client(port, actorName, timeout);
        BlockPlacementMultiplayerSession observer = client(port, observerName, timeout);
        BlockPosition support, target; BlockState beforeState;
        RemoteWorldView actorBefore, observerBefore, actorAfter, observerAfter;
        ServerPlayerState player;
        try {
            server.boot(); server.operator(actorName); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            observer.connect(); observer.synchronizePose(); observer.moveAndObserve(0D, 10D, 0D, 3);
            requirePlayers(server.players(), actorName, observerName);
            actor.look(0F, 90F); PlayerPose pose = acquire(actor, actorName);
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            require(actor.inventory().occupiedSlots() == 1 && actor.inventory().slot(36).item().equals(stone),
                    "held seed drifted");
            actorBefore = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4);
            support = new BlockPosition((int) Math.floor(pose.x()), (int) Math.floor(pose.y()) - 1,
                    (int) Math.floor(pose.z())); target = BlockFace.UP.adjacent(support);
            BlockState supportState = actorBefore.blockAt(support.x(), support.y(), support.z());
            beforeState = actorBefore.blockAt(target.x(), target.y(), target.z());
            require(supportState.legacyId() != 0, "settled support was empty: " + supportState);
            require(replaceable(beforeState), "settled target was not replaceable: " + beforeState);
            MovementOutcome clearance = actor.moveAndObserve(0D, 3D, 0D, 3);
            require(!clearance.corrected() && clearance.resulting().y() >= pose.y() + 2.9D,
                    "placement target clearance failed");
            observerBefore = observer.awaitRemoteChunk(Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16));
            require(observerBefore.blockAt(target.x(), target.y(), target.z()).equals(beforeState),
                    "observer target baseline drifted");
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0)); actor.placeHeldBlock(support, BlockFace.UP);
            actorAfter = actor.awaitBlock(target, STONE); observerAfter = observer.awaitBlock(target, STONE);
            actor.sustainTicks(5); require(actor.inventory().occupiedSlots() == 0
                    && actor.inventory().slot(36).empty(), "placed stack did not leave actor inventory");
            observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            require(actorBefore.blockAt(target.x(), target.y(), target.z()).equals(beforeState)
                    && observerBefore.blockAt(target.x(), target.y(), target.z()).equals(beforeState),
                    "immutable placement snapshots changed");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 0, "placed inventory was persisted as occupied");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M53_API=held-block,packet15,packet53,two-caches,server-authoritative");
        System.out.println("WORLDLINE_M53_TARGET=" + target + ";support=" + support + ";before=" + beforeState);
        System.out.println("WORLDLINE_M53_WORLD=actor=" + actorAfter.blockAt(target.x(), target.y(), target.z())
                + ";observer=" + observerAfter.blockAt(target.x(), target.y(), target.z()));
        System.out.println("WORLDLINE_M53_PERSISTED=items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M53_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M53_SIGNATURE=" + sha256(TRACE));
    }

    private static BlockPlacementMultiplayerSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static PlayerPose acquire(BlockPlacementMultiplayerSession client, String username) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " 1 1"); client.sustainTicks(40);
        for (int step = 0; step < 100 && client.inventory().occupiedSlots() < 1; step++)
            client.moveAndObserve(0D, -1D, 0D, 1);
        client.sustainTicks(10); MovementOutcome settled = null;
        for (int step = 0; step < 100; step++) { settled = client.moveAndObserve(0D, -1D, 0D, 2);
            if (settled.corrected()) break; }
        require(settled != null && settled.corrected(), "ground settlement correction absent");
        return settled.resulting();
    }
    private static boolean replaceable(BlockState state) {
        int id = state.legacyId(); return id == 0 || id == 8 || id == 9 || id == 78; }
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
