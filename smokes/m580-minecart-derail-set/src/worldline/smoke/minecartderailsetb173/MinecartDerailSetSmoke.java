package worldline.smoke.minecartderailsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Official type-10 minecart leaves rail 66 and occupies wooden plate 72. */
public final class MinecartDerailSetSmoke {
    private MinecartDerailSetSmoke() {}

    public static void main(String[] a) throws Exception {
        if (a.length != 7)
            throw new IllegalArgumentException(
                    "usage: MinecartDerailSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
        int port = Integer.parseInt(a[2]);
        long seed = Long.parseLong(a[3]);
        String user = a[4];
        int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
        Duration timeout = Duration.ofSeconds(90);
        MinecartDerailSetArm.require(seed == 17320110707L && user.equals("CartDerail580")
                && user.length() <= 16, "minecart-derail-set identity drift");
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed,
                timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout),
                reader = null;
        MinecartDerailSetArm arm;
        int[] column = new int[1];
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3, 4, 5}, new int[] {1, 27, 66, 72, 328, 76},
                    new int[] {32, 1, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            MinecartDerailSetArm.require(actor.awaitInventory().occupiedSlots() == 6,
                    "minecart-derail inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            BlockPosition top = MinecartDerailSetArm.raise(actor, initial, cx, cz, column);
            arm = MinecartDerailSetArm.place(actor, top);
            RemoteObjectSpawn cart = arm.idleCart(actor);
            BlockState torch = arm.launch(actor);
            actor.close();
            MinecartDerailSetArm.awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            reader.awaitBlock(arm.powered, new BlockState(27, 8));
            reader.awaitBlock(arm.plate, new BlockState(72, 1));
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            arm.persist(after, cx, cz, torch);
            String evidence = "column=" + column[0] + ",support="
                    + MinecartDerailSetArm.cell(arm.support) + ":1:0,wall="
                    + MinecartDerailSetArm.cell(arm.wall) + ":1:0,bumper="
                    + MinecartDerailSetArm.cell(arm.bumper) + ":1:0,rail="
                    + MinecartDerailSetArm.cell(arm.powered) + ":27:0->8,track="
                    + MinecartDerailSetArm.cell(arm.track) + ":66:0,plate="
                    + MinecartDerailSetArm.cell(arm.plate)
                    + ":72:0->1,cart=type10+thrower0+fixed" + cart.fixedX() + ":"
                    + cart.fixedY() + ":" + cart.fixedZ()
                    + ",unpowered-hold=idle,derail=1,torch="
                    + MinecartDerailSetArm.cell(arm.torch) + ":76:" + torch.metadata()
                    + ",persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+wall+powered-rail27+rail66+plate72+bumper+torch76+minecart328"
                    + "|cause=packet15-item27+packet15-item66+packet15-item72+packet15-minecart328+packet15-item76"
                    + "|wire=packet23-type10+thrower0+packet53-rail27:0->8+packet53-plate72:0->1+packet53-torch76:5"
                    + "|oracle=unpowered-hold-idle+derail-off-rail66-onto-plate+fresh-login|"
                    + evidence;
            System.out.println("WORLDLINE_M580_SET=" + evidence);
            System.out.println("WORLDLINE_M580_TRACE=" + trace);
            System.out.println("WORLDLINE_M580_SIGNATURE=" + MinecartDerailSetArm.sha(trace));
        } finally {
            actor.close();
            if (reader != null)
                reader.close();
            server.close();
        }
    }
}
