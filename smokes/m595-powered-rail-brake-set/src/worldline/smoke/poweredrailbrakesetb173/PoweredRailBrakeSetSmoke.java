package worldline.smoke.poweredrailbrakesetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Official unpowered powered-rail 27 stops a minecart after a powered launch. */
public final class PoweredRailBrakeSetSmoke {
    private PoweredRailBrakeSetSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) {
            throw new IllegalArgumentException(
                    "usage: PoweredRailBrakeSetSmoke server.jar workspace port seed username chunkX chunkZ");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        String user = arguments[4];
        int chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]);
        PoweredRailBrakeSetArm.require(seed == 17320110707L && user.equals("RailBrake595")
                && user.length() <= 16, "powered-rail-brake identity drift");
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3,
                true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        B173WireClient reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
                    new int[] {1, 27, 28, 328, 76}, new int[] {32, 8, 1, 1, 1},
                    new int[] {0, 0, 0, 0, 0});
            actor.connect();
            actor.synchronizePose();
            PoweredRailBrakeSetArm.require(actor.awaitInventory().occupiedSlots() == 5,
                    "powered-rail-brake inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX,
                    chunkZ);
            int[] column = new int[1];
            PoweredRailBrakeSetArm arm = PoweredRailBrakeSetArm.place(actor,
                    PoweredRailBrakeSetArm.raise(actor, initial, chunkX, chunkZ, column));
            actor.moveAndObserve(-1D, 0D, 0D, 1);
            actor.selectHeldSlot(3);
            actor.useHeldItemOnBlock(arm.launch, BlockFace.UP);
            RemoteObjectSpawn cart = actor.awaitObjectSpawn(10);
            PoweredRailBrakeSetArm.require(cart.type() == 10 && cart.throwerId() == 0
                    && cart.velocityX() == 0 && cart.velocityY() == 0 && cart.velocityZ() == 0
                    && cart.fixedX() == arm.launch.x() * 32 + 16
                    && cart.fixedY() == arm.launch.y() * 32 + 27
                    && cart.fixedZ() == arm.launch.z() * 32 + 16
                    && cart.fixedZ() != arm.beyond.z() * 32 + 16,
                    "minecart packet23 type10 spawn bounds drift");
            RemoteWorldView idle = WorldlineSmokeAwait.observe(actor, 10);
            PoweredRailBrakeSetArm.require(PoweredRailBrakeSetArm.idleDetector(idle, arm.beyond)
                    && PoweredRailBrakeSetArm.unpowered(idle, arm.launch)
                    && PoweredRailBrakeSetArm.unpowered(idle, arm.mid),
                    "unpowered launch moved the cart");
            actor.selectHeldSlot(4);
            actor.placeHeldBlock(BlockFace.DOWN.adjacent(arm.torch), BlockFace.UP);
            WorldlineSmokeAwait.awaitWorld(actor, world ->
                    world.blockAt(arm.torch.x(), arm.torch.y(), arm.torch.z())
                            .equals(new BlockState(76, 5))
                            && PoweredRailBrakeSetArm.powered(world, arm.launch)
                            && PoweredRailBrakeSetArm.powered(world, arm.mid)
                            && PoweredRailBrakeSetArm.idleDetector(world, arm.beyond),
                    "powered launch rails", 40);
            WorldlineSmokeAwait.observe(actor, 2);
            actor.selectHeldSlot(0);
            actor.look(-90F, 0F);
            actor.beginBreak(arm.torch);
            WorldlineSmokeAwait.observe(actor, 1);
            actor.finishBreak(arm.torch);
            actor.awaitBlock(arm.torch, new BlockState(0, 0));
            WorldlineSmokeAwait.awaitWorld(actor, world ->
                    world.blockAt(arm.torch.x(), arm.torch.y(), arm.torch.z())
                            .equals(new BlockState(0, 0))
                            && PoweredRailBrakeSetArm.unpowered(world, arm.launch)
                            && PoweredRailBrakeSetArm.unpowered(world, arm.mid)
                            && PoweredRailBrakeSetArm.idleDetector(world, arm.beyond),
                    "unpowered brake after torch break", 40);
            RemoteWorldView held = WorldlineSmokeAwait.observe(actor, 20);
            PoweredRailBrakeSetArm.require(PoweredRailBrakeSetArm.idleDetector(held, arm.beyond)
                    && PoweredRailBrakeSetArm.unpowered(held, arm.launch)
                    && PoweredRailBrakeSetArm.unpowered(held, arm.mid),
                    "beyond detector occupied after unpowered brake");
            actor.close();
            PoweredRailBrakeSetArm.awaitPlayers(server, 0);
            server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout);
            reader.connect();
            reader.synchronizePose();
            reader.awaitBlock(arm.launch, new BlockState(27, 0));
            reader.awaitBlock(arm.mid, new BlockState(27, 0));
            reader.awaitBlock(arm.beyond, new BlockState(28, 0));
            reader.awaitBlock(arm.torch, new BlockState(0, 0));
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX,
                    chunkZ);
            arm.persist(after, chunkX, chunkZ);
            String evidence = "column=" + column[0] + ",support="
                    + PoweredRailBrakeSetArm.cell(arm.support) + ":1:0,wall="
                    + PoweredRailBrakeSetArm.cell(arm.wall) + ":1:0,bumper="
                    + PoweredRailBrakeSetArm.cell(arm.bumper) + ":1:0,launch="
                    + PoweredRailBrakeSetArm.cell(arm.launch) + ":27:0->8->0,mid="
                    + PoweredRailBrakeSetArm.cell(arm.mid) + ":27:0->8->0,beyond="
                    + PoweredRailBrakeSetArm.cell(arm.beyond)
                    + ":28:0,cart=type10+thrower0+fixed" + cart.fixedX() + ":" + cart.fixedY()
                    + ":" + cart.fixedZ()
                    + ",moved=1,braked=1,torch=" + PoweredRailBrakeSetArm.cell(arm.torch)
                    + ":76:5->0,persisted=true,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-stone+wall+launch27+mid27+beyond28+bumper+torch76+minecart328"
                    + "|cause=packet15-item27+packet15-item28+packet15-minecart328+packet15-item76+break-torch76"
                    + "|wire=packet23-type10+thrower0+packet53-rail27:0->8->0+packet53-beyond28:0"
                    + "+packet53-torch76:5->0"
                    + "|oracle=powered-launch-then-unpowered-brake-stop+fresh-login|" + evidence;
            System.out.println("WORLDLINE_M595_SET=" + evidence);
            System.out.println("WORLDLINE_M595_TRACE=" + trace);
            System.out.println("WORLDLINE_M595_SIGNATURE=" + PoweredRailBrakeSetArm.sha(trace));
        } finally {
            actor.close();
            if (reader != null) {
                reader.close();
            }
            server.close();
        }
    }
}
