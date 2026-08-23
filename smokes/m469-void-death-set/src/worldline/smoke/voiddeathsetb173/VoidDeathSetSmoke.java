package worldline.smoke.voiddeathsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Walks Packet13 steps of at most 9 until pose y is below 0, then Packet8 health 0. */
public final class VoidDeathSetSmoke {
  private VoidDeathSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 5)
      throw new IllegalArgumentException(
          "usage: VoidDeathSetSmoke server.jar workspace port seed username");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    require(seed == 17320110707L && user.equals("VoidDeath469") && user.length() <= 16,
        "void-death-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.write(workspace, user, 4.5D, -8.5D, 4.5D);
      actor.connect();
      PlayerPose pose = actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 0 && actor.awaitHealth(20) == 20
              && pose.y() < 0D,
          "void-death-set baseline drift y=" + pose.y());
      B173VoidDeath.Outcome death = B173VoidDeath.walkAndRespawn(actor, pose);
      int steps = death.steps();
      require(death.deadHealth() <= 0 && death.deathPose().y() < 0D
              && death.respawn().equals(new RemoteRespawn(0, 0, 20))
              && death.respawnPose().y() >= 0D,
          "void death outcome drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      ServerPlayerState saved = server.player(user);
      require(saved.dimension() == 0 && saved.health() == 20, "persisted void-death respawn drift");
      String evidence = "walk-off=cap9,steps=" + steps
          + ",pose-y<0,health=20->0->20,packet8=0,packet9=09:00,dimension=0,spawn-y>=0,persisted=20,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=underside-void-air-above-kill|cause=packet13-walk-down-cap9-until-pose-y<0|wire=packet8-health20->0+packet9-dimension-zero|oracle=void-walk-death-not-fall-not-env-not-m135-wait-under-kill|"
          + evidence;
      System.out.println("WORLDLINE_M469_SET=" + evidence);
      System.out.println("WORLDLINE_M469_TRACE=" + trace);
      System.out.println("WORLDLINE_M469_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
