package worldline.smoke.ladderclimbposeb173;

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
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places a two-cell east ladder and proves Packet13 pose y holds versus an air fall. */
public final class LadderClimbPoseSmoke {
    private LadderClimbPoseSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 7) throw new IllegalArgumentException(
                "usage: LadderClimbPoseSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
        require(seed == 17320110707L && user.equals("LadderClb600") && user.length() <= 16,
                "ladder-climb identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1}, new int[] {1, 65}, new int[] {32, 8}, new int[] {0, 0});
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 2, "ladder-climb inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            BlockPosition top = foundation(initial, cx, cz);
            int column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
                top = place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "water column exceeded ladder-climb fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                column++;
            }
            BlockPosition upper = place(actor, top, BlockFace.UP, 1);
            pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
            column++;
            BlockPosition low = BlockFace.EAST.adjacent(top);
            BlockPosition high = BlockFace.EAST.adjacent(upper);
            require(initial.blockAt(local(low.x(), cx), low.y(), local(low.z(), cz)).legacyId() == 0
                            && initial.blockAt(local(high.x(), cx), high.y(), local(high.z(), cz)).legacyId() == 0,
                    "ladder column was not initial air");
            actor.selectHeldSlot(1);
            BlockState placed = new BlockState(65, 5);
            actor.placeHeldBlock(top, BlockFace.EAST);
            actor.awaitBlock(low, placed);
            actor.placeHeldBlock(upper, BlockFace.EAST);
            actor.awaitBlock(high, placed);
            RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
            require(live.blockAt(low.x(), low.y(), low.z()).equals(placed)
                            && live.blockAt(high.x(), high.y(), high.z()).equals(placed),
                    "live two-cell ladder 65:5 drift");
            int ticks = 10;
            require(live.blockAt(low.x() + 1, upper.y() + 1, low.z()).legacyId() == 0
                            && live.blockAt(low.x() + 1, upper.y(), low.z()).legacyId() == 0,
                    "air-fall column was not air");
            pose = actor.moveAndObserve(low.x() + 1.5D - pose.x(), upper.y() + 1.5D - pose.y(),
                    low.z() + 0.5D - pose.z(), 4).resulting();
            require((int) Math.floor(pose.x()) == low.x() + 1 && pose.y() >= upper.y() + 1.0D,
                    "air pose drift");
            PlayerPose airStart = pose;
            MovementOutcome airMove = actor.moveAndObserve(0D, -1D, 0D, ticks);
            pose = airMove.resulting();
            int air = milli(airStart.y() - pose.y());
            require(!airMove.corrected() && air > 0, "air Packet13 fall was not free " + air);
            pose = actor.moveAndObserve(low.x() + 0.5D - pose.x(), low.y() + 1.0D - pose.y(),
                    low.z() + 0.5D - pose.z(), 4).resulting();
            actor.look(90F, 0F);
            pose = actor.moveAndObserve(-0.2D, 0D, 0D, 2).resulting();
            require((int) Math.floor(pose.x()) == low.x() && (int) Math.floor(pose.z()) == low.z()
                            && live.blockAt(low.x(), (int) Math.floor(pose.y()), low.z()).legacyId() == 65,
                    "ladder pose was not inside 65");
            PlayerPose ladderStart = pose;
            MovementOutcome ladderMove = actor.moveAndObserve(0D, 0D, 0D, ticks);
            pose = ladderMove.resulting();
            int climb = milli(pose.y() - ladderStart.y());
            require(air > 0 && climb >= 0, "ladder pose y did not hold or climb vs air fall "
                    + air + "/" + climb);
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(new BlockState(1, 0))
                            && after.blockAt(local(upper.x(), cx), upper.y(), local(upper.z(), cz))
                                    .equals(new BlockState(1, 0))
                            && after.blockAt(local(low.x(), cx), low.y(), local(low.z(), cz)).equals(placed)
                            && after.blockAt(local(high.x(), cx), high.y(), local(high.z(), cz)).equals(placed),
                    "persisted ladder-climb drift");
            String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
                    + ",upper=" + cell(upper, 1, 0) + ",ladder=" + cell(low, 65, 5) + "+"
                    + cell(high, 65, 5) + ",face=east,ticks=" + ticks
                    + ",air-fall=true,ladder-hold=true,climbed=true,persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+ladder65-east-column2"
                    + "|cause=packet15-item65+packet13-air-fall+packet13-ladder-pose"
                    + "|wire=packet13-pose-air-vs-ladder|oracle=ladder-climb-or-hold-vs-air-fall|"
                    + evidence;
            System.out.println("WORLDLINE_M600_SET=" + evidence);
            System.out.println("WORLDLINE_M600_TRACE=" + trace);
            System.out.println("WORLDLINE_M600_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) reader.close();
            server.close();
        }
    }

    private static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic ladder-climb foundation");
    }

    private static String cell(BlockPosition position, int id, int meta) {
        return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + meta;
    }

    private static boolean water(int id) { return id == 8 || id == 9; }
    private static int local(int value, int chunk) { return value - chunk * 16; }
    private static int milli(double value) { return (int) Math.round(value * 1000D); }

    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            if (server.players().size() == count) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte next : digest) hex.append(String.format("%02x", next & 255));
        return hex.toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
