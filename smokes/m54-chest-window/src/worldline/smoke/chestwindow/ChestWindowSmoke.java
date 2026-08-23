package worldline.smoke.chestwindow;

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
import worldline.api.ChestWindowMultiplayerSession;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWindowKind;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves one official single-chest Packet100/104 descriptor and empty view. */
public final class ChestWindowSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|block=chest54"
            + "|activate=packet15-empty|open=packet100-readUTF|kind=chest|title=Chest"
            + "|owned=27|total=63|contents=empty|full=packet104|disconnect=clean";
    private ChestWindowSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: ChestWindowSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        ChestWindowMultiplayerSession actor = client(port, actorName, timeout);
        ChestWindowMultiplayerSession observer = client(port, observerName, timeout);
        BlockPosition support, target; BlockState chestState; RemoteContainerWindow window;
        try {
            server.boot(); server.operator(actorName); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            observer.connect(); observer.synchronizePose(); observer.moveAndObserve(0D, 10D, 0D, 3);
            requirePlayers(server.players(), actorName, observerName);
            actor.look(0F, 90F); PlayerPose pose = acquire(actor, actorName);
            RemoteItemStack chest = new RemoteItemStack(54, 1, 0);
            require(actor.inventory().occupiedSlots() == 1 && actor.inventory().slot(36).item().equals(chest),
                    "chest seed drifted");
            RemoteWorldView baseline = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4);
            support = new BlockPosition((int) Math.floor(pose.x()), (int) Math.floor(pose.y()) - 1,
                    (int) Math.floor(pose.z())); target = BlockFace.UP.adjacent(support);
            BlockState supportState = baseline.blockAt(support.x(), support.y(), support.z());
            BlockState targetState = baseline.blockAt(target.x(), target.y(), target.z());
            require(supportState.legacyId() != 0 && replaceable(targetState), "chest anchor drifted");
            MovementOutcome clearance = actor.moveAndObserve(0D, 3D, 0D, 3);
            require(!clearance.corrected() && clearance.resulting().y() >= pose.y() + 2.9D,
                    "chest target clearance failed");
            observer.awaitRemoteChunk(Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16));
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 54, 0));
            actor.placeHeldBlock(support, BlockFace.UP);
            RemoteWorldView actorWorld = worldline.test.WorldlineSmokeAwait.awaitWorld(actor,view->view.blockAt(target.x(),target.y(),target.z()).legacyId()==54,"actor chest placement",5), observerWorld = worldline.test.WorldlineSmokeAwait.awaitWorld(observer,view->view.blockAt(target.x(),target.y(),target.z()).legacyId()==54,"observer chest placement",5);
            chestState = actorWorld.blockAt(target.x(), target.y(), target.z());
            require(chestState.legacyId() == 54
                    && observerWorld.blockAt(target.x(), target.y(), target.z()).equals(chestState),
                    "placed chest state diverged across clients");
            require(actor.inventory().occupiedSlots() == 0,
                    "placed chest did not leave actor inventory");
            observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            window = actor.openChest(target, BlockFace.UP);
            RemoteWindowDescriptor descriptor = window.descriptor();
            require(descriptor.kind() == RemoteWindowKind.CHEST && "Chest".equals(descriptor.title())
                    && descriptor.containerSlots() == 27, "chest descriptor drifted");
            require(window.inventory().size() == 63 && window.inventory().occupiedSlots() == 0,
                    "empty chest full-window view drifted");
            actor.close(); observer.close(); awaitPlayers(server, 0);
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M54_API=chest-window,packet100-readUTF,packet104,immutable");
        System.out.println("WORLDLINE_M54_TARGET=" + target + ";state=" + chestState);
        System.out.println("WORLDLINE_M54_WINDOW=id=" + window.descriptor().windowId() + ";kind="
                + window.descriptor().kind() + ";title=" + window.descriptor().title() + ";owned="
                + window.descriptor().containerSlots() + ";total=" + window.inventory().size()
                + ";occupied=" + window.inventory().occupiedSlots());
        System.out.println("WORLDLINE_M54_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M54_SIGNATURE=" + sha256(TRACE));
    }

    private static ChestWindowMultiplayerSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static PlayerPose acquire(ChestWindowMultiplayerSession client, String username) {
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " 54 1");
        worldline.test.WorldlineSmokeAwait.awaitEntity(client,()->{client.moveAndObserve(0D,-1D,0D,1);
            return client.inventory();},inventory->inventory.occupiedSlots()>=1,"chest grant",100);
        worldline.test.WorldlineSmokeAwait.observe(client,10); MovementOutcome settled = null;
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
