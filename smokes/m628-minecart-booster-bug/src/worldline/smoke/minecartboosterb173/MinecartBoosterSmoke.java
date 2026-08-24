package worldline.smoke.minecartboosterb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteObjectMovement;
import worldline.api.RemoteObjectSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173FixtureSupport;
import worldline.b173server.B173MinecartBooster;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;
import worldline.testkit.MinecartBoosterFixture;

/** Freezes the official side-by-side minecart booster on parallel rails. */
public final class MinecartBoosterSmoke {
    private MinecartBoosterSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: MinecartBoosterSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String username = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        MinecartBoosterArm.require(seed == 17320110707L
                && username.equals("CartBoost628") && username.length() <= 16,
                "minecart booster identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout);
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, username, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2}, new int[] {1, 66, 328},
                    new int[] {64, 8, 2}, new int[] {0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            MinecartBoosterArm.require(actor.awaitInventory().occupiedSlots() == 3,
                    "minecart booster inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ)
                    .chunkAt(chunkX, chunkZ);
            MinecartBoosterArm arm = MinecartBoosterArm.build(actor, initial, chunkX, chunkZ);
            actor.selectHeldSlot(2);
            actor.useHeldItemOnBlock(arm.booster[1], BlockFace.UP);
            RemoteObjectSpawn booster = actor.awaitObjectSpawn(10);
            actor.useHeldItemOnBlock(arm.driver[1], BlockFace.UP);
            RemoteObjectSpawn driver = actor.awaitObjectSpawn(10);
            WorldlineSmokeAwait.observe(actor, 10);
            actor.look(0F, 0F);
            B173MinecartBooster.push(actor, driver.entityId());
            RemoteObjectMovement driverMove = B173MinecartBooster.awaitForward(actor, driver, 0, 1);
            RemoteObjectMovement boosterMove = B173MinecartBooster.awaitForward(actor, booster, 0, 1);
            MinecartBoosterFixture.Evidence evidence = MinecartBoosterFixture.observe(
                    driver, booster, driverMove, boosterMove, 0, 1);
            MinecartBoosterArm.require(evidence.lateralFixed() == 32
                    && evidence.driverForward() && evidence.boosterForward(),
                    "minecart booster TestKit evidence drift");
            actor.close();
            B173FixtureSupport.awaitPlayers(server, 0);
            String signal = "driver=type10+forward,booster=type10+forward,parallel-gap=1"
                    + ",driver-rail=66:0,booster-rail=66:0,push=packet7-attack"
                    + ",clients=1,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-parallel-north-south-rail66+minecart328x2+rear-walls"
                    + "|cause=packet15-minecart328x2+packet7-driver-attack"
                    + "|wire=packet23-type10x2+packet31-or33-or34-forward"
                    + "|oracle=attacked-driver-transfers-forward-motion-to-parallel-cart|" + signal;
            System.out.println("WORLDLINE_M628_SET=" + signal);
            System.out.println("WORLDLINE_M628_TRACE=" + trace);
            System.out.println("WORLDLINE_M628_SIGNATURE=" + B173FixtureSupport.sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }
}
