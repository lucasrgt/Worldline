package worldline.smoke.chestbreakspillsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChestTransfer;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowKind;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Places a loaded official chest 54, Packet14-breaks it, and proves Packet21 spills. */
public final class ChestBreakSpillSetSmoke {
    private static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);
    private static final RemoteItemStack DIRT = new RemoteItemStack(3, 1, 0);
    private static final RemoteItemStack CHEST = new RemoteItemStack(54, 1, 0);
    private static final RemoteItemStack AXE = new RemoteItemStack(286, 1, 0);
    private static final BlockState AIR = new BlockState(0, 0);

    private ChestBreakSpillSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: ChestBreakSpillSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        Duration timeout = Duration.ofSeconds(90);
        require(seed == 17320110707L && user.equals("ChestSpill592") && user.length() <= 16,
                "chest-break-spill identity drift");
        B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        BlockPosition top, chest;
        int column;
        RemoteDroppedItem cobble, dirt, chestDrop;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4}, new int[] {1, 54, 4, 3, 286},
                    new int[] {32, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 5
                    && actor.inventory().slot(40).item().equals(AXE),
                    "chest-break-spill inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            top = foundation(initial, cx, cz);
            column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(
                    local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "water column exceeded chest-break-spill fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                column++;
            }
            actor.selectHeldSlot(1);
            chest = place(actor, top, BlockFace.UP, 54);
            RemoteContainerWindow window = actor.openChest(chest, BlockFace.UP);
            require(window.descriptor().kind() == RemoteWindowKind.CHEST
                    && "Chest".equals(window.descriptor().title())
                    && window.descriptor().containerSlots() == 27
                    && window.inventory().size() == 63,
                    "chest-break-spill window drift");
            RemoteChestTransfer loadCobble = actor.storeInOpenChest(38, 0);
            RemoteChestTransfer loadDirt = actor.storeInOpenChest(39, 1);
            require(loadCobble.stack().equals(COBBLE) && loadDirt.stack().equals(DIRT)
                    && loadDirt.after().slot(0).item().equals(COBBLE)
                    && loadDirt.after().slot(1).item().equals(DIRT),
                    "chest-break-spill store drift");
            actor.closeWindow();
            harvest(actor, chest);
            cobble = drop(actor, COBBLE);
            dirt = drop(actor, DIRT);
            chestDrop = drop(actor, CHEST);
            require(cobble.item().equals(COBBLE) && dirt.item().equals(DIRT)
                    && chestDrop.item().equals(CHEST)
                    && cobble.entityId() != dirt.entityId()
                    && dirt.entityId() != chestDrop.entityId()
                    && cobble.entityId() != chestDrop.entityId(),
                    "chest-break-spill Packet21 identity drift");
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                    .equals(new BlockState(1, 0))
                    && after.blockAt(local(chest.x(), cx), chest.y(),
                    local(chest.z(), cz)).equals(AIR),
                    "persisted chest-break-spill air drift");
            String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
                    + ",chest=" + cell(chest, 54, 0)
                    + "->0:0,load=4x1+3x1,spill=packet21-4x1+packet21-3x1,chest-drop=packet21-54"
                    + ",persisted=air,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+chest54+cobble4+dirt3"
                    + "|cause=packet15-item54+packet102-store-4+packet102-store-3+packet14-goldaxe286"
                    + "|wire=packet100-Chest-27+packet21-4x1+packet21-3x1+packet21-54"
                    + "|oracle=chest-break-spill-not-place-not-orient|" + evidence;
            System.out.println("WORLDLINE_M592_SPILL=" + evidence);
            System.out.println("WORLDLINE_M592_TRACE=" + trace);
            System.out.println("WORLDLINE_M592_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) reader.close();
            server.close();
        }
    }

    private static void harvest(B173WireClient actor, BlockPosition chest) throws Exception {
        actor.selectHeldSlot(4);
        actor.beginBreak(chest);
        WorldlineSmokeAwait.observe(actor, 12);
        actor.finishBreak(chest);
        actor.awaitBlock(chest, AIR);
    }

    private static RemoteDroppedItem drop(B173WireClient actor, RemoteItemStack expected) {
        RemoteDroppedItem item = actor.peekDroppedItem(expected);
        if (item == null) item = actor.awaitDroppedItem(expected);
        require(item.item().equals(expected) && item.item().legacyId() == expected.legacyId()
                && item.item().count() == expected.count(),
                "Packet21 " + expected.legacyId() + " spill absent");
        return item;
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support,
            BlockFace face, int id) throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic chest-break-spill foundation");
    }

    private static String cell(BlockPosition position, int id, int meta) {
        return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + meta;
    }

    private static boolean water(int id) { return id == 8 || id == 9; }
    private static int local(int value, int chunk) { return value - chunk * 16; }

    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return;
            Thread.sleep(100L);
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte item : digest) hex.append(String.format("%02x", item & 255));
        return hex.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
