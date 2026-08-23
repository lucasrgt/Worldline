package worldline.smoke.doorupperbreaksetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places wooden door 64, Packet14-breaks the upper half, and freezes both-air plus item 324. */
public final class DoorUpperBreakSetSmoke {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState DOOR_LOW = new BlockState(64, 0);
    private static final BlockState DOOR_HIGH = new BlockState(64, 8);
    private static final RemoteItemStack DOOR = new RemoteItemStack(324, 1, 0);

    private DoorUpperBreakSetSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 9) {
            throw new IllegalArgumentException(
                    "usage: DoorUpperBreakSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        int fixture = Integer.parseInt(arguments[7]);
        int signal = Integer.parseInt(arguments[8]);
        Duration timeout = Duration.ofSeconds(90);
        require(seed == 17320110707L && user.equals("DoorUpper593") && user.length() <= 16,
                "door-upper-break identity drift");
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2}, new int[] {1, 324, 258}, new int[] {32, 1, 1}, new int[] {0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 3, "door-upper-break inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            BlockPosition top = foundation(initial, chunkX, chunkZ);
            int column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1, local(top.z(), chunkZ)).legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "water column exceeded door-upper-break fixture");
            }
            int lift = 0;
            while (lift < 8) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                column++;
                lift++;
            }
            BlockPosition lower = BlockFace.UP.adjacent(top);
            BlockPosition upper = BlockFace.UP.adjacent(lower);
            require(initial.blockAt(local(lower.x(), chunkX), lower.y(), local(lower.z(), chunkZ)).legacyId() == 0
                            && initial.blockAt(local(upper.x(), chunkX), upper.y(), local(upper.z(), chunkZ)).legacyId() == 0,
                    "door cells were not initial air");
            actor.look(-90F, 0F);
            actor.selectHeldSlot(1);
            actor.useHeldItemOnBlock(top, BlockFace.UP);
            awaitDoor(actor, lower, upper);
            worldline.test.WorldlineSmokeAwait.observe(actor, fixture);
            actor.selectHeldSlot(2);
            actor.beginBreak(upper);
            worldline.test.WorldlineSmokeAwait.observe(actor, signal);
            actor.finishBreak(upper);
            actor.awaitBlock(upper, AIR);
            actor.awaitBlock(lower, AIR);
            RemoteDroppedItem drop = actor.peekDroppedItem(DOOR);
            if (drop == null) {
                drop = actor.awaitDroppedItem(DOOR);
            }
            require(drop.item().equals(DOOR) && drop.item().legacyId() == 324 && drop.item().count() == 1,
                    "Packet21 door 324 drop absent");
            RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
            require(live.blockAt(upper.x(), upper.y(), upper.z()).equals(AIR)
                            && live.blockAt(lower.x(), lower.y(), lower.z()).equals(AIR),
                    "live door-upper-break leftover drift");
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            require(after.blockAt(local(lower.x(), chunkX), lower.y(), local(lower.z(), chunkZ)).equals(AIR)
                            && after.blockAt(local(upper.x(), chunkX), upper.y(), local(upper.z(), chunkZ)).equals(AIR),
                    "persisted door-upper-break air drift");
            String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
                    + ",lower=" + cell(lower, 64, 0) + "->0:0,upper=" + cell(upper, 64, 8)
                    + "->0:0,drops=packet21-324,persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+woodendoor64|settle=" + fixture + "+" + signal
                    + "ticks|cause=packet15-item324-place+packet14-ironaxe258-upper"
                    + "|effect=official-door64-upper-break+both-air+packet21-324"
                    + "|observation=fresh-login-packet51|" + evidence;
            System.out.println("WORLDLINE_M593_SET=" + evidence);
            System.out.println("WORLDLINE_M593_TRACE=" + trace);
            System.out.println("WORLDLINE_M593_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
    }

    private static void awaitDoor(B173WireClient actor, BlockPosition lower, BlockPosition upper)
            throws Exception {
        RemoteWorldView view = actor.awaitBlock(lower, DOOR_LOW);
        if (!view.blockAt(upper.x(), upper.y(), upper.z()).equals(DOOR_HIGH)) {
            view = actor.awaitBlock(upper, DOOR_HIGH);
        }
        require(view.blockAt(lower.x(), lower.y(), lower.z()).equals(DOOR_LOW)
                        && view.blockAt(upper.x(), upper.y(), upper.z()).equals(DOOR_HIGH),
                "wooden door cells " + view.blockAt(lower.x(), lower.y(), lower.z())
                        + " / " + view.blockAt(upper.x(), upper.y(), upper.z()));
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic door-upper-break foundation");
    }

    private static String cell(BlockPosition position, int id, int meta) {
        return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + meta;
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int value, int chunk) {
        return value - chunk * 16;
    }

    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) {
                return;
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte item : digest) {
            hex.append(String.format("%02x", item & 255));
        }
        return hex.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
