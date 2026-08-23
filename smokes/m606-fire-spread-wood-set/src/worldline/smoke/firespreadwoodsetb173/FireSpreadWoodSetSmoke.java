package worldline.smoke.firespreadwoodsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Ignites netherrack fire 51 and waits for Packet53 fire in air above planks 5 or wood 17. */
public final class FireSpreadWoodSetSmoke {
    private FireSpreadWoodSetSmoke() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 9) {
            throw new IllegalArgumentException("usage: FireSpreadWoodSetSmoke server.jar workspace port "
                    + "seed username chunkX chunkZ windowTicks spreadWindows");
        }
        Path jar = Paths.get(args[0]);
        Path workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int cx = Integer.parseInt(args[5]);
        int cz = Integer.parseInt(args[6]);
        int window = Integer.parseInt(args[7]);
        int windows = Integer.parseInt(args[8]);
        FireSpreadWoodSetArm.require(seed == 17320110707L && user.equals("FireWood606")
                && user.length() <= 16 && window >= 1 && window <= 1200
                && windows >= 1 && windows <= 40, "fire-spread-wood identity drift");
        Duration timeout = Duration.ofMinutes(20);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        int[] column = new int[1];
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4}, new int[] {1, 87, 259, 5, 17},
                    new int[] {64, 1, 1, 16, 4}, new int[] {0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            FireSpreadWoodSetArm.require(actor.awaitInventory().occupiedSlots() == 5,
                    "fire-spread-wood inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            BlockPosition top = FireSpreadWoodSetArm.raise(actor, initial, cx, cz, column);
            actor.selectHeldSlot(0);
            BlockPosition[] ring = FireSpreadWoodSetArm.ring(actor, top);
            actor.selectHeldSlot(1);
            BlockPosition rack = FireSpreadWoodSetArm.place(actor, top, BlockFace.UP, 87);
            BlockPosition flame = BlockFace.UP.adjacent(rack);
            BlockPosition[] fuels = FireSpreadWoodSetArm.fuels(actor, ring);
            actor.selectHeldSlot(0);
            BlockPosition cover = FireSpreadWoodSetArm.cover(actor, ring);
            RemoteWorldView placed = WorldlineSmokeAwait.observe(actor, 5);
            FireSpreadWoodSetArm.require(FireSpreadWoodSetArm.id(placed, rack) == 87
                    && FireSpreadWoodSetArm.id(placed, cover) == 1
                    && FireSpreadWoodSetArm.id(placed, fuels[0]) == 17
                    && FireSpreadWoodSetArm.id(placed, fuels[1]) == 5,
                    "wood pad cells missing before ignition");
            actor.selectHeldSlot(2);
            actor.useHeldItemOnBlock(rack, BlockFace.UP);
            actor.awaitBlock(flame, new BlockState(51, 0));
            actor.moveAndObserve(8D, 0D, 0D, 8);
            FireSpreadWoodSetArm.waitSpread(actor, flame, fuels, window, windows);
            actor.close();
            FireSpreadWoodSetArm.awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            FireSpreadWoodSetArm.persist(after, cx, cz, rack, flame, cover, fuels);
            String evidence = "column=" + column[0] + ",support="
                    + FireSpreadWoodSetArm.token(top, 1, 0) + ",rack="
                    + FireSpreadWoodSetArm.token(rack, 87, 0)
                    + ",flint=259,source-fire=" + FireSpreadWoodSetArm.cell(flame)
                    + ":51,wood-ring=8,cover=" + FireSpreadWoodSetArm.token(cover, 1, 0)
                    + ",fuels=5+17,spread=air->51,source-stay=true,persisted=true"
                    + ",clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-netherrack87+planks5-ring+wood17+cover1+flintsteel259"
                    + "|cause=packet15-item259+random-ticks|wire=packet53-fire51-spread-air"
                    + "|oracle=live-source-fire51+adjacent-wood-plank-spread+fresh-login|"
                    + evidence;
            System.out.println("WORLDLINE_M606_SET=" + evidence);
            System.out.println("WORLDLINE_M606_TRACE=" + trace);
            System.out.println("WORLDLINE_M606_SIGNATURE=" + FireSpreadWoodSetArm.sha(trace));
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
    }
}
