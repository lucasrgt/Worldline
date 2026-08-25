package worldline.smoke.portalinvalidframeb173;

import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;
import worldline.testkit.PortalInvalidFrameFixture;

/** Proves that one required top-frame gap prevents official portal activation. */
public final class PortalInvalidFrameSmoke {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState FIRE = new BlockState(51, 0);
    private PortalInvalidFrameSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: PortalInvalidFrameSmoke server.jar workspace port seed user chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        require(seed == 17320110707L && "PortalGate651".equals(user)
                && cx == 0 && cz == 0, "invalid portal fixture identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true, true);
        B173WireClient actor = client(port, user, timeout);
        B173WireClient reader = client(port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2}, new int[] {1, 49, 259},
                    new int[] {16, 13, 1}, new int[] {0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 3, "portal inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            BlockPosition anchor = foundation(initial, cx, cz);
            int column = 0;
            actor.selectHeldSlot(0);
            while (water(initial.blockAt(local(anchor.x(), cx), anchor.y() + 1,
                    local(anchor.z(), cz)).legacyId())) {
                anchor = place(actor, anchor, BlockFace.UP, 1);
                actor.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "water column exceeded portal fixture");
            }
            anchor = place(actor, anchor, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column++;
            actor.selectHeldSlot(1);
            BlockPosition bottom = place(actor, anchor, BlockFace.UP, 49);
            List<BlockPosition> frame = new ArrayList<>(), interior = new ArrayList<>();
            frame.add(bottom);
            BlockPosition cursor = bottom;
            for (int east = 0; east < 3; east++) {
                cursor = place(actor, cursor, BlockFace.EAST, 49);
                frame.add(cursor);
            }
            BlockPosition left = bottom, right = cursor;
            for (int up = 0; up < 4; up++) {
                left = place(actor, left, BlockFace.UP, 49);
                right = place(actor, right, BlockFace.UP, 49);
                frame.add(left);
                frame.add(right);
            }
            frame.add(place(actor, left, BlockFace.EAST, 49));
            BlockPosition missing = new BlockPosition(bottom.x() + 2,
                    bottom.y() + 4, bottom.z());
            for (int y = 1; y <= 3; y++) for (int x = 1; x <= 2; x++)
                interior.add(new BlockPosition(bottom.x() + x, bottom.y() + y, bottom.z()));
            require(frame.size() == 13 && interior.size() == 6,
                    "invalid portal geometry drift");
            RemoteWorldView framed = actor.awaitRemoteChunk(cx, cz);
            for (BlockPosition cell : frame)
                require(framed.blockAt(cell.x(), cell.y(), cell.z())
                        .equals(new BlockState(49, 0)), "obsidian frame drift at " + cell);
            require(framed.blockAt(missing.x(), missing.y(), missing.z()).equals(AIR),
                    "required top-frame gap was not air");
            for (BlockPosition cell : interior)
                require(framed.blockAt(cell.x(), cell.y(), cell.z()).equals(AIR),
                        "portal interior was not empty at " + cell);
            actor.selectHeldSlot(2);
            BlockPosition ignition = new BlockPosition(bottom.x() + 1,
                    bottom.y() + 1, bottom.z());
            actor.useHeldItemOnBlock(new BlockPosition(bottom.x() + 1,
                    bottom.y(), bottom.z()), BlockFace.UP);
            WorldlineSmokeAwait.awaitBlock(actor, ignition, FIRE, 20);
            RemoteWorldView settled = WorldlineSmokeAwait.observe(actor, 20);
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader.connect();
            reader.synchronizePose();
            RemoteWorldView fresh = reader.awaitRemoteChunk(cx, cz);
            PortalInvalidFrameFixture.Evidence evidence = PortalInvalidFrameFixture.reject(
                    settled, fresh, frame, missing, interior, true);
            require(evidence.obsidianCells() == 13 && evidence.missingAir()
                    && evidence.fireObserved() && evidence.livePortalCells() == 0
                    && evidence.persistedPortalCells() == 0,
                    "invalid portal TestKit evidence drifted");
            reader.close();
            awaitPlayers(server, 0);
            server.save();
            String bounds = bottom.x() + ":" + bottom.y() + ":" + bottom.z() + "-"
                    + (bottom.x() + 3) + ":" + (bottom.y() + 4) + ":" + bottom.z();
            String signal = "column=" + column + ",frame=" + bounds
                    + ",obsidian=13,missing=" + cell(missing, 0, 0)
                    + ",interior=6,fire=observed,portal=0,persistedPortal=0"
                    + ",flint=259,dimension=0,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|profile=allow-nether-true|fixture=stone-column+upright-obsidian49-frame4x5-missing-top-inner|"
                    + "construction=packet15-thirteen-obsidian49+one-required-air-gap|"
                    + "baseline=six-air-interior-cells|cause=packet15-flint-and-steel259+packet53-fire51|"
                    + "effect=no-portal-block90-after-20-tick-window|"
                    + "observation=live-packet53+fresh-login-packet51|"
                    + "oracle=invalid-frame-rejection-not-portal-activation|" + signal;
            System.out.println("WORLDLINE_M651_SET=" + signal);
            System.out.println("WORLDLINE_M651_TRACE=" + trace);
            System.out.println("WORLDLINE_M651_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            reader.close();
            server.close();
        }
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 10; x++) for (int z = 4; z <= 11; z++)
            for (int y = 126; y >= 1; y--)
                if (chunk.blockAt(x, y, z).legacyId() == 3
                        && water(chunk.blockAt(x, y + 1, z).legacyId()))
                    return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic invalid portal foundation");
    }
    private static B173WireClient client(int port, String user, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, user, timeout);
    }
    private static String cell(BlockPosition position, int id, int metadata) {
        return position.x() + ":" + position.y() + ":" + position.z()
                + ":" + id + ":" + metadata;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
