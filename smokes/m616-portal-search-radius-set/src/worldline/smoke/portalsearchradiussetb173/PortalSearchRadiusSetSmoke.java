package worldline.smoke.portalsearchradiussetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;
import worldline.smoke.portalsearchradiussetb173.PortalSearchRadiusKit.Frame;
import worldline.smoke.portalsearchradiussetb173.PortalSearchRadiusKit.Raised;
import worldline.smoke.portalsearchradiussetb173.PortalSearchRadiusScan.Portal;

/** Travels an offset Overworld portal onto an existing Nether frame inside the search radius. */
public final class PortalSearchRadiusSetSmoke {
    private PortalSearchRadiusSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 10) throw new IllegalArgumentException(
                "usage: PortalSearchRadiusSetSmoke server.jar workspace port seed username "
                        + "chunkX chunkZ portalTicks travelTicks netherShift");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]), chunkZ = Integer.parseInt(arguments[6]);
        int portalTicks = Integer.parseInt(arguments[7]);
        int travelTicks = Integer.parseInt(arguments[8]), shift = Integer.parseInt(arguments[9]);
        PortalSearchRadiusKit.require(seed == 17320110707L && user.equals("PortalSrch616")
                && user.length() <= 16 && shift >= 24 && shift <= 48,
                "portal-search-radius-set identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout,
                3, true, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            PortalSearchRadiusKit.seed(workspace, user, 4.5D, 60D, 4.5D, 0);
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            PortalSearchRadiusKit.require(actor.dimension() == 0
                    && actor.awaitInventory().occupiedSlots() == 3,
                    "portal-search-radius inventory or dimension drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ)
                    .chunkAt(chunkX, chunkZ);
            BlockPosition spawn = PortalSearchRadiusKit.spawnFoundation(initial, chunkX, chunkZ);
            Raised raised = PortalSearchRadiusKit.raise(actor, initial, chunkX, chunkZ, spawn);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            Frame first = PortalSearchRadiusKit.east(actor, raised.top);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            pose = PortalSearchRadiusScan.go(actor, pose, first.enterX(), first.enterY(),
                    first.enterZ());
            PortalSearchRadiusKit.light(actor, first, portalTicks);
            PortalSearchRadiusKit.require(actor.dimension() == 0,
                    "source portal traveled during activation");
            pose = PortalSearchRadiusScan.enter(actor, pose, first.enterX(), first.enterY(),
                    first.enterZ(), travelTicks, -1);
            RemoteWorldView nether = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
            Portal existing = PortalSearchRadiusScan.find(nether, pose, -1);
            RemoteChunkSnapshot netherChunk = nether.chunkAt(((int) Math.floor(pose.x())) >> 4,
                    ((int) Math.floor(pose.z())) >> 4);
            PortalSearchRadiusKit.require(PortalSearchRadiusScan.sky(netherChunk) == 0,
                    "generated Nether portal drift");
            double owX = (existing.minX + shift) * 8 + 0.5D, owZ = existing.minZ * 8 + 0.5D;
            actor = relogOverworld(actor, server, workspace, user, timeout, owX, 70D, owZ);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            int farCx = ((int) Math.floor(pose.x())) >> 4, farCz = ((int) Math.floor(pose.z())) >> 4;
            RemoteChunkSnapshot far = actor.awaitRemoteChunk(farCx, farCz).chunkAt(farCx, farCz);
            BlockPosition land = PortalSearchRadiusKit.farFoundation(far, farCx, farCz);
            actor = relogOverworld(actor, server, workspace, user, timeout,
                    land.x() + 0.5D, land.y() + 4.5D, land.z() + 0.5D);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            far = actor.awaitRemoteChunk(farCx, farCz).chunkAt(farCx, farCz);
            Raised offset = PortalSearchRadiusKit.raise(actor, far, farCx, farCz, land);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            Frame source = PortalSearchRadiusKit.east(actor, offset.top);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            pose = PortalSearchRadiusScan.go(actor, pose, source.enterX(), source.enterY(),
                    source.enterZ());
            PortalSearchRadiusKit.light(actor, source, portalTicks);
            PortalSearchRadiusKit.require(actor.dimension() == 0,
                    "offset portal traveled during activation");
            pose = PortalSearchRadiusScan.enter(actor, pose, source.enterX(), source.enterY(),
                    source.enterZ(), travelTicks, -1);
            RemoteWorldView searched = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
            String evidence = PortalSearchRadiusScan.prove(existing, pose, searched, shift);
            actor.close();
            PortalSearchRadiusScan.awaitPlayers(server, 0);
            server.save();
            PortalSearchRadiusKit.require(server.player(user).dimension() == -1,
                    "searched player dimension was not persisted");
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|profile=allow-nether-true|source=official-m382-obsidian49-frame+flint259"
                    + "|outbound=packet9-0-to-minus1|existing=generated-nether-portal90"
                    + "|offset-overworld=relog-shift-east-" + shift + "+m382-frame"
                    + "|search-trip=packet9-0-to-minus1"
                    + "|effect=official-search-existing-nether-portal"
                    + "|oracle=not-m560-scale-only-not-m563-create-not-m562-pair"
                    + "|observation=live-packet51|" + evidence;
            System.out.println("WORLDLINE_M616_SET=" + evidence);
            System.out.println("WORLDLINE_M616_TRACE=" + trace);
            System.out.println("WORLDLINE_M616_SIGNATURE=" + PortalSearchRadiusScan.sha(trace));
        } finally { actor.close(); server.close(); }
    }

    private static B173WireClient relogOverworld(B173WireClient actor, B173DedicatedServer server,
            Path workspace, String user, Duration timeout, double x, double y, double z)
            throws Exception {
        B173WireClient next = PortalSearchRadiusKit.relog(actor, server, workspace, user, timeout,
                x, y, z, 0);
        next.synchronizePose();
        PortalSearchRadiusKit.require(next.dimension() == 0
                && next.awaitInventory().occupiedSlots() == 3,
                "Overworld relog inventory or dimension drift");
        return next;
    }
}
