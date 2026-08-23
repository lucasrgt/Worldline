package worldline.smoke.netherexitcreatesetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.smoke.netherexitcreatesetb173.NetherExitPortalKit.Frame;
import worldline.smoke.netherexitcreatesetb173.NetherExitPortalKit.Raised;
import worldline.smoke.netherexitcreatesetb173.NetherExitPortalScan.Portal;

/** Returns from a far Nether portal so no Overworld portal is in range and the server creates 49+90. */
public final class NetherExitCreateSetSmoke {
    private NetherExitCreateSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 11) throw new IllegalArgumentException(
                "usage: NetherExitCreateSetSmoke server.jar workspace port seed username chunkX chunkZ portalTicks travelTicks cooldownTicks netherShift");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]), chunkZ = Integer.parseInt(arguments[6]);
        int portalTicks = Integer.parseInt(arguments[7]), travelTicks = Integer.parseInt(arguments[8]);
        int cooldownTicks = Integer.parseInt(arguments[9]), shift = Integer.parseInt(arguments[10]);
        NetherExitPortalKit.require(seed == 17320110707L && user.equals("NetherExit563") && user.length() <= 16
                && shift >= 24 && shift <= 48 && cooldownTicks >= 200, "nether-exit-create-set identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        int column; Frame source; Portal created;
        try {
            server.boot();
            seedItems(workspace, user, 4.5D, 60D, 4.5D, 0);
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            NetherExitPortalKit.require(actor.dimension() == 0 && actor.awaitInventory().occupiedSlots() == 3,
                    "nether-exit-create-set inventory or dimension drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            Raised raised = NetherExitPortalKit.raise(actor, initial, chunkX, chunkZ);
            column = raised.column;
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            source = NetherExitPortalKit.east(actor, raised.top);
            NetherExitPortalKit.light(actor, source, portalTicks);
            NetherExitPortalKit.require(actor.dimension() == 0, "source portal traveled during activation");
            pose = NetherExitPortalScan.enter(actor, pose, source.enterX(), source.enterY(), source.enterZ(),
                    travelTicks, -1);
            RemoteWorldView nether = worldline.test.WorldlineSmokeAwait.observe(actor,20);
            Portal generated = NetherExitPortalScan.find(nether, pose, -1);
            RemoteChunkSnapshot netherChunk = nether.chunkAt(((int) Math.floor(pose.x())) >> 4,
                    ((int) Math.floor(pose.z())) >> 4);
            NetherExitPortalKit.require(NetherExitPortalScan.frame(nether, generated) == 14
                    && NetherExitPortalScan.sky(netherChunk) == 0, "generated Nether portal drift");
            pose = NetherExitPortalScan.leave(actor, generated, pose, cooldownTicks);
            double farX = generated.minX + shift + 0.5D, farZ = generated.minZ + 0.5D;
            actor = relog(actor, server, workspace, user, timeout, farX, 70D, farZ, -1);
            pose = actor.synchronizePose();
            NetherExitPortalKit.require(actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() == 3,
                    "far Nether relog inventory or dimension drift");
            int farCx = ((int) Math.floor(pose.x())) >> 4, farCz = ((int) Math.floor(pose.z())) >> 4;
            pose = NetherExitPortalScan.hoverUntilChunk(actor, pose, farCx, farCz);
            RemoteWorldView farView = worldline.test.WorldlineSmokeAwait.observe(actor,20);
            BlockPosition land = NetherExitPortalKit.standNether(farView);
            actor = relog(actor, server, workspace, user, timeout,
                    land.x() + 0.5D, land.y() + 1D, land.z() - 1.5D, -1);
            pose = actor.synchronizePose();
            NetherExitPortalKit.require(actor.dimension() == -1 && actor.awaitInventory().occupiedSlots() == 3,
                    "nether pad relog inventory or dimension drift");
            pose = NetherExitPortalScan.hoverUntilChunk(actor, pose, land.x() >> 4, land.z() >> 4);
            int up = worldline.test.WorldlineSmokeAwait.observe(actor,1).blockAt(land.x(), land.y() + 1, land.z()).legacyId();
            NetherExitPortalKit.require(up == 0, "nether pad ceiling id=" + up + " at " + land);
            Frame far = NetherExitPortalKit.east(actor, land);
            NetherExitPortalKit.light(actor, far, portalTicks);
            NetherExitPortalKit.require(actor.dimension() == -1, "far Nether portal traveled during activation");
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            pose = NetherExitPortalScan.enter(actor, pose, far.enterX(), far.enterY(), far.enterZ(), travelTicks, 0);
            int overCx = ((int) Math.floor(pose.x())) >> 4, overCz = ((int) Math.floor(pose.z())) >> 4;
            pose = NetherExitPortalScan.hoverUntilChunk(actor, pose, overCx, overCz);
            RemoteWorldView returned = worldline.test.WorldlineSmokeAwait.observe(actor,20);
            created = NetherExitPortalScan.find(returned, pose, 0);
            RemoteChunkSnapshot over = returned.chunkAt(((int) Math.floor(pose.x())) >> 4,
                    ((int) Math.floor(pose.z())) >> 4);
            NetherExitPortalKit.require(NetherExitPortalScan.sky(over) > 0
                    && NetherExitPortalScan.frame(returned, created) == 14
                    && created.count == 6
                    && !NetherExitPortalScan.sameSource(created, source)
                    && NetherExitPortalScan.rangeChebyshev(created, source) > 128,
                    "Overworld create-portal drift reused M134/M382 source");
            actor.close();
            NetherExitPortalScan.awaitPlayers(server, 0);
            server.save();
            NetherExitPortalKit.require(server.player(user).dimension() == 0,
                    "created-exit player dimension was not persisted");
            String evidence = "dimensions=0->-1->0,column=" + column + ",source=" + source.source()
                    + ",shift=" + shift + ",created=6x90+14x49,obsidian=49,portal=90,not-source,"
                    + "not-m134-reuse,not-m382-activation-only,not-m561-search,not-m562-pair,cooldown="
                    + cooldownTicks + ",persisted=true,clients=3,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|profile=allow-nether-true|source=official-m382-obsidian49-frame+flint259"
                    + "|outbound=packet9-0-to-minus1|nether=leave+cooldown+relog-shift-east-" + shift + "+m382-frame"
                    + "|return=packet9-minus1-to-0|effect=official-create-overworld-portal-obsidian49-plus-portal90"
                    + "|oracle=not-m134-reuse-of-source-frame-not-m561-search-not-m562-pair|"
                    + "observation=live-packet51|" + evidence;
            System.out.println("WORLDLINE_M563_SET=" + evidence);
            System.out.println("WORLDLINE_M563_TRACE=" + trace);
            System.out.println("WORLDLINE_M563_SIGNATURE=" + NetherExitPortalScan.sha(trace));
        } finally { actor.close(); server.close(); }
    }

    private static void seedItems(Path workspace, String user, double x, double y, double z, int dimension) {
        B173PlayerSeed.writeInventory(workspace, user, x, y, z, dimension, new int[] {0, 1, 2},
                new int[] {1, 49, 259}, new int[] {64, 14, 1}, new int[] {0, 0, 0});
    }

    private static B173WireClient relog(B173WireClient actor, B173DedicatedServer server, Path workspace, String user,
            Duration timeout, double x, double y, double z, int dimension) throws Exception {
        actor.close();
        NetherExitPortalScan.awaitPlayers(server, 0);
        server.save();
        seedItems(workspace, user, x, y, z, dimension);
        B173WireClient next = new B173WireClient("127.0.0.1", server.state().port(), user, timeout);
        next.connect();
        return next;
    }
}
