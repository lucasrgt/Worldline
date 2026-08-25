package worldline.smoke.chestaccessconstraintsb173;

import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;
import worldline.testkit.ChestAccessFixture;

/** Proves solid-lid access blocking and third-chest rejection on the official server. */
public final class ChestAccessConstraintsSmoke {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STONE = new BlockState(1, 0);
    private static final BlockState CHEST = new BlockState(54, 0);
    private ChestAccessConstraintsSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: ChestAccessConstraintsSmoke server.jar workspace port seed user chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        require(seed == 17320110707L && "ChestGate649".equals(user)
                && cx == 0 && cz == 0, "chest access fixture identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = client(port, user, timeout);
        B173WireClient reader = client(port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1}, new int[] {1, 54},
                    new int[] {64, 5}, new int[] {0, 0});
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 2, "chest inventory drift");
            BlockPosition[] pillars = new BlockPosition[8];
            int column = raise(actor, actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz),
                    cx, cz, pillars);
            actor.moveAndObserve(0D, 0D, 1D, 2);
            for (int step = 0; step < 7; step++) actor.moveAndObserve(-1D, 0D, 0D, 2);
            actor.selectHeldSlot(1);
            BlockPosition control = place(actor, pillars[0], BlockFace.UP, 54);
            actor.selectHeldSlot(2);
            RemoteContainerWindow single = open(actor, control);
            require(window(single).matches("Chest", 27, 63),
                    "live single chest window drifted");
            actor.selectHeldSlot(1);
            BlockPosition blocked = place(actor, pillars[3], BlockFace.UP, 54);
            for (int step = 0; step < 3; step++) actor.moveAndObserve(1D, 0D, 0D, 2);
            actor.selectHeldSlot(0);
            BlockPosition fill = place(actor, pillars[2], BlockFace.UP, 1);
            BlockPosition stack = place(actor, fill, BlockFace.UP, 1);
            BlockPosition lid = place(actor, stack, BlockFace.EAST, 1);
            WorldlineSmokeAwait.awaitBlock(actor, blocked, CHEST, 20);
            WorldlineSmokeAwait.awaitBlock(actor, lid, STONE, 20);
            actor.selectHeldSlot(2);
            actor.activateBlock(blocked, BlockFace.UP);
            RemoteWorldView blockedWindow = WorldlineSmokeAwait.observe(actor, 30);
            require(blockedWindow.blockAt(blocked.x(), blocked.y(), blocked.z()).equals(CHEST)
                    && blockedWindow.blockAt(lid.x(), lid.y(), lid.z()).equals(STONE),
                    "lidded chest topology drifted during bounded Packet100 absence");
            for (int step = 0; step < 3; step++) actor.moveAndObserve(1D, 0D, 0D, 2);
            actor.selectHeldSlot(1);
            BlockPosition left = place(actor, pillars[6], BlockFace.UP, 54);
            BlockPosition right = place(actor, pillars[7], BlockFace.UP, 54);
            actor.selectHeldSlot(2);
            RemoteContainerWindow pair = open(actor, left);
            require(window(pair).matches("Large chest", 54, 90),
                    "live double chest window drifted");
            BlockPosition third = BlockFace.UP.adjacent(pillars[5]);
            actor.selectHeldSlot(1);
            actor.placeHeldBlock(pillars[5], BlockFace.UP);
            RemoteWorldView rejected = WorldlineSmokeAwait.observe(actor, 30);
            RemoteInventoryView inventory = actor.awaitInventory();
            require(rejected.blockAt(third.x(), third.y(), third.z()).equals(AIR)
                    && inventory.slot(37).item().equals(new RemoteItemStack(54, 1, 0)),
                    "third adjacent chest placement was not rejected");
            actor.selectHeldSlot(2);
            RemoteContainerWindow unchanged = open(actor, right);
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            require(cell(after, control, cx, cz).equals(CHEST)
                    && cell(after, blocked, cx, cz).equals(CHEST)
                    && cell(after, lid, cx, cz).equals(STONE)
                    && cell(after, left, cx, cz).equals(CHEST)
                    && cell(after, right, cx, cz).equals(CHEST)
                    && cell(after, third, cx, cz).equals(AIR),
                    "persisted chest access topology drifted");
            reader.selectHeldSlot(2);
            RemoteContainerWindow freshSingle = open(reader, control);
            RemoteContainerWindow freshLarge = open(reader, left);
            ChestAccessFixture.Evidence evidence = ChestAccessFixture.verify(
                    reader.awaitRemoteChunk(cx, cz),
                    new ChestAccessFixture.Sites(control, blocked, lid, left, right, third),
                    window(freshSingle), true, window(freshLarge), true);
            require(evidence.blocked() && evidence.thirdRejected()
                    && window(pair).shape().equals(window(unchanged).shape()),
                    "chest access TestKit evidence drifted");
            reader.close();
            awaitPlayers(server, 0);
            server.save();
            String signal = "column=" + column + ",support=" + cell(pillars[0], 1, 0)
                    + ",control=" + cell(control, 54, 0)
                    + ",single=title=Chest,owned=27,total=63"
                    + ",lid=" + cell(lid, 1, 0) + ",blocked=" + cell(blocked, 54, 0)
                    + ",open=absent-30,left=" + cell(left, 54, 0)
                    + ",right=" + cell(right, 54, 0)
                    + ",double=title=Large chest,owned=54,total=90"
                    + ",third=" + cell(third, 0, 0)
                    + ",rejected=true,held=54:1,persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone-row-four-chest54+lid1+air-third|"
                    + "cause=packet100-single27+packet7-lidded-nowindow30"
                    + "+packet100-large54+packet15-third-rejected|"
                    + "wire=packet53-chest54x4+stone1-lid+air-third+packet100-max-owned54|"
                    + "oracle=chest-access-constraints|" + signal;
            System.out.println("WORLDLINE_M649_SET=" + signal);
            System.out.println("WORLDLINE_M649_TRACE=" + trace);
            System.out.println("WORLDLINE_M649_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            reader.close();
            server.close();
        }
    }

    private static int raise(B173WireClient actor, RemoteChunkSnapshot chunk, int cx, int cz,
            BlockPosition[] pillars) throws Exception {
        BlockPosition top = foundation(chunk, cx, cz);
        int column = 0;
        actor.selectHeldSlot(0);
        while (water(cell(chunk, BlockFace.UP.adjacent(top), cx, cz).legacyId())) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded chest access fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column++;
        }
        pillars[0] = top;
        for (int east = 1; east < pillars.length; east++) {
            pillars[east] = place(actor, pillars[east - 1], BlockFace.EAST, 1);
            actor.moveAndObserve(1D, 0D, 0D, 2);
        }
        return column;
    }
    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
        for (int x = 4; x <= 11; x++)
            for (int z = 4; z <= 11; z++)
                for (int y = 126; y >= 1; y--)
                    if (chunk.blockAt(x, y, z).legacyId() == 3
                            && water(chunk.blockAt(x, y + 1, z).legacyId()))
                        return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
        throw new IllegalStateException("no deterministic chest access foundation");
    }
    private static RemoteContainerWindow open(B173WireClient client, BlockPosition position)
            throws Exception {
        RemoteContainerWindow window = client.openChest(position, BlockFace.UP);
        client.closeWindow();
        return window;
    }
    private static ChestAccessFixture.Window window(RemoteContainerWindow value) {
        RemoteWindowDescriptor descriptor = value.descriptor();
        return new ChestAccessFixture.Window(descriptor.title(), descriptor.containerSlots(),
                value.inventory().size());
    }
    private static BlockState cell(RemoteChunkSnapshot chunk, BlockPosition position,
            int cx, int cz) {
        return chunk.blockAt(local(position.x(), cx), position.y(), local(position.z(), cz));
    }
    private static String cell(BlockPosition position, int id, int metadata) {
        return position.x() + ":" + position.y() + ":" + position.z()
                + ":" + id + ":" + metadata;
    }
    private static B173WireClient client(int port, String user, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, user, timeout);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
