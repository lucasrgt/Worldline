package worldline.smoke.pistonpushentitysetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Official piston 33 extends into air and displaces a cobble item entity west. */
public final class PistonPushEntitySetSmoke {
    static final RemoteItemStack COBBLE = new RemoteItemStack(4, 1, 0);

    private PistonPushEntitySetSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 9) throw new IllegalArgumentException(
                "usage: PistonPushEntitySetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
        int fixture = Integer.parseInt(args[7]), signal = Integer.parseInt(args[8]);
        PistonPushEntitySetArm.require(seed == 17320110707L && user.equals("PistEnt612")
                && user.length() <= 16, "piston-push-entity identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
        PistonPushEntitySetArm arm;
        int[] column = new int[1];
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3}, new int[] {1, 33, 69, 4},
                    new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            actor.look(-90F, 0F);
            PistonPushEntitySetArm.require(actor.awaitInventory().occupiedSlots() == 4,
                    "piston-push-entity inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            arm = PistonPushEntitySetArm.place(actor, initial, cx, cz, column);
            RemoteDroppedItem spawn = arm.dropCobble(actor);
            arm.installPiston(actor);
            worldline.test.WorldlineSmokeAwait.observe(actor, fixture);
            PistonPushEntitySetArm.require(actor.peekDroppedItem(COBBLE) != null
                    && actor.peekDroppedItem(COBBLE).entityId() == spawn.entityId(),
                    "dropped cobble despawned before piston extend");
            arm.extend(actor, signal);
            actor.close();
            PistonPushEntitySetArm.awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteDroppedItem after = reader.awaitDroppedItem(COBBLE);
            PistonPushEntitySetArm.displaced(spawn, after);
            RemoteChunkSnapshot persisted = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            arm.persist(persisted, cx, cz);
            String evidence = "column=" + column[0]
                    + ",extend=33:4->12,head=0:0->34:4,item=4x1:0,dx-west=true,piston="
                    + PistonPushEntitySetArm.cell(arm.piston) + ":33:4->12,head-cell="
                    + PistonPushEntitySetArm.cell(arm.head) + ":0:0->34:4,pushed="
                    + PistonPushEntitySetArm.cell(arm.pushed)
                    + ":0:0,persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=piston33-west+air-head+cobble-item4+floors"
                    + "|settle=" + fixture + "+" + signal + "ticks"
                    + "|cause=packet14-status4-cobble+packet15-lever-activate"
                    + "|effect=official-piston33-extend+item-entity-west"
                    + "|observation=fresh-login-packet21+packet51|" + evidence;
            System.out.println("WORLDLINE_M612_SET=" + evidence);
            System.out.println("WORLDLINE_M612_TRACE=" + trace);
            System.out.println("WORLDLINE_M612_SIGNATURE=" + PistonPushEntitySetArm.sha(trace));
        } finally {
            actor.close();
            if (reader != null) reader.close();
            server.close();
        }
    }
}
