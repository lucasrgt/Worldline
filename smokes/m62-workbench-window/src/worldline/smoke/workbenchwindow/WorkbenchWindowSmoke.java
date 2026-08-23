package worldline.smoke.workbenchwindow;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWindowKind;
import worldline.api.RemoteWorldView;
import worldline.api.WorkbenchSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Proves the exceptional declared-nine/combined-46 workbench window layout. */
public final class WorkbenchWindowSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|servers=1|clients=1"
            + "|fixture=placed-workbench58+stone1|open=packet15-empty"
            + "|descriptor=packet100-workbench-Crafting-declared9"
            + "|contents=packet104-total46|owned0-9=empty"
            + "|mapping=personal36-combined37-stone|close=packet101+personal-action1"
            + "|block=58|player-items=1|disconnect=clean";
    private WorkbenchWindowSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: WorkbenchWindowSmoke server.jar workspace port seed actor");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String actorName = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        WorkbenchSession actor = new B173WireClient("127.0.0.1", port, actorName, timeout);
        BlockPosition target; RemoteContainerWindow opened; RemoteWindowClosure closure;
        try {
            server.boot(); server.operator(actorName); actor.connect(); actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 0, "actor inventory was not empty");
            actor.look(0F, 90F); PlayerPose pose = acquire(actor, actorName, 58, 1);
            RemoteWorldView baseline = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4); BlockPosition support = placement(baseline, pose);
            target = BlockFace.UP.adjacent(support); require(replaceable(baseline.blockAt(
                    target.x(), target.y(), target.z())), "workbench target was not replaceable");
            int workbenchSlot = find(actor.inventory(), new RemoteItemStack(58, 1, 0));
            require(workbenchSlot >= 36 && workbenchSlot <= 44, "workbench hotbar seed drifted");
            actor.selectHeldSlot(workbenchSlot - 36);
            actor.placeHeldBlock(support, BlockFace.UP); BlockState placed = actor.awaitBlock(
                    target, new BlockState(58, 0)).blockAt(target.x(), target.y(), target.z());
            worldline.test.WorldlineSmokeAwait.observe(actor,5); require(placed.equals(new BlockState(58, 0))
                    && actor.inventory().occupiedSlots() == 0, "placed workbench state drifted");
            acquire(actor, actorName, 1, 1); RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
            require(actor.inventory().occupiedSlots() == 1 && actor.inventory().slot(36).item().equals(stone)
                    && actor.inventory().slot(37).empty(), "stone sentinel seed drifted");
            actor.selectHeldSlot(1); opened = actor.openWorkbench(target, BlockFace.UP);
            RemoteWindowDescriptor descriptor = opened.descriptor(); RemoteInventoryView combined = opened.inventory();
            require(descriptor.kind() == RemoteWindowKind.WORKBENCH && "Crafting".equals(descriptor.title())
                    && descriptor.containerSlots() == 9 && descriptor.playerTailOffset() == 10
                    && descriptor.totalSlots() == 46 && combined.size() == 46,
                    "workbench descriptor or shape drifted");
            for (int slot = 0; slot < 10; slot++) require(combined.slot(slot).empty(),
                    "workbench result or matrix was not empty");
            require(combined.occupiedSlots() == 1 && combined.slot(37).item().equals(stone)
                    && actor.inventory().slot(36).item().equals(stone), "workbench personal tail mapping drifted");
            closure = actor.closeWindow(); require(closure.closedWindow().equals(opened)
                    && closure.proofAction() == 1 && closure.personalBefore().slot(36).item().equals(stone)
                    && closure.personalAfter().equals(closure.personalBefore()), "workbench close proof drifted");
            actor.close(); awaitPlayers(server, 0); server.save();
            require(server.player(actorName).inventoryItems() == 1, "workbench player inventory did not persist");
        } finally { actor.close(); server.close(); }
        System.out.println("WORLDLINE_M62_API=workbench-window,packet100-declared9,packet104-total46");
        System.out.println("WORLDLINE_M62_WINDOW=id=" + opened.descriptor().windowId() + ";declared="
                + opened.descriptor().containerSlots() + ";offset=" + opened.descriptor().playerTailOffset()
                + ";total=" + opened.inventory().size() + ";stone=36->37;close=" + closure.proofAction());
        System.out.println("WORLDLINE_M62_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M62_SIGNATURE=" + sha256(TRACE));
    }

    private static PlayerPose acquire(WorkbenchSession client, String username, int id, int count) {
        int target = client.inventory().occupiedSlots() + count;
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " " + id + " " + count);
        worldline.test.WorldlineSmokeAwait.awaitEntity(client,()->{client.moveAndObserve(0D,-1D,0D,1);
            return client.inventory();},inventory->inventory.occupiedSlots()>=target,"workbench grant",100);
        worldline.test.WorldlineSmokeAwait.observe(client,10); MovementOutcome settled = null;
        for (int step = 0; step < 100; step++) { settled = client.moveAndObserve(0D, -1D, 0D, 2);
            if (settled.corrected()) break; } require(settled != null && settled.corrected(),
                "ground settlement correction absent"); return settled.resulting();
    }
    private static BlockPosition placement(RemoteWorldView view, PlayerPose pose) {
        int x = (int) Math.floor(pose.x()), y = (int) Math.floor(pose.y()), z = (int) Math.floor(pose.z());
        for (int radius = 2; radius <= 5; radius++) for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++) if (Math.max(Math.abs(dx), Math.abs(dz)) == radius)
                for (int dy = 3; dy >= -5; dy--) { BlockPosition support = new BlockPosition(x + dx, y + dy, z + dz);
                    BlockPosition target = BlockFace.UP.adjacent(support); try { if (support.y() >= 0 && target.y() < 128
                            && view.blockAt(support.x(), support.y(), support.z()).legacyId() != 0
                            && replaceable(view.blockAt(target.x(), target.y(), target.z()))) return support;
                    } catch (IllegalArgumentException absent) { } }
        throw new IllegalStateException("nearby workbench placement absent");
    }
    private static boolean replaceable(BlockState state) { int id = state.legacyId();
        return id == 0 || id == 8 || id == 9 || id == 78; }
    private static int find(RemoteInventoryView view, RemoteItemStack expected) {
        for (int slot = 9; slot <= 44; slot++) if (!view.slot(slot).empty()
                && view.slot(slot).item().equals(expected)) return slot; return -1; }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
