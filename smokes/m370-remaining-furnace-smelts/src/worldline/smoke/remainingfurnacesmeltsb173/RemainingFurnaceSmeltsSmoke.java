package worldline.smoke.remainingfurnacesmeltsb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173RemainingFurnaceSmelts;
import worldline.b173server.B173WireClient;

/** Places three idle furnaces 61:2 and smelts cactus 81, log 17, and clay 337. */
public final class RemainingFurnaceSmeltsSmoke {
    private RemainingFurnaceSmeltsSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: RemainingFurnaceSmeltsSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        RemainingFurnaceSmeltsSupport.require(user.length() <= 16, "username exceeds 16");
        Duration timeout = Duration.ofSeconds(180);
        RemoteItemStack cactus = new RemoteItemStack(81, 1, 0), log = new RemoteItemStack(17, 1, 0),
                clay = new RemoteItemStack(337, 1, 0), coal = new RemoteItemStack(263, 1, 0),
                green = new RemoteItemStack(351, 1, 2), charcoal = new RemoteItemStack(263, 1, 1),
                brick = new RemoteItemStack(336, 1, 0);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4, 5, 6, 7},
                    new int[] {1, 61, 81, 17, 337, 263, 263, 263},
                    new int[] {32, 3, 1, 1, 1, 1, 1, 1},
                    new int[] {0, 0, 0, 0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            RemoteInventoryView inventory = actor.awaitInventory();
            RemainingFurnaceSmeltsSupport.require(inventory.occupiedSlots() == 8
                    && inventory.slot(38).item().equals(cactus)
                    && inventory.slot(39).item().equals(log)
                    && inventory.slot(40).item().equals(clay)
                    && inventory.slot(41).item().equals(coal)
                    && inventory.slot(42).item().equals(coal)
                    && inventory.slot(43).item().equals(coal)
                    && !inventory.slot(38).item().equals(new RemoteItemStack(15, 1, 0)),
                    "remaining-furnace-smelts inventory drift");
            RemainingFurnaceSmeltsSupport.Raised raised = RemainingFurnaceSmeltsSupport.raise(actor,
                    actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz), cx, cz);
            actor.selectHeldSlot(1);
            BlockPosition cactusFurnace = RemainingFurnaceSmeltsSupport.furnace(actor, raised.support);
            BlockPosition logFurnace = RemainingFurnaceSmeltsSupport.furnace(actor, raised.east);
            BlockPosition clayFurnace = RemainingFurnaceSmeltsSupport.furnace(actor, raised.west);
            RemoteItemStack cactusOut = B173RemainingFurnaceSmelts.smelt(actor, cactusFurnace, 38, 41, cactus, green);
            RemoteItemStack logOut = B173RemainingFurnaceSmelts.smelt(actor, logFurnace, 39, 42, log, charcoal);
            RemoteItemStack clayOut = B173RemainingFurnaceSmelts.smelt(actor, clayFurnace, 40, 43, clay, brick);
            RemainingFurnaceSmeltsSupport.require(cactusOut.equals(green) && logOut.equals(charcoal)
                    && clayOut.equals(brick), "compound remaining furnace outputs drifted");
            actor.close();
            RemainingFurnaceSmeltsSupport.awaitPlayers(server, 0);
            String evidence = "column=" + raised.column
                    + ",support=" + RemainingFurnaceSmeltsSupport.cell(raised.support, 1, 0)
                    + ",furnaces=3x61:2,cactus=81->351:2,log=17->263:1,clay=337->336"
                    + ",cook=199,burn=1600,completion=1401,clients=1,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+3xfurnace61:2+cactus81+log17+clay337+coal263"
                    + "|cause=packet15-item61+packet102-load-81+17+337"
                    + "|wire=packet100-type2-Furnace-39+packet103-output351:2+263:1+336-slot2+packet105-cook199"
                    + "|oracle=idle-61:2+live-cactusgreen351:2+charcoal263:1+brick336|" + evidence;
            System.out.println("WORLDLINE_M370_SMELT=" + evidence);
            System.out.println("WORLDLINE_M370_TRACE=" + trace);
            System.out.println("WORLDLINE_M370_SIGNATURE=" + RemainingFurnaceSmeltsSupport.sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }
}
