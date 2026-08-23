package worldline.smoke.spawnlightcapsetb173;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.RemoteMobSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Unlit pad permits Packet24 50/54; sparse torches raise light >= 8 and reject the same volume. */
public final class SpawnLightCapSetSmoke {
    private SpawnLightCapSetSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 7) throw new IllegalArgumentException(
                "usage: SpawnLightCapSetSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
        Path dark = workspace.resolve("dark"), lit = workspace.resolve("lit");
        int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        String user = args[4];
        int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
        SpawnLightCapPad.require(seed == 17320110707L && user.equals("SpawnLit617")
                && user.length() <= 16, "spawn-light-cap identity drift");
        Duration timeout = Duration.ofSeconds(180);
        Files.createDirectories(dark);
        SpawnLightCapPad pad = SpawnLightCapPad.build(jar, dark, port, seed, user, cx, cz, timeout);
        SpawnLightCapPad.copyWorld(dark, lit);
        B173DedicatedServer server = B173DedicatedServer.monsters(jar, dark, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
        RemoteMobSpawn spawned;
        int darkLight, litLight, torches;
        try {
            server.boot();
            actor.connect();
            actor.synchronizePose();
            SpawnLightCapPad.require(actor.awaitInventory().occupiedSlots() >= 1,
                    "spawn-light-cap dark inventory drift");
            server.setTime(14000L);
            darkLight = SpawnLightCapProbe.blockLight(actor, pad.sample, cx, cz);
            SpawnLightCapPad.require(darkLight == 0, "unlit pad block-light drift " + darkLight);
            spawned = SpawnLightCapProbe.awaitDark(actor, pad.first, pad.second);
            SpawnLightCapPad.require((spawned.legacyType() == 50 || spawned.legacyType() == 54)
                    && spawned.legacyType() != 90 && spawned.legacyType() != 51
                    && spawned.legacyType() != 52,
                    "dark arm collapsed to M141 pig or M390 spider identity");
            actor.close();
            SpawnLightCapPad.awaitPlayers(server, 0);
            server.save();
        } finally {
            actor.close();
            server.close();
        }
        server = B173DedicatedServer.monsters(jar, lit, port, seed, timeout, 3, true);
        actor = new B173WireClient("127.0.0.1", port, user, timeout);
        try {
            server.boot();
            actor.connect();
            actor.synchronizePose();
            SpawnLightCapPad.require(actor.awaitInventory().occupiedSlots() >= 1,
                    "spawn-light-cap torch inventory drift");
            torches = SpawnLightCapPad.lightPad(actor, pad.first);
            actor.close();
            SpawnLightCapPad.awaitPlayers(server, 0);
            server.save();
            actor = new B173WireClient("127.0.0.1", port, user, timeout);
            actor.connect();
            actor.synchronizePose();
            litLight = SpawnLightCapProbe.blockLight(actor, pad.sample, cx, cz);
            SpawnLightCapPad.require(litLight >= 8, "torch pad block-light < 8: " + litLight);
            server.setTime(14000L);
            SpawnLightCapProbe.requireTorchReject(actor, pad.first, pad.second, 200);
            actor.close();
            SpawnLightCapPad.awaitPlayers(server, 0);
            server.save();
            String evidence = "column=" + pad.column + ",platform=7x7-48grass,spawners="
                    + SpawnLightCapPad.cell(pad.first, 52, 0) + "+"
                    + SpawnLightCapPad.cell(pad.second, 52, 0)
                    + ",entityid=Creeper+Zombie,dark=type50-or-54,torch=50:5x" + torches
                    + ",dark-light=0,lit-light>=8,torch-arm=absent,night=14000,clients=4,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=raised-7x7-grass-platform+two-spawner52"
                    + "|cause=nbt-entityid-creeper+zombie+time-14000+sparse-torch-50-5-light-cap"
                    + "|wire=packet24-type50-or-54-dark+packet24-type50-or-54-absent-torch"
                    + "|oracle=spawn-light-cap-unlit-permit-torch-reject"
                    + "-not-m435-natural-not-m569-delay-not-m564-carpet|"
                    + evidence;
            System.out.println("WORLDLINE_M617_SET=" + evidence);
            System.out.println("WORLDLINE_M617_TRACE=" + trace);
            System.out.println("WORLDLINE_M617_SIGNATURE=" + SpawnLightCapPad.sha(trace));
        } finally {
            actor.close();
            server.close();
        }
    }
}
