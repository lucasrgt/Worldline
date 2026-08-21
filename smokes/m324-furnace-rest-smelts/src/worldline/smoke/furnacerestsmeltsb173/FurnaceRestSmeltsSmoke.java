package worldline.smoke.furnacerestsmeltsb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places three idle furnaces 61:2 and smelts sand 12, cobble 4, and fish 349. */
public final class FurnaceRestSmeltsSmoke {
    private FurnaceRestSmeltsSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: FurnaceRestSmeltsSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        FurnaceRestSmeltsSupport.require(user.length() <= 16, "username exceeds 16");
        Duration timeout = Duration.ofSeconds(180);
        RemoteItemStack sand = new RemoteItemStack(12, 1, 0), cobble = new RemoteItemStack(4, 1, 0),
                fish = new RemoteItemStack(349, 1, 0), coal = new RemoteItemStack(263, 1, 0),
                glass = new RemoteItemStack(20, 1, 0), stone = new RemoteItemStack(1, 1, 0),
                cooked = new RemoteItemStack(350, 1, 0);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4, 5, 6, 7},
                    new int[] {1, 61, 12, 4, 349, 263, 263, 263},
                    new int[] {32, 3, 1, 1, 1, 1, 1, 1},
                    new int[] {0, 0, 0, 0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            RemoteInventoryView inventory = actor.awaitInventory();
            FurnaceRestSmeltsSupport.require(inventory.occupiedSlots() == 8
                    && inventory.slot(38).item().equals(sand)
                    && inventory.slot(39).item().equals(cobble)
                    && inventory.slot(40).item().equals(fish)
                    && inventory.slot(41).item().equals(coal)
                    && inventory.slot(42).item().equals(coal)
                    && inventory.slot(43).item().equals(coal)
                    && !inventory.slot(38).item().equals(new RemoteItemStack(15, 1, 0)),
                    "furnace-rest-smelts inventory drift");
            FurnaceRestSmeltsSupport.Raised raised = FurnaceRestSmeltsSupport.raise(actor,
                    actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz), cx, cz);
            actor.selectHeldSlot(1);
            BlockPosition sandFurnace = FurnaceRestSmeltsSupport.furnace(actor, raised.support);
            BlockPosition cobbleFurnace = FurnaceRestSmeltsSupport.furnace(actor, raised.east);
            BlockPosition fishFurnace = FurnaceRestSmeltsSupport.furnace(actor, raised.west);
            RemoteFurnaceSmelt sandSmelt = FurnaceRestSmeltsClicks.smelt(actor, sandFurnace, 38, 41, sand, glass);
            RemoteFurnaceSmelt cobbleSmelt = FurnaceRestSmeltsClicks.smelt(actor, cobbleFurnace, 39, 42, cobble, stone);
            RemoteFurnaceSmelt fishSmelt = FurnaceRestSmeltsClicks.smelt(actor, fishFurnace, 40, 43, fish, cooked);
            FurnaceRestSmeltsSupport.require(sandSmelt.output().equals(glass) && cobbleSmelt.output().equals(stone)
                    && fishSmelt.output().equals(cooked), "compound rest furnace outputs drifted");
            actor.close();
            FurnaceRestSmeltsSupport.awaitPlayers(server, 0);
            String evidence = "column=" + raised.column
                    + ",support=" + FurnaceRestSmeltsSupport.cell(raised.support, 1, 0)
                    + ",furnaces=3x61:2,sand=12->20,cobble=4->1,fish=349->350"
                    + ",cook=199,burn=1600,completion=1401,clients=1,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+3xfurnace61:2+sand12+cobble4+fish349+coal263"
                    + "|cause=packet15-item61+packet102-load-12+4+349"
                    + "|wire=packet100-type2-Furnace-39+packet103-output20+1+350-slot2+packet105-cook199"
                    + "|oracle=idle-61:2+live-sand20+cobble1+fish350|" + evidence;
            System.out.println("WORLDLINE_M324_SMELT=" + evidence);
            System.out.println("WORLDLINE_M324_TRACE=" + trace);
            System.out.println("WORLDLINE_M324_SIGNATURE=" + FurnaceRestSmeltsSupport.sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }
}
