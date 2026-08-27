package worldline.smoke.leafsupportdistanceb173;

import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves the official four-block leaf support radius against an isolated five-block control. */
public final class LeafSupportDistanceSmoke {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STONE = new BlockState(1, 0);
    private static final BlockState LOG = new BlockState(17, 0);
    private static final BlockState LEAF = new BlockState(18, 8);

    private LeafSupportDistanceSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 9) {
            throw new IllegalArgumentException(
                    "usage: LeafSupportDistanceSmoke server.jar workspace port seed username "
                            + "chunkX chunkZ windowTicks distanceWindows");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String username = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        int windowTicks = Integer.parseInt(arguments[7]);
        int distanceWindows = Integer.parseInt(arguments[8]);
        require(seed == 17320110707L && username.equals("LeafDist665")
                        && username.length() <= 16 && windowTicks >= 1 && windowTicks <= 1200
                        && distanceWindows >= 1 && distanceWindows <= 8,
                "leaf-support-distance arguments");

        Duration timeout = Duration.ofMinutes(20);
        B173DedicatedServer server =
                new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout);
        B173WireClient reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2}, new int[] {1, 17, 18},
                    new int[] {64, 1, 2}, new int[] {0, 0, 0});
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 3,
                    "leaf-support-distance inventory drift");

            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            BlockPosition top = foundation(initial, chunkX, chunkZ);
            requireNoFoliage(initial, chunkX, chunkZ, top);
            int column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(top.x(), chunkX), top.y() + 1,
                    local(top.z(), chunkZ)).legacyId())) {
                top = place(actor, top, BlockFace.UP, STONE);
                pose = step(actor, pose, 0D, 1D, 0D);
                column++;
                require(column <= 15, "water column exceeded leaf-support-distance fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = place(actor, top, BlockFace.UP, STONE);
                pose = step(actor, pose, 0D, 1D, 0D);
                column++;
            }
            require(column == 17, "leaf-support-distance column drift");

            BlockPosition nearSupport = top;
            for (int step = 0; step < 4; step++) {
                nearSupport = place(actor, nearSupport, BlockFace.EAST, STONE);
                pose = step(actor, pose, 1D, 0D, 0D);
            }
            pose = go(actor, pose, top.x() + 0.5D, top.y() + 2.0D, top.z() + 0.5D);
            BlockPosition farSupport = top;
            for (int step = 0; step < 5; step++) {
                farSupport = place(actor, farSupport, BlockFace.SOUTH, STONE);
                pose = step(actor, pose, 0D, 0D, 1D);
            }

            pose = go(actor, pose, top.x() + 0.5D, top.y() + 2.0D, top.z() + 0.5D);
            actor.selectHeldSlot(1);
            BlockPosition log = place(actor, top, BlockFace.UP, LOG);
            pose = go(actor, pose, nearSupport.x() + 0.5D, nearSupport.y() + 2.0D,
                    nearSupport.z() + 0.5D);
            actor.selectHeldSlot(2);
            BlockPosition nearLeaf = place(actor, nearSupport, BlockFace.UP, LEAF);
            require(distance(log, nearLeaf) == 4, "near leaf distance drift");

            pose = go(actor, pose, farSupport.x() + 0.5D, farSupport.y() + 2.0D,
                    farSupport.z() + 0.5D);
            RemoteWorldView placed = placeObserved(actor, farSupport, BlockFace.UP, LEAF);
            BlockPosition farLeaf = BlockFace.UP.adjacent(farSupport);
            require(distance(log, farLeaf) == 5 && distance(nearLeaf, farLeaf) == 5,
                    "far leaf isolation drift");
            require(at(placed, log).equals(LOG) && at(placed, nearLeaf).equals(LEAF)
                            && at(placed, farLeaf).equals(LEAF),
                    "leaf-support-distance placement drift");

            RemoteWorldView settled = actor.awaitBlock(farLeaf, AIR);
            require(at(settled, log).equals(LOG) && leaf(at(settled, nearLeaf))
                            && at(settled, farLeaf).equals(AIR),
                    "live leaf-support-distance drift");

            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, username, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            require(at(after, log, chunkX, chunkZ).equals(LOG)
                            && leaf(at(after, nearLeaf, chunkX, chunkZ))
                            && at(after, farLeaf, chunkX, chunkZ).equals(AIR)
                            && at(after, nearSupport, chunkX, chunkZ).equals(STONE)
                            && at(after, farSupport, chunkX, chunkZ).equals(STONE),
                    "persisted leaf-support-distance drift");

            String evidence = "column=" + column
                    + ",log=17:0,near=18:8@distance4->leaf"
                    + ",far=18:8@distance5->0:0,support-radius=4"
                    + ",persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+oak17+isolated-leaves18-axis-distance4+distance5"
                    + "|cause=packet15-item17+packet15-item18+random-ticks"
                    + "|wire=packet53-leaves18:8->0:0+packet51-leaves18:8+packet51-log17:0"
                    + "|oracle=official-leaf-support-radius4+fresh-login|" + evidence;
            System.out.println("WORLDLINE_M665_SET=" + evidence);
            System.out.println("WORLDLINE_M665_TRACE=" + trace);
            System.out.println("WORLDLINE_M665_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
    }

    private static BlockPosition place(B173WireClient client, BlockPosition support,
            BlockFace face, BlockState expected) throws Exception {
        return worldline.b173server.B173FixtureSupport.place(client, support, face, expected);
    }

    private static RemoteWorldView placeObserved(B173WireClient client, BlockPosition support,
            BlockFace face, BlockState expected) {
        BlockPosition target = face.adjacent(support);
        client.placeHeldBlock(support, face);
        return client.awaitBlock(target, expected);
    }

    private static PlayerPose step(B173WireClient actor, PlayerPose pose,
            double dx, double dy, double dz) {
        return actor.moveAndObserve(dx, dy, dz, 1).resulting();
    }

    private static PlayerPose go(B173WireClient actor, PlayerPose pose,
            double x, double y, double z) {
        for (int attempt = 0; attempt < 16; attempt++) {
            if (close(pose, x, y, z)) {
                return pose;
            }
            pose = step(actor, pose, clamp(x - pose.x()), clamp(y - pose.y()),
                    clamp(z - pose.z()));
        }
        require(close(pose, x, y, z), "player did not reach leaf-support-distance target");
        return pose;
    }

    private static boolean close(PlayerPose pose, double x, double y, double z) {
        return Math.abs(pose.x() - x) <= 0.4D && Math.abs(pose.y() - y) <= 0.4D
                && Math.abs(pose.z() - z) <= 0.4D;
    }

    private static double clamp(double value) {
        if (value > 1D) {
            return 1D;
        }
        if (value < -1D) {
            return -1D;
        }
        return value;
    }

    private static BlockPosition foundation(RemoteChunkSnapshot snapshot, int chunkX, int chunkZ) {
        for (int x = 4; x <= 11; x++) {
            for (int z = 4; z <= 11; z++) {
                for (int y = 126; y >= 1; y--) {
                    if (snapshot.blockAt(x, y, z).legacyId() == 3
                            && water(snapshot.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                }
            }
        }
        throw new IllegalStateException("no deterministic leaf-support-distance foundation");
    }

    private static void requireNoFoliage(RemoteChunkSnapshot snapshot, int chunkX,
            int chunkZ, BlockPosition top) {
        for (int x = top.x() - 4; x <= top.x() + 8; x++) {
            for (int z = top.z() - 4; z <= top.z() + 9; z++) {
                for (int y = top.y() - 3; y <= top.y() + 5; y++) {
                    int id = snapshot.blockAt(local(x, chunkX), y, local(z, chunkZ)).legacyId();
                    require(id != 17 && id != 18,
                            "natural foliage intersects leaf-support-distance fixture");
                }
            }
        }
    }

    private static int distance(BlockPosition first, BlockPosition second) {
        int x = Math.abs(first.x() - second.x());
        int y = Math.abs(first.y() - second.y());
        int z = Math.abs(first.z() - second.z());
        return Math.max(x, Math.max(y, z));
    }

    private static boolean leaf(BlockState state) {
        return state.legacyId() == 18 && (state.metadata() & 3) == 0;
    }

    private static BlockState at(RemoteWorldView view, BlockPosition position) {
        return view.blockAt(position.x(), position.y(), position.z());
    }

    private static BlockState at(RemoteChunkSnapshot snapshot, BlockPosition position,
            int chunkX, int chunkZ) {
        return snapshot.blockAt(local(position.x(), chunkX), position.y(),
                local(position.z(), chunkZ));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
