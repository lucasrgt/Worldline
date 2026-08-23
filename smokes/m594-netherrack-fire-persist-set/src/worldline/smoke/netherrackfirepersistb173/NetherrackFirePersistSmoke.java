package worldline.smoke.netherrackfirepersistb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Netherrack fire persists across a long window while plank fire expires. */
public final class NetherrackFirePersistSmoke {
    private NetherrackFirePersistSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) {
            throw new IllegalArgumentException("usage: NetherrackFirePersistSmoke "
                    + "server.jar workspace port seed username chunkX chunkZ");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        require(seed == 17320110707L && user.equals("NethFire594") && user.length() <= 16,
                "netherrack-fire-persist identity drift");
        Duration timeout = Duration.ofMinutes(8);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        BlockPosition top;
        BlockPosition rack;
        BlockPosition netherFire;
        BlockPosition planks;
        BlockPosition plankFire;
        int column;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3}, new int[] {1, 87, 5, 259},
                    new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 4, "fire persist inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            top = foundation(initial, chunkX, chunkZ);
            column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1,
                    local(top.z(), chunkZ)).legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "water column exceeded fire persist fixture");
            }
            int lift = 0;
            while (lift < 8) {
                top = place(actor, top, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                column++;
                lift++;
            }
            actor.selectHeldSlot(1);
            rack = place(actor, top, BlockFace.UP, 87);
            netherFire = BlockFace.UP.adjacent(rack);
            actor.selectHeldSlot(0);
            BlockPosition pedestal = place(actor, rack, BlockFace.EAST, 1);
            actor.selectHeldSlot(2);
            planks = place(actor, pedestal, BlockFace.UP, 5);
            plankFire = BlockFace.UP.adjacent(planks);
            actor.selectHeldSlot(3);
            actor.useHeldItemOnBlock(rack, BlockFace.UP);
            actor.awaitBlock(netherFire, new BlockState(51, 0));
            actor.useHeldItemOnBlock(planks, BlockFace.UP);
            actor.awaitBlock(plankFire, new BlockState(51, 0));
            RemoteWorldView early = worldline.test.WorldlineSmokeAwait.observe(actor, 40);
            require(id(early, rack) == 87 && id(early, netherFire) == 51,
                    "netherrack fire lost before long window");
            actor.moveAndObserve(8D, 0D, 0D, 8);
            worldline.test.WorldlineSmokeAwait.observe(actor, 1200);
            worldline.test.WorldlineSmokeAwait.observe(actor, 1200);
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            int nether = cellId(after, chunkX, chunkZ, netherFire);
            int plankCell = cellId(after, chunkX, chunkZ, planks);
            int plankFlame = cellId(after, chunkX, chunkZ, plankFire);
            require(cellId(after, chunkX, chunkZ, rack) == 87 && nether == 51
                            && plankCell != 51 && plankFlame != 51,
                    "fresh login netherrack persist or plank expiry drift nether="
                            + nether + " planks=" + plankCell + " flame=" + plankFlame);
            String evidence = "column=" + column + ",support=" + cell(top) + ":1:0,rack="
                    + cell(rack) + ":87:0,flint=259,nether-fire=" + cell(netherFire)
                    + ":51,planks=" + cell(planks) + ":5,plank-fire=" + cell(plankFire)
                    + ":expired,hold=2400,netherrack-persist=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-netherrack87+planks5+flintsteel259"
                    + "|cause=packet15-item259+long-observation-window"
                    + "|wire=packet53-fire51-netherrack-persist+plank-fire-expired"
                    + "|oracle=netherrack-fire-persist-not-stone-ignition-not-support-extinguish|"
                    + evidence;
            System.out.println("WORLDLINE_M594_FIRE=" + evidence);
            System.out.println("WORLDLINE_M594_TRACE=" + trace);
            System.out.println("WORLDLINE_M594_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face,
            int id) throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot snapshot, int chunkX, int chunkZ) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (snapshot.blockAt(x, y, z).legacyId() == 3
                            && water(snapshot.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic fire persist foundation");
    }

    private static int id(RemoteWorldView view, BlockPosition position) {
        return view.blockAt(position.x(), position.y(), position.z()).legacyId();
    }

    private static int cellId(RemoteChunkSnapshot chunk, int chunkX, int chunkZ, BlockPosition position) {
        return chunk.blockAt(local(position.x(), chunkX), position.y(),
                local(position.z(), chunkZ)).legacyId();
    }

    private static String cell(BlockPosition position) {
        return position.x() + ":" + position.y() + ":" + position.z();
    }

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    private static int local(int value, int chunk) {
        return value - chunk * 16;
    }

    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) {
                return;
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte item : digest) {
            text.append(String.format("%02x", item & 255));
        }
        return text.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
