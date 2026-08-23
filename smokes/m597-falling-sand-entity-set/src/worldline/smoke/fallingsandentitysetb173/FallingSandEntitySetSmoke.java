package worldline.smoke.fallingsandentitysetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Removes one support and observes the official falling-sand entity land as sand. */
public final class FallingSandEntitySetSmoke {
    private FallingSandEntitySetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 9) {
            throw new IllegalArgumentException("usage: FallingSandEntitySetSmoke server.jar workspace port "
                    + "seed username chunkX chunkZ fixtureTicks gravityTicks");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String username = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        int fixtureTicks = Integer.parseInt(arguments[7]);
        int gravityTicks = Integer.parseInt(arguments[8]);
        Duration timeout = Duration.ofSeconds(90);
        require(seed == 17320110707L && username.equals("FallSand597") && username.length() <= 16,
                "falling-sand-entity identity drift");
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 5, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout);
        B173WireClient reader = null;
        RemoteObjectSpawn fall = null;
        BlockPosition support = null;
        BlockPosition sand = null;
        int column = 0;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D,
                    new int[] {0, 1}, new int[] {1, 12}, new int[] {16, 1}, new int[] {0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 2, "falling-sand-entity inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            support = foundation(initial, chunkX, chunkZ);
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(support.x(), chunkX), support.y() + 1,
                    local(support.z(), chunkZ)).legacyId())) {
                support = place(actor, support, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                column++;
                require(column <= 15, "water column exceeded falling-sand-entity fixture");
            }
            support = place(actor, support, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column++;
            sand = BlockFace.UP.adjacent(support);
            require(initial.blockAt(local(sand.x(), chunkX), sand.y(),
                    local(sand.z(), chunkZ)).legacyId() == 0, "sand target was not initial air");
            actor.selectHeldSlot(1);
            actor.placeHeldBlock(support, BlockFace.UP);
            actor.awaitBlock(sand, new BlockState(12, 0));
            actor.selectHeldSlot(2);
            actor.moveAndObserve(0D, -2D, 0D, 2);
            RemoteChunkSnapshot before = WorldlineSmokeAwait.observe(actor, fixtureTicks)
                    .chunkAt(chunkX, chunkZ);
            require(before.blockAt(local(support.x(), chunkX), support.y(), local(support.z(), chunkZ))
                    .equals(new BlockState(1, 0)), "stable sand support fixture drift");
            require(before.blockAt(local(sand.x(), chunkX), sand.y(), local(sand.z(), chunkZ))
                    .equals(new BlockState(12, 0)), "stable sand 12:0 fixture drift");
            actor.beginBreak(support);
            Thread.sleep(3000L);
            actor.finishBreak(support);
            BlockState opened = actor.awaitBlock(support, new BlockState(0, 0))
                    .blockAt(support.x(), support.y(), support.z());
            WorldlineSmokeAwait.observe(actor, 3);
            fall = actor.awaitObjectSpawn(70);
            require(opened.equals(new BlockState(0, 0)) && fall.type() == 70
                    && fall.entityId() != actor.state().entityId(),
                    "falling-sand Packet23 type drift type=" + fall.type());
            RemoteWorldView live = WorldlineSmokeAwait.observe(actor, gravityTicks);
            require(live.blockAt(support.x(), support.y(), support.z()).equals(new BlockState(12, 0))
                    && live.blockAt(sand.x(), sand.y(), sand.z()).equals(new BlockState(0, 0)),
                    "sand did not land as 12:0 after entity " + fall.type());
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, username, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            require(after.blockAt(local(support.x(), chunkX), support.y(), local(support.z(), chunkZ))
                    .equals(new BlockState(12, 0)), "fresh landed sand 12:0 drift");
            require(after.blockAt(local(sand.x(), chunkX), sand.y(), local(sand.z(), chunkZ))
                    .equals(new BlockState(0, 0)), "fresh cleared upper sand drift");
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
        require(fall != null && support != null && sand != null, "falling-sand-entity evidence missing");
        String evidence = "column=" + column + ",lower=" + cell(support) + ":1:0->12:0,upper="
                + cell(sand) + ":12:0->0:0,entity-type=" + fall.type() + ",packet23=" + fall.type()
                + ",persisted=true,clients=2,disconnect=clean";
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|fixture=stone-column+supported-sand12|settle=" + fixtureTicks + "+" + gravityTicks
                + "ticks|cause=packet14-remove-support|confirmation=packet53-air"
                + "|effect=official-falling-sand-entity-land"
                + "|observation=packet23-type-observed+live-packet53+fresh-login-packet51|" + evidence;
        System.out.println("WORLDLINE_M597_SET=" + evidence);
        System.out.println("WORLDLINE_M597_TRACE=" + trace);
        System.out.println("WORLDLINE_M597_SIGNATURE=" + sha(trace));
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
                    if (chunk.blockAt(x, y, z).legacyId() == 3
                            && water(chunk.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic falling-sand-entity foundation");
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
        StringBuilder hex = new StringBuilder();
        int index = 0;
        while (index < digest.length) {
            hex.append(String.format("%02x", digest[index] & 255));
            index++;
        }
        return hex.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
