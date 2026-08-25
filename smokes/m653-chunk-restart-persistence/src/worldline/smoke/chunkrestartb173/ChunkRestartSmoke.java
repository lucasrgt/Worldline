package worldline.smoke.chunkrestartb173;

import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.sha;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.MovementOutcome;
import worldline.api.RemoteChestTransfer;
import worldline.api.RemoteChunkUnload;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.ChunkRestartFixture;

/** Proves chest inventory, dropped item, and minecart persistence across one full server restart. */
public final class ChunkRestartSmoke {
    private static final RemoteItemStack DIRT = new RemoteItemStack(3, 1, 0);
    private static final RemoteItemStack GLASS = new RemoteItemStack(20, 1, 0);
    private ChunkRestartSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 12) throw new IllegalArgumentException(
                "usage: ChunkRestartSmoke server.jar workspace port seed actor far reader "
                        + "chunkX chunkZ farX farZ settleTicks");
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String actor = arguments[4], far = arguments[5], reader = arguments[6];
        int chunkX = Integer.parseInt(arguments[7]), chunkZ = Integer.parseInt(arguments[8]);
        int farX = Integer.parseInt(arguments[9]), farZ = Integer.parseInt(arguments[10]);
        int settleTicks = Integer.parseInt(arguments[11]);
        require(seed == 17320110707L && chunkX == 20 && chunkZ == 20
                && farX == 40 && farZ == 40 && settleTicks == 100,
                "chunk restart fixture drift");
        Outcome outcome = run(jar, workspace, port, seed, actor, far, reader,
                chunkX, chunkZ, farX, farZ, settleTicks);
        String signal = "chunk=20:20,unload=packet50,stop=graceful,restart=new-process,"
                + "reload=fresh-client,chest=glass20,item=3:1:0,minecart=type10,"
                + "identity=normalized,replicas=2,disconnect=clean";
        String trace = "v1|server=official-b1.7.3|servers=2|seed=17320110707"
                + "|fixture=chunk20:20+chest54+glass20+item3+minecart10"
                + "|cause=op-tp-beyond-view+packet50-unload+100-tick-absence+graceful-stop"
                + "+new-jvm-boot+fresh-client-reload"
                + "|oracle=chunk-restart-persistence|" + signal;
        System.out.println("WORLDLINE_M653_EVIDENCE=" + outcome.text());
        System.out.println("WORLDLINE_M653_SET=" + signal);
        System.out.println("WORLDLINE_M653_TRACE=" + trace);
        System.out.println("WORLDLINE_M653_SIGNATURE=" + sha(trace));
    }

    private static Outcome run(Path jar, Path workspace, int port, long seed, String actorName,
            String farName, String readerName, int chunkX, int chunkZ, int farX, int farZ,
            int settleTicks) throws Exception {
        Duration timeout = Duration.ofSeconds(120);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed,
                timeout, 3, true);
        B173WireClient actor = client(port, actorName, timeout);
        B173WireClient far = client(port, farName, timeout);
        try {
            server.boot();
            seedActor(workspace, actorName, chunkX, chunkZ);
            B173PlayerSeed.write(workspace, farName, farX * 16 + 8.5D, 100D, farZ * 16 + 8.5D);
            server.operator(actorName);
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 6, "actor inventory drift");
            actor.awaitRemoteChunk(chunkX, chunkZ);
            BlockPosition support = settle(actor);
            require(!actor.moveAndObserve(0D, 3D, 0D, 3).corrected(), "fixture clearance failed");
            M653FixtureSupport.Fixture fixture = M653FixtureSupport.build(actor, support);
            RemoteObjectSpawn cartBefore = M653FixtureSupport.spawnCart(actor, fixture.rail);
            RemoteDroppedItem itemBefore = dropItem(actor);
            storeGlass(actor, fixture.chest);
            far.connect();
            far.synchronizePose();
            far.awaitInventory();
            actor.sendChat("/tp " + actorName + " " + farName);
            RemoteChunkUnload unload = actor.awaitRemoteChunkUnload(chunkX, chunkZ);
            far.close();
            worldline.test.WorldlineSmokeAwait.observe(actor, settleTicks);
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            server.close();
            return restart(jar, workspace, port, seed, readerName, fixture, unload,
                    itemBefore, cartBefore, timeout);
        } finally {
            actor.close();
            far.close();
            server.close();
        }
    }

    private static Outcome restart(Path jar, Path workspace, int port, long seed,
            String readerName, M653FixtureSupport.Fixture fixture, RemoteChunkUnload unload,
            RemoteDroppedItem itemBefore, RemoteObjectSpawn cartBefore, Duration timeout)
                    throws Exception {
        B173DedicatedServer restarted = new B173DedicatedServer(jar, workspace, port, seed,
                timeout, 3, true);
        B173WireClient reader = client(port, readerName, timeout);
        try {
            restarted.boot();
            B173PlayerSeed.write(workspace, readerName, fixture.returnPad.x() + .5D,
                    fixture.returnPad.y() + 1D, fixture.returnPad.z() + .5D);
            reader.connect();
            reader.synchronizePose();
            reader.awaitInventory();
            reader.awaitRemoteChunk(20, 20);
            RemoteDroppedItem itemAfter = reader.awaitDroppedItem(DIRT);
            RemoteObjectSpawn cartAfter = reader.awaitObjectSpawn(10);
            RemoteContainerWindow reopened = reader.openChest(fixture.chest, BlockFace.UP);
            require(reopened.descriptor().kind() == worldline.api.RemoteWindowKind.CHEST,
                    "restarted chest window drift");
            reader.closeWindow();
            ChunkRestartFixture.Evidence proof = ChunkRestartFixture.await(20, 20,
                    fixture.chest, GLASS, reopened, itemBefore, itemAfter,
                    cartBefore, cartAfter, unload);
            reader.close();
            awaitPlayers(restarted, 0);
            restarted.save();
            return new Outcome(proof, unload, itemBefore, itemAfter, cartBefore, cartAfter);
        } finally {
            reader.close();
            restarted.close();
        }
    }

    private static void storeGlass(B173WireClient actor, BlockPosition chest) throws Exception {
        actor.selectHeldSlot(6);
        RemoteContainerWindow opened = actor.openChest(chest, BlockFace.UP);
        require(opened.inventory().slot(59).item().equals(GLASS)
                && opened.inventory().slot(0).empty(), "chest window mapping drift");
        RemoteChestTransfer stored = actor.storeInOpenChest(41, 0);
        require(stored.takeAction() == 1 && stored.storeAction() == 2
                && stored.after().slot(0).item().equals(GLASS), "chest glass deposit drift");
        actor.closeWindow();
    }

    private static RemoteDroppedItem dropItem(B173WireClient actor) {
        actor.selectHeldSlot(4);
        actor.dropHeldItem();
        return actor.awaitDroppedItem(DIRT);
    }

    private static BlockPosition settle(B173WireClient actor) {
        MovementOutcome settled = null;
        for (int step = 0; step < 80; step++) {
            settled = actor.moveAndObserve(0D, -1D, 0D, 2);
            if (settled.corrected()) break;
        }
        require(settled != null && settled.corrected(), "ground settlement correction absent");
        return new BlockPosition((int) Math.floor(settled.resulting().x()),
                (int) Math.floor(settled.resulting().y()) - 1,
                (int) Math.floor(settled.resulting().z()));
    }

    private static void seedActor(Path workspace, String user, int chunkX, int chunkZ) {
        B173PlayerSeed.writeInventory(workspace, user, chunkX * 16 + 8.5D, 100D,
                chunkZ * 16 + 8.5D, new int[] {0, 1, 2, 3, 4, 5},
                new int[] {1, 54, 66, 328, 3, 20},
                new int[] {32, 1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0, 0});
    }
    private static B173WireClient client(int port, String user, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, user, timeout);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class Outcome {
        final ChunkRestartFixture.Evidence proof;
        final RemoteChunkUnload unload;
        final RemoteDroppedItem beforeItem, afterItem;
        final RemoteObjectSpawn beforeCart, afterCart;
        Outcome(ChunkRestartFixture.Evidence proof, RemoteChunkUnload unload,
                RemoteDroppedItem beforeItem, RemoteDroppedItem afterItem,
                RemoteObjectSpawn beforeCart, RemoteObjectSpawn afterCart) {
            this.proof = proof;
            this.unload = unload;
            this.beforeItem = beforeItem;
            this.afterItem = afterItem;
            this.beforeCart = beforeCart;
            this.afterCart = afterCart;
        }
        String text() {
            return "chunk=" + proof.chunkX() + ":" + proof.chunkZ()
                    + ",unloadRemaining=" + unload.remainingTrackedChunks()
                    + ",chest=" + proof.stored() + ",item=" + proof.item()
                    + ",itemIds=" + beforeItem.entityId() + "->" + afterItem.entityId()
                    + ",cartIds=" + beforeCart.entityId() + "->" + afterCart.entityId();
        }
    }
}
