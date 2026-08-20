package worldline.smoke.furnacefuelsetb173;

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

/** Places three idle furnaces 61:2 and smelts cobble 4→stone 1 with coal, planks, and lava. */
public final class FurnaceFuelSetSmoke {
    private FurnaceFuelSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: FurnaceFuelSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        FurnaceFuelSetSupport.require(user.length() <= 16, "username exceeds 16");
        Duration timeout = Duration.ofSeconds(180);
        RemoteItemStack cobble = new RemoteItemStack(4, 1, 0), coal = new RemoteItemStack(263, 1, 0),
                planks = new RemoteItemStack(5, 1, 0), lava = new RemoteItemStack(327, 1, 0),
                stone = new RemoteItemStack(1, 1, 0);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4, 5, 6, 7},
                    new int[] {1, 61, 4, 4, 4, 263, 5, 327},
                    new int[] {32, 3, 1, 1, 1, 1, 1, 1},
                    new int[] {0, 0, 0, 0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            RemoteInventoryView inventory = actor.awaitInventory();
            FurnaceFuelSetSupport.require(inventory.occupiedSlots() == 8
                    && inventory.slot(38).item().equals(cobble)
                    && inventory.slot(39).item().equals(cobble)
                    && inventory.slot(40).item().equals(cobble)
                    && inventory.slot(41).item().equals(coal)
                    && inventory.slot(42).item().equals(planks)
                    && inventory.slot(43).item().equals(lava)
                    && !inventory.slot(38).item().equals(new RemoteItemStack(15, 1, 0)),
                    "furnace-fuel-set inventory drift");
            FurnaceFuelSetSupport.Raised raised = FurnaceFuelSetSupport.raise(actor,
                    actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz), cx, cz);
            actor.selectHeldSlot(1);
            BlockPosition coalFurnace = FurnaceFuelSetSupport.furnace(actor, raised.support);
            BlockPosition plankFurnace = FurnaceFuelSetSupport.furnace(actor, raised.east);
            BlockPosition lavaFurnace = FurnaceFuelSetSupport.furnace(actor, raised.west);
            RemoteFurnaceSmelt coalSmelt = FurnaceFuelSetClicks.smelt(actor, coalFurnace, 38, 41, coal, 1600);
            RemoteFurnaceSmelt plankSmelt = FurnaceFuelSetClicks.smelt(actor, plankFurnace, 39, 42, planks, 300);
            RemoteFurnaceSmelt lavaSmelt = FurnaceFuelSetClicks.smelt(actor, lavaFurnace, 40, 43, lava, 20000);
            FurnaceFuelSetSupport.require(coalSmelt.output().equals(stone) && plankSmelt.output().equals(stone)
                    && lavaSmelt.output().equals(stone)
                    && coalSmelt.maximumBurn() == 1600 && plankSmelt.maximumBurn() == 300
                    && lavaSmelt.maximumBurn() == 20000, "compound furnace fuels drifted");
            actor.close();
            FurnaceFuelSetSupport.awaitPlayers(server, 0);
            String evidence = "column=" + raised.column
                    + ",support=" + FurnaceFuelSetSupport.cell(raised.support, 1, 0)
                    + ",coal=" + FurnaceFuelSetSupport.cell(coalFurnace, 61, 2)
                    + ",planks=" + FurnaceFuelSetSupport.cell(plankFurnace, 61, 2)
                    + ",lava=" + FurnaceFuelSetSupport.cell(lavaFurnace, 61, 2)
                    + ",input=4->1,fuels=263+5+327,coal=1600:1401,planks=300:101,lava=20000:19801"
                    + ",out=" + FurnaceFuelSetSupport.item(stone)
                    + ",cook=199,clients=1,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+3xfurnace61:2+cobble4+coal263+planks5+lava327"
                    + "|cause=packet15-item61+packet102-load-4+263+5+327"
                    + "|wire=packet100-type2-Furnace-39+packet103-output1-slot2+packet105-cook199-burn1600+300+20000"
                    + "|oracle=idle-61:2+live-stone1-fuels-263+5+327|" + evidence;
            System.out.println("WORLDLINE_M338_FUEL=" + evidence);
            System.out.println("WORLDLINE_M338_TRACE=" + trace);
            System.out.println("WORLDLINE_M338_SIGNATURE=" + FurnaceFuelSetSupport.sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }
}
