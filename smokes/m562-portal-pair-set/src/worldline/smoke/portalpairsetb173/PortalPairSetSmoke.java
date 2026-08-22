package worldline.smoke.portalpairsetb173;

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
import worldline.smoke.portalpairsetb173.PortalPairFrames.Frame;
import worldline.smoke.portalpairsetb173.PortalPairFrames.Raised;
import worldline.smoke.portalpairsetb173.PortalPairTravel.Portal;

/** Two Overworld portals in one 8:1 cell both exit through one generated Nether portal. */
public final class PortalPairSetSmoke {
    private PortalPairSetSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 10) throw new IllegalArgumentException(
                "usage: PortalPairSetSmoke server.jar workspace port seed username chunkX chunkZ portalTicks travelTicks cooldownTicks");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]), chunkZ = Integer.parseInt(arguments[6]);
        int portalTicks = Integer.parseInt(arguments[7]), travelTicks = Integer.parseInt(arguments[8]);
        int cooldownTicks = Integer.parseInt(arguments[9]);
        PortalPairFrames.require(seed == 17320110707L && user.equals("PortalPair562") && user.length() <= 16,
                "portal-pair-set identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        int column; Frame first, second; Portal shared;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
                    new int[] {1, 49, 259}, new int[] {64, 32, 1}, new int[] {0, 0, 0});
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            PortalPairFrames.require(actor.dimension() == 0 && actor.awaitInventory().occupiedSlots() == 3,
                    "portal-pair-set inventory or dimension drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            Raised raised = PortalPairFrames.raise(actor, initial, chunkX, chunkZ);
            column = raised.column;
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            first = PortalPairFrames.east(actor, raised.top);
            PortalPairFrames.light(actor, first);
            PortalPairFrames.requireLit(actor.sustainTicks(portalTicks), first);
            pose = PortalPairTravel.enter(actor, pose, first.enterX(), first.enterY(), first.enterZ(), travelTicks, -1);
            RemoteWorldView nether = actor.sustainTicks(20);
            RemoteChunkSnapshot netherChunk = nether.chunkAt(((int) Math.floor(pose.x())) >> 4,
                    ((int) Math.floor(pose.z())) >> 4);
            shared = PortalPairTravel.find(nether, pose, -1);
            PortalPairFrames.require(PortalPairTravel.frame(nether, shared) == 14 && PortalPairTravel.sky(netherChunk) == 0
                    && PortalPairTravel.nearPortalBlocks(nether, pose) == 6, "generated Nether pair-exit drift");
            pose = PortalPairTravel.leave(actor, shared, pose);
            PortalPairFrames.require(actor.dimension() == -1, "nether leave returned early");
            actor.sustainTicks(cooldownTicks);
            pose = PortalPairTravel.enter(actor, pose, shared.minX + 0.5D, shared.minY, shared.minZ + 0.5D,
                    travelTicks, 0);
            RemoteWorldView overworld = actor.sustainTicks(20);
            Portal landed = PortalPairTravel.find(overworld, pose, 0);
            PortalPairFrames.require(landed.count == 6 && PortalPairTravel.frame(overworld, landed) == 14
                    && actor.awaitInventory().occupiedSlots() >= 3,
                    "overworld return portal or inventory drift");
            pose = PortalPairTravel.leave(actor, landed, pose);
            PortalPairFrames.require(actor.dimension() == 0, "overworld leave changed dimension");
            actor.sustainTicks(cooldownTicks);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            BlockPosition origin = PortalPairFrames.bottomOf(landed);
            pose = PortalPairTravel.walk(actor, pose, origin.x() - 0.5D, origin.y() + 1D, origin.z() - 0.5D);
            second = PortalPairFrames.pairNeighbor(actor, landed);
            PortalPairFrames.require(second.cellX() == PortalPairFrames.cell(origin.x() + 1)
                    && second.cellZ() == PortalPairFrames.cell(origin.z())
                    && second.bottom.z() != origin.z(),
                    "pair did not collapse to one nether cell");
            PortalPairFrames.light(actor, second);
            PortalPairFrames.requireLit(actor.sustainTicks(portalTicks), second);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            pose = PortalPairTravel.walk(actor, pose, second.enterX(), second.enterY(), second.enterZ());
            pose = PortalPairTravel.enter(actor, pose, second.enterX(), second.enterY(), second.enterZ(),
                    travelTicks, -1);
            RemoteWorldView reused = actor.sustainTicks(20);
            Portal again = PortalPairTravel.find(reused, pose, -1);
            PortalPairFrames.require(again.sameExit(shared) && PortalPairTravel.nearPortalBlocks(reused, pose) == 6
                    && PortalPairTravel.frame(reused, again) == 14, "second Overworld portal created a second Nether exit");
            actor.close();
            PortalPairTravel.awaitPlayers(server, 0);
            server.save();
            PortalPairFrames.require(server.player(user).dimension() == -1, "pair player dimension was not persisted");
            String evidence = "pair=shared-exit,scale=8,sameCell=1,column=" + column
                    + ",netherPortals=1,dimensions=0->-1,0->-1,cooldown=" + cooldownTicks + ",travel=" + travelTicks;
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|profile=allow-nether-true|fixture=two-east-obsidian49-frames-one-8:1-cell"
                    + "|construction=packet15-two-14x49-frames+flint259|cause=packet11-inside-portal90-twice"
                    + "|outbound=packet9-0-to-minus1-twice|nether=one-generated-portal-shared-exit"
                    + "|oracle=same-nether-cell+one-nether-portal-not-m134-roundtrip-not-m560-scale-not-m561-search"
                    + "|observation=nether-packet51+portal14x6-once|" + evidence + "|disconnect=clean";
            System.out.println("WORLDLINE_M562_SET=" + evidence);
            System.out.println("WORLDLINE_M562_TRACE=" + trace);
            System.out.println("WORLDLINE_M562_SIGNATURE=" + PortalPairTravel.sha(trace));
        } finally { actor.close(); server.close(); }
    }
}
