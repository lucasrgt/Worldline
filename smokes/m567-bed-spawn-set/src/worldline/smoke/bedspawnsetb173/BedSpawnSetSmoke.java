package worldline.smoke.bedspawnsetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Occupies one official bed at night, wakes, then cactus-dies so Packet9 is at the bed. */
public final class BedSpawnSetSmoke {
    private BedSpawnSetSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 7) throw new IllegalArgumentException(
                "usage: BedSpawnSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
        int port = Integer.parseInt(args[2]); long seed = Long.parseLong(args[3]); String user = args[4];
        int chunkX = Integer.parseInt(args[5]), chunkZ = Integer.parseInt(args[6]);
        BedSpawnSupport.require(seed == 17320110707L && user.equals("BedSpawn567") && user.length() <= 16,
                "bed-spawn-set identity drift");
        Duration timeout = Duration.ofSeconds(180);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
        BlockPosition top, foot, head; int column; RemoteBedUse sleep; PlayerPose pose, wake, respawned;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
                    new int[] {0, 1, 2, 3}, new int[] {1, 355, 12, 81}, new int[] {32, 1, 1, 1},
                    new int[] {0, 0, 0, 0});
            actor.connect(); pose = actor.synchronizePose();
            BedSpawnSupport.require(actor.awaitInventory().occupiedSlots() == 4, "bed inventory drift");
            RemoteChunkSnapshot initial = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            top = BedSpawnSupport.foundation(initial, chunkX, chunkZ); column = 0; actor.selectHeldSlot(0);
            while (BedSpawnSupport.water(initial.blockAt(BedSpawnSupport.local(top.x(), chunkX), top.y() + 1,
                    BedSpawnSupport.local(top.z(), chunkZ)).legacyId())) {
                top = BedSpawnSupport.place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
                BedSpawnSupport.require(++column <= 15, "water column exceeded bed spawn set fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = BedSpawnSupport.place(actor, top, BlockFace.UP, 1);
                pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting(); column++;
            }
            pose = actor.moveAndObserve(top.x() + 0.5D - pose.x(), top.y() + 1.0D - pose.y(),
                    top.z() + 0.5D - pose.z(), 8).resulting();
            BedSpawnSupport.pad(actor, top); actor.look(0F, 0F);
            pose = actor.moveAndObserve(0D, 0D, 0D, 2).resulting();
            actor.selectHeldSlot(1); actor.useHeldItemOnBlock(top, BlockFace.UP);
            foot = BlockFace.UP.adjacent(top); head = BlockFace.SOUTH.adjacent(foot);
            actor.awaitBlock(foot, new BlockState(26, 0)); actor.awaitBlock(head, new BlockState(26, 8));
            actor.selectHeldSlot(4); server.setTime(18000L); worldline.test.WorldlineSmokeAwait.observe(actor,20);
            actor.activateBlock(foot, BlockFace.UP); sleep = B173BedAccess.await(actor);
            BedSpawnSupport.require(sleep.entityId() == actor.state().entityId() && sleep.unused() == 0
                    && sleep.x() == head.x() && sleep.y() == head.y() && sleep.z() == head.z()
                    && sleep.sleepPacket() == 17 && sleep.bedPacket() == 70
                    && sleep.packet70() == RemoteBedUse.NO_PACKET70, "Packet17 sleep enter drift");
            actor.awaitBlock(head, new BlockState(26, 12));
            BedSpawnSupport.require(worldline.test.WorldlineSmokeAwait.observe(actor,240).blockAt(head.x(), head.y(), head.z())
                    .equals(new BlockState(26, 8)), "SMP bed skip did not leave occupied head");
            wake = actor.moveAndObserve(0D, 0D, 0D, 8).resulting();
            BedSpawnSupport.require(wake.y() >= foot.y() - 0.5D && wake.y() <= foot.y() + 2.0D,
                    "actor is not standing after bed leave");
            actor.selectHeldSlot(2);
            BlockPosition sand = BedSpawnSupport.place(actor, BlockFace.EAST.adjacent(top), BlockFace.UP, 12);
            actor.selectHeldSlot(3);
            BlockPosition cactus = BedSpawnSupport.place(actor, sand, BlockFace.UP, 81);
            BedSpawnOracle world = BedSpawnOracle.read(workspace.resolve("world/level.dat"));
            respawned = world.cactusDeath(actor, wake, cactus);
            BedSpawnSupport.require(world.atBed(respawned, foot, head) && !world.atWorld(respawned),
                    "Packet9 respawn was world spawn not bed y=" + respawned.y());
            actor.close(); BedSpawnSupport.awaitPlayers(server, 0); server.save();
            reader = new B173WireClient("127.0.0.1", port, user, timeout); reader.connect();
            pose = reader.synchronizePose();
            RemoteChunkSnapshot after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            BedSpawnSupport.require(after.blockAt(BedSpawnSupport.local(foot.x(), chunkX), foot.y(),
                    BedSpawnSupport.local(foot.z(), chunkZ)).equals(new BlockState(26, 0))
                    && after.blockAt(BedSpawnSupport.local(head.x(), chunkX), head.y(),
                    BedSpawnSupport.local(head.z(), chunkZ)).equals(new BlockState(26, 8)),
                    "post-respawn bed halves drift");
            BedSpawnSupport.require(world.atBed(pose, foot, head) && !world.atWorld(pose)
                    && reader.awaitHealth(20) == 20, "persisted Packet9 is not bed spawn");
            String evidence = "column=" + column + ",foot=" + foot.x() + ":" + foot.y() + ":" + foot.z()
                    + ":26:0,head=" + head.x() + ":" + head.y() + ":" + head.z()
                    + ":26:8,enter=26:8->26:12,packet17=head,packet70=-1,leave=26:12->26:8,wake=standing,"
                    + "death=cactus81,health=20->0->20,packet8=0,packet9=09:00,dimension=0,spawn=bed,"
                    + "world=not-level.dat,persisted=bed,clients=2,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-3x3-stone+item355-block26+sand12-cactus81|cause=packet15-item355-place"
                    + "+empty-hand-night-use+cactus-aabb|wire=packet17-sleep+packet70=-1+packet53-occupied"
                    + "+packet8-health20->0+packet9-dimension-zero|oracle=bed-spawn-packet9-not-world-spawn"
                    + "-not-m330-occupy-only-not-m135-wait-under-kill-not-m469-void-without-bed|" + evidence;
            System.out.println("WORLDLINE_M567_SET=" + evidence);
            System.out.println("WORLDLINE_M567_TRACE=" + trace);
            System.out.println("WORLDLINE_M567_SIGNATURE=" + BedSpawnSupport.sha(trace));
        } finally { actor.close(); if (reader != null) reader.close(); server.close(); }
    }
}
