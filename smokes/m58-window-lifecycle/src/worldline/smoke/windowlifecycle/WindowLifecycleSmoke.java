package worldline.smoke.windowlifecycle;

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
import worldline.api.MovementOutcome;
import worldline.api.PersonalCraftingSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteWorldView;
import worldline.api.RemoteWindowClosure;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatWeather;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves explicit Packet101 close and restored personal-window transactions. */
public final class WindowLifecycleSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|clients=2|fixture=placed-chest54"
            + "|open=packet15+packet100+packet104|close=packet101-tracked-id|duplicate=fail-closed"
            + "|proof=packet102-window0-action1-accepted|post-close=packet103-window0"
            + "|personal=packet102-actions2,3-accepted"
            + "|peer=stone-empty-stone|persisted=1|disconnect=clean";
    private WindowLifecycleSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: WindowLifecycleSmoke server.jar workspace port seed actor observer");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4], observerName = arguments[5]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        PersonalCraftingSession actor = client(port, actorName, timeout), observer = client(port, observerName, timeout);
        RemoteContainerWindow window; RemoteWindowClosure closure;
        RemotePersonalTransaction take, restore; ServerPlayerState player;
        BlockPosition target;
        try {
            server.boot(); server.save(); server.operator(actorName);
            B173LevelDatWeather.Weather world = B173LevelDatWeather.read(workspace.resolve("world/level.dat"));
            double x = world.spawnX() + 0.5D, y = world.spawnY() + 20D, z = world.spawnZ() + 0.5D;
            B173PlayerSeed.writeHolding(workspace, actorName, x, y, z, 54, 1, 0);
            B173PlayerSeed.write(workspace, observerName, x + 3D, y, z);
            actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 1
                    && actor.inventory().slot(36).item().equals(new RemoteItemStack(54, 1, 0)),
                    "actor chest seed drifted");
            actor.look(0F, 90F); PlayerPose pose = settle(actor);
            RemoteWorldView baseline = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4);
            BlockPosition support = placement(baseline, pose); target = BlockFace.UP.adjacent(support);
            observer.connect(); observer.synchronizePose(); requirePlayers(server.players(), actorName, observerName);
            observer.awaitRemoteChunk(Math.floorDiv(target.x(), 16), Math.floorDiv(target.z(), 16));
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 54, 0)); actor.placeHeldBlock(support, BlockFace.UP);
            actor.awaitBlock(target, new BlockState(54, 0));
            BlockState chest = worldline.test.WorldlineSmokeAwait.observe(actor,5).blockAt(target.x(), target.y(), target.z());
            require(chest.legacyId() == 54 && worldline.test.WorldlineSmokeAwait.observe(observer,5).blockAt(target.x(), target.y(), target.z())
                    .equals(chest), "placed lifecycle chest diverged");
            window = actor.openChest(target, BlockFace.UP); require(window.inventory().size() == 63
                    && window.inventory().occupiedSlots() == 0, "lifecycle chest open drifted");
            closure = actor.closeWindow(); require(closure.closedWindow().equals(window)
                    && closure.proofAction() == 1 && closure.personalBefore().equals(closure.personalAfter()),
                    "personal-window closure proof drifted"); boolean duplicateRejected = false;
            try { actor.closeWindow(); } catch (IllegalStateException expected) { duplicateRejected = true; }
            require(duplicateRejected, "duplicate remote window close was accepted");
            actor.sendChat("/give " + actorName + " 1 1");
            RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            worldline.test.WorldlineSmokeAwait.awaitSlot(actor, actor::inventory, 36, stone, 40);
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0)); take = actor.clickPersonalSlot(36);
            worldline.test.WorldlineSmokeAwait.observe(actor,5); observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName));
            restore = actor.clickPersonalSlot(36); worldline.test.WorldlineSmokeAwait.observe(actor,5);
            observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
            require(take.actionId() == 2 && restore.actionId() == 3
                    && restore.after().slot(36).item().equals(stone), "post-close personal transaction drifted");
            actor.close(); observer.close(); awaitPlayers(server, 0); server.save(); player = server.player(actorName);
            require(player.inventoryItems() == 1, "post-close inventory persistence drifted");
        } finally { actor.close(); observer.close(); server.close(); }
        System.out.println("WORLDLINE_M58_API=remote-window-close,tracked-packet101,personal-restore");
        System.out.println("WORLDLINE_M58_WINDOW=id=" + window.descriptor().windowId() + ";proof="
                + closure.proofAction() + ";actions=" + take.actionId() + "," + restore.actionId()
                + ";items=" + player.inventoryItems());
        System.out.println("WORLDLINE_M58_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M58_SIGNATURE=" + sha256(TRACE));
    }

    private static PersonalCraftingSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static PlayerPose settle(PersonalCraftingSession client) { worldline.test.WorldlineSmokeAwait.observe(client,5);
        MovementOutcome settled = null; for (int step = 0; step < 100; step++) {
            settled = client.moveAndObserve(0D, -1D, 0D, 2); if (settled.corrected()) break; }
        require(settled != null && settled.corrected(), "ground settlement correction absent");
        return settled.resulting(); }
    private static BlockPosition placement(RemoteWorldView view, PlayerPose pose) {
        int x=(int)Math.floor(pose.x()),y=(int)Math.floor(pose.y()),z=(int)Math.floor(pose.z());
        for(int r=2;r<=5;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)
            if(Math.max(Math.abs(dx),Math.abs(dz))==r)for(int dy=3;dy>=-5;dy--){BlockPosition support=
                new BlockPosition(x+dx,y+dy,z+dz),target=BlockFace.UP.adjacent(support);try{
                if(support.y()>=0&&target.y()<128&&view.blockAt(support.x(),support.y(),support.z()).legacyId()!=0
                        &&replaceable(view.blockAt(target.x(),target.y(),target.z())))return support;
            }catch(IllegalArgumentException absent){}}
        throw new IllegalStateException("nearby chest placement absent"); }
    private static boolean replaceable(BlockState state) { int id = state.legacyId();
        return id == 0 || id == 8 || id == 9 || id == 78; }
    private static void requirePlayers(List<String> players, String first, String second) {
        Set<String> expected = new HashSet<>(); expected.add(first); expected.add(second);
        require(players.size() == 2 && new HashSet<>(players).equals(expected), "two-player presence drifted"); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
