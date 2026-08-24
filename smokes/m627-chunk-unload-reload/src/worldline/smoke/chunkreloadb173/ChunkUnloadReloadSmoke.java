package worldline.smoke.chunkreloadb173;

import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.place;
import static worldline.b173server.B173FixtureSupport.sha;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.RemoteChunkUnload;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteFurnaceLoad;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWindowKind;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.ChunkReloadFixture;

/** Proves three persistent states across one official in-process chunk unload and reload. */
public final class ChunkUnloadReloadSmoke {
    private static final RemoteItemStack DIRT = new RemoteItemStack(3, 1, 0);
    private static final RemoteItemStack GLASS = new RemoteItemStack(20, 1, 0);
    private ChunkUnloadReloadSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 12) throw new IllegalArgumentException(
                "usage: ChunkUnloadReloadSmoke server.jar workspace port seed actor far reader "
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
                "chunk reload fixture drift");
        Outcome outcome = run(jar, workspace, port, seed, actor, far, reader,
                chunkX, chunkZ, farX, farZ, settleTicks);
        String signal = "chunk=20:20,unload=packet50,reload=fresh-client,furnace=62+glass20,"
                + "item=3:1:0,minecart=type10,identity=rotated,replicas=2,disconnect=clean";
        String trace = "v1|server=official-b1.7.3|seed=17320110707"
                + "|fixture=chunk20:20+far40:40+lit-furnace62+item3+minecart10"
                + "|cause=op-tp-beyond-view+packet50-unload+100-tick-absence+fresh-client-reload"
                + "|oracle=chunk-lifecycle-persistence|" + signal;
        System.out.println("WORLDLINE_M627_EVIDENCE=" + outcome.text());
        System.out.println("WORLDLINE_M627_SET=" + signal);
        System.out.println("WORLDLINE_M627_TRACE=" + trace);
        System.out.println("WORLDLINE_M627_SIGNATURE=" + sha(trace));
    }

    private static Outcome run(Path jar, Path workspace, int port, long seed, String actorName,
            String farName, String readerName, int chunkX, int chunkZ, int farX, int farZ,
            int settleTicks) throws Exception {
        Duration timeout = Duration.ofSeconds(120);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed,
                timeout, 3, true);
        B173WireClient actor = client(port, actorName, timeout);
        B173WireClient far = client(port, farName, timeout);
        B173WireClient reader = client(port, readerName, timeout);
        try {
            server.boot();
            seedActor(workspace, actorName, chunkX, chunkZ);
            B173PlayerSeed.write(workspace, farName, farX * 16 + 8.5D, 100D, farZ * 16 + 8.5D);
            server.operator(actorName);
            actor.connect();
            actor.synchronizePose();
            require(actor.awaitInventory().occupiedSlots() == 7, "actor inventory drift");
            actor.awaitRemoteChunk(chunkX, chunkZ);
            BlockPosition support = settle(actor);
            require(!actor.moveAndObserve(0D, 3D, 0D, 3).corrected(),
                    "fixture clearance failed");
            Fixture fixture = build(actor, support);
            RemoteObjectSpawn cartBefore = spawnCart(actor, fixture.rail);
            RemoteDroppedItem itemBefore = dropItem(actor);
            BlockState burningBefore = burn(actor, fixture.furnace);
            far.connect();
            far.synchronizePose();
            far.awaitInventory();
            actor.sendChat("/tp " + actorName + " " + farName);
            RemoteChunkUnload unload = actor.awaitRemoteChunkUnload(chunkX, chunkZ);
            far.close();
            worldline.test.WorldlineSmokeAwait.observe(actor, settleTicks);
            B173PlayerSeed.write(workspace, readerName, fixture.returnPad.x() + .5D,
                    fixture.returnPad.y() + 1D, fixture.returnPad.z() + .5D);
            reader.connect();
            reader.synchronizePose();
            reader.awaitInventory();
            reader.awaitRemoteChunk(chunkX, chunkZ);
            RemoteDroppedItem itemAfter = reader.awaitDroppedItem(DIRT);
            RemoteObjectSpawn cartAfter = reader.awaitObjectSpawn(10);
            BlockState burningAfter = worldline.test.WorldlineSmokeAwait.observe(reader, 3)
                    .blockAt(fixture.furnace.x(), fixture.furnace.y(), fixture.furnace.z());
            RemoteContainerWindow reopened = reader.openFurnace(fixture.furnace, BlockFace.UP);
            require(reopened.descriptor().kind() == RemoteWindowKind.FURNACE
                    && !reopened.inventory().slot(2).empty(), "reloaded furnace output absent");
            RemoteItemStack output = reopened.inventory().slot(2).item();
            reader.closeWindow();
            ChunkReloadFixture.Evidence proof = ChunkReloadFixture.observe(chunkX, chunkZ,
                    fixture.furnace, burningBefore, burningAfter, output, itemBefore, itemAfter,
                    cartBefore, cartAfter, unload);
            actor.close();
            reader.close();
            awaitPlayers(server, 0);
            server.save();
            return new Outcome(proof, unload, itemBefore, itemAfter, cartBefore, cartAfter);
        } finally {
            actor.close();
            far.close();
            reader.close();
            server.close();
        }
    }

    private static Fixture build(B173WireClient actor, BlockPosition support) throws Exception {
        actor.selectHeldSlot(0);
        BlockPosition top = place(actor, support, BlockFace.UP, 1);
        for (int lift = 0; lift < 5; lift++) {
            top = place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
        }
        BlockPosition furnacePad = place(actor, top, BlockFace.EAST, 1);
        BlockPosition east = place(actor, furnacePad, BlockFace.EAST, 1);
        BlockPosition returnPad = place(actor, east, BlockFace.EAST, 1);
        BlockPosition railPad = place(actor, top, BlockFace.WEST, 1);
        actor.selectHeldSlot(1);
        BlockPosition furnace = M627FixtureSupport.placeLegacy(actor, furnacePad, BlockFace.UP, 61);
        actor.selectHeldSlot(4);
        BlockPosition rail = place(actor, railPad, BlockFace.UP, 66);
        return new Fixture(furnace, rail, returnPad);
    }

    private static BlockState burn(B173WireClient actor, BlockPosition furnace) throws Exception {
        actor.selectHeldSlot(1);
        RemoteContainerWindow opened = actor.openFurnace(furnace, BlockFace.UP);
        require(opened.inventory().slot(32).item().equals(new RemoteItemStack(12, 1, 0))
                && opened.inventory().slot(33).item().equals(new RemoteItemStack(263, 1, 0)),
                "furnace ingredients drift");
        RemoteFurnaceLoad load = actor.loadFurnace(38, 39);
        require(load.input().equals(new RemoteItemStack(12, 1, 0))
                && load.fuel().equals(new RemoteItemStack(263, 1, 0)), "furnace load drift");
        RemoteFurnaceSmelt smelt = actor.awaitFurnaceSmelt();
        require(smelt.output().equals(GLASS) && smelt.completionBurn() == 1401,
                "furnace smelt drift");
        actor.closeWindow();
        BlockState state = worldline.test.WorldlineSmokeAwait.observe(actor, 3)
                .blockAt(furnace.x(), furnace.y(), furnace.z());
        require(state.legacyId() == 62, "furnace was not burning before unload");
        return state;
    }

    private static RemoteObjectSpawn spawnCart(B173WireClient actor, BlockPosition rail) {
        actor.selectHeldSlot(5);
        actor.useHeldItemOnBlock(rail, BlockFace.UP);
        RemoteObjectSpawn cart = actor.awaitObjectSpawn(10);
        require(cart.type() == 10 && cart.throwerId() == 0, "minecart spawn drift");
        return cart;
    }

    private static RemoteDroppedItem dropItem(B173WireClient actor) {
        actor.selectHeldSlot(6);
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
                chunkZ * 16 + 8.5D, new int[] {0, 1, 2, 3, 4, 5, 6},
                new int[] {1, 61, 12, 263, 66, 328, 3},
                new int[] {32, 1, 1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0, 0, 0});
    }
    private static B173WireClient client(int port, String user, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, user, timeout);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class Fixture {
        final BlockPosition furnace, rail, returnPad;
        Fixture(BlockPosition furnace, BlockPosition rail, BlockPosition returnPad) {
            this.furnace = furnace;
            this.rail = rail;
            this.returnPad = returnPad;
        }
    }
    private static final class Outcome {
        final ChunkReloadFixture.Evidence proof;
        final RemoteChunkUnload unload;
        final RemoteDroppedItem beforeItem, afterItem;
        final RemoteObjectSpawn beforeCart, afterCart;
        Outcome(ChunkReloadFixture.Evidence proof, RemoteChunkUnload unload,
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
            return "chunk=" + proof.chunkX() + ":" + proof.chunkZ() + ",unloadRemaining="
                    + unload.remainingTrackedChunks() + ",furnace=" + proof.furnace()
                    + ",output=" + proof.furnaceOutput() + ",itemIds=" + beforeItem.entityId()
                    + "->" + afterItem.entityId() + ",cartIds=" + beforeCart.entityId()
                    + "->" + afterCart.entityId();
        }
    }
}
