package worldline.smoke.redstoneoneticksetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** One redstone-tick wall-lever pulse extends sticky 29 then retracts, leaving cobble. */
public final class RedstoneOneTickSetSmoke {
    private RedstoneOneTickSetSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 7) throw new IllegalArgumentException(
                "usage: RedstoneOneTickSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
        RedstoneOneTickSetArm.require(seed == 17320110707L && user.equals("OneTick615")
                && user.length() <= 16, "redstone-one-tick identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
        int[] column = new int[1];
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3}, new int[] {1, 29, 4, 69},
                    new int[] {32, 1, 1, 1}, new int[] {0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            actor.look(-90F, 0F);
            RedstoneOneTickSetArm.require(actor.awaitInventory().occupiedSlots() == 4,
                    "redstone-one-tick inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            RedstoneOneTickSetArm arm = RedstoneOneTickSetArm.place(actor, initial, cx, cz, column);
            arm.idle(worldline.test.WorldlineSmokeAwait.awaitBlock(actor, arm.piston,
                    RedstoneOneTickSetArm.PISTON, 8), "one-tick idle drift");
            RemoteWorldView live = arm.pulse(actor);
            actor.close();
            RedstoneOneTickSetArm.awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            arm.persist(after, cx, cz);
            String evidence = "column=" + column[0] + ",pulse=one-tick,drop=sticky-payload," + arm.cells(live)
                    + ",persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=west-sticky29+east-wall-lever69:1"
                    + "|cause=packet15-lever-cut-on-69:9"
                    + "|wire=packet53-lever69:1->9->1+sticky29-drop"
                    + "|oracle=tick-resolved-on-then-off+dropped-cobble+fresh-login|" + evidence;
            System.out.println("WORLDLINE_M615_SET=" + evidence);
            System.out.println("WORLDLINE_M615_TRACE=" + trace);
            System.out.println("WORLDLINE_M615_SIGNATURE=" + RedstoneOneTickSetArm.sha(trace));
        } finally {
            actor.close();
            if (reader != null) reader.close();
            server.close();
        }
    }
}
