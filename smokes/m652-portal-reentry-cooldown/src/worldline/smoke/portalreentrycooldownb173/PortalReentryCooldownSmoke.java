package worldline.smoke.portalreentrycooldownb173;

import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;
import worldline.testkit.PortalReentryCooldownFixture;

/** Proves arrival-portal suppression and return after one bounded exit window. */
public final class PortalReentryCooldownSmoke {
    private PortalReentryCooldownSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 10) throw new IllegalArgumentException(
                "usage: PortalReentryCooldownSmoke server.jar workspace port seed user chunkX chunkZ "
                        + "contactTicks outsideTicks residenceTicks");
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int cx = Integer.parseInt(arguments[5]);
        int cz = Integer.parseInt(arguments[6]);
        int contactTicks = Integer.parseInt(arguments[7]);
        int outsideTicks = Integer.parseInt(arguments[8]);
        int residenceTicks = Integer.parseInt(arguments[9]);
        require(seed == 17320110707L && "PortalGate652".equals(user)
                && cx == 0 && cz == 0 && contactTicks == 120
                && outsideTicks == 220 && residenceTicks == 120,
                "portal cooldown fixture identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2}, new int[] {1, 49, 259},
                    new int[] {16, 14, 1}, new int[] {0, 0, 0});
            actor.connect();
            PlayerPose pose = actor.synchronizePose();
            require(actor.dimension() == 0 && actor.awaitInventory().occupiedSlots() == 3,
                    "portal source player drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
            PortalReentryWorld.Activation activation =
                    PortalReentryWorld.activate(actor, initial, cx, cz);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            pose = move(actor, pose, activation.source.insideX(), activation.source.minY,
                    activation.source.insideZ(), 1);
            WorldlineSmokeAwait.observe(actor, residenceTicks);
            require(actor.awaitDimension(-1) == -1, "outbound portal transition absent");
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            RemoteWorldView netherWorld = WorldlineSmokeAwait.observe(actor, 20);
            PortalReentryWorld.Portal destination = PortalReentryWorld.find(netherWorld, pose);
            require(PortalReentryWorld.frame(netherWorld, destination) == 14,
                    "destination portal frame drifted");
            pose = move(actor, pose, destination.insideX(), destination.minY,
                    destination.insideZ(), 1);
            WorldlineSmokeAwait.observe(actor, contactTicks);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            boolean heldInside = destination.contains(pose);
            int heldDimension = actor.dimension();
            require(heldInside && heldDimension == -1,
                    "arrival portal contact was not held through cooldown treatment");
            pose = move(actor, pose, destination.outsideX(), destination.minY,
                    destination.outsideZ(), 1);
            WorldlineSmokeAwait.observe(actor, outsideTicks - 2);
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            boolean exitedCollision = !destination.contains(pose);
            require(exitedCollision && actor.dimension() == -1,
                    "same player did not remain outside the arrival portal");
            pose = move(actor, pose, destination.insideX(), destination.minY,
                    destination.insideZ(), 1);
            boolean reenteredCollision = destination.contains(pose);
            require(reenteredCollision, "same player did not re-enter the arrival portal");
            WorldlineSmokeAwait.observe(actor, residenceTicks);
            require(actor.awaitDimension(0) == 0, "released portal return absent");
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            RemoteWorldView overworld = WorldlineSmokeAwait.observe(actor, 20);
            PortalReentryWorld.Portal returned = PortalReentryWorld.find(overworld, pose);
            require(PortalReentryWorld.frame(overworld, returned) == 14,
                    "returned portal frame drifted");
            actor.close();
            awaitPlayers(server, 0);
            server.save();
            int persisted = server.player(user).dimension();
            PortalReentryCooldownFixture.Evidence evidence =
                    PortalReentryCooldownFixture.verify(
                            new PortalReentryCooldownFixture.Trial(user, -1, heldDimension, 0,
                                    contactTicks, heldInside, false, false),
                            new PortalReentryCooldownFixture.Trial(user, -1, 0, outsideTicks,
                                    residenceTicks, heldInside, exitedCollision,
                                    reenteredCollision),
                            6, 6, 6, persisted);
            require(evidence.blockedTicks() == 120 && evidence.releaseTicks() == 220
                    && evidence.persistedDimension() == 0,
                    "portal cooldown TestKit evidence drifted");
            String source = activation.bottom.x() + ":" + activation.bottom.y()
                    + ":" + activation.bottom.z();
            String signal = "dimensions=0->-1->-1->0,column=" + activation.column
                    + ",source=" + source + ",sourcePortal=6:14,destinationPortal=6:14"
                    + ",contactHold=120,outsideRelease=220"
                    + ",path=inside->outside->inside,sameActor=true,returnResidence=120"
                    + ",returnPortal=6:14,persisted=0,clients=1,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|profile=allow-nether-true|source=official-upright-portal49x14+portal90x6|"
                    + "outbound=packet11-contact120+packet9-zero-to-minus1|"
                    + "blocked=destination-portal-contact120+dimension-minus1-held|"
                    + "release=packet11-outside220+reentry-contact120|"
                    + "path=packet13-same-player-inside-outside-inside|"
                    + "return=packet9-minus1-to-zero+portal49x14+portal90x6|"
                    + "oracle=arrival-contact-suppressed-until-bounded-exit|" + signal;
            System.out.println("WORLDLINE_M652_SET=" + signal);
            System.out.println("WORLDLINE_M652_TRACE=" + trace);
            System.out.println("WORLDLINE_M652_SIGNATURE=" + sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }

    private static PlayerPose move(B173WireClient actor, PlayerPose pose,
            double x, double y, double z, int ticks) {
        return actor.moveAndObserve(x - pose.x(), y - pose.y(), z - pose.z(), ticks).resulting();
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
