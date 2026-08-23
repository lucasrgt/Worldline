package worldline.smoke.envdeathsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Three official Packet8 health-0 deaths: drowning, sand suffocation, then lava 11. */
public final class EnvDeathSetSmoke {
  private EnvDeathSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: EnvDeathSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    EnvDeathSetSupport.require(
        seed == 17320110707L && user.equals("EnvDeath465") && user.length() <= 16,
        "env-death-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = null;
    EnvDeathSetSupport.Sites sites;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 12, 327, 9}, new int[] {64, 8, 1, 8}, new int[] {0, 0, 0, 0}, 20);
      actor = EnvDeathSetSupport.open(port, user, timeout);
      EnvDeathSetSupport.require(
          actor.awaitInventory().occupiedSlots() == 4 && actor.awaitHealth(20) == 20,
          "env-death inventory or health drift");
      sites = EnvDeathSetSupport.build(actor, cx, cz);
      EnvDeathSetSupport.dieZero(actor, "drowning");
      EnvDeathSetSupport.require(EnvDeathSetSupport.water(actor.sustainTicks(1)
                                         .blockAt(sites.upper.x(), sites.upper.y(), sites.upper.z())
                                         .legacyId()),
          "drown death left water 8/9");
      actor.close();
      EnvDeathSetSupport.awaitPlayers(server, 0);
      server.save();
      B173PlayerSeed.writeInventory(workspace, user, sites.pad.x() + 0.5D, sites.pad.y() + 1.0D,
          sites.pad.z() + 0.5D, new int[] {0, 1}, new int[] {1, 12}, new int[] {16, 4},
          new int[] {0, 0}, 20);
      actor = EnvDeathSetSupport.open(port, user, timeout);
      EnvDeathSetSupport.require(actor.awaitHealth(20) == 20, "suffocate login health drift");
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(sites.tower, BlockFace.WEST);
      actor.selectHeldSlot(0);
      BlockPosition tower = EnvDeathSetSupport.place(actor, sites.tower, BlockFace.UP, 1);
      actor.selectHeldSlot(1);
      actor.placeHeldBlock(tower, BlockFace.WEST);
      actor.awaitBlock(sites.body, new BlockState(12, 0));
      actor.awaitBlock(sites.head, new BlockState(12, 0));
      EnvDeathSetSupport.dieZero(actor, "suffocation");
      EnvDeathSetSupport.require(
          actor.sustainTicks(1).blockAt(sites.head.x(), sites.head.y(), sites.head.z()).legacyId()
              == 12,
          "suffocate death left sand 12");
      actor.close();
      EnvDeathSetSupport.awaitPlayers(server, 0);
      server.save();
      B173PlayerSeed.writeInventory(workspace, user, sites.safe.x() + 0.5D, sites.safe.y() + 1.0D,
          sites.safe.z() + 0.5D, new int[] {0}, new int[] {1}, new int[] {1}, new int[] {0}, 20);
      actor = EnvDeathSetSupport.open(port, user, timeout);
      EnvDeathSetSupport.require(actor.awaitHealth(20) == 20, "lava login health drift");
      EnvDeathSetSupport.walk(
          actor, sites.lava.x() + 0.5D, sites.lava.y() + 2.0D, sites.lava.z() + 0.5D);
      EnvDeathSetSupport.walk(actor, sites.lava.x() + 0.5D, sites.lava.y(), sites.lava.z() + 0.5D);
      EnvDeathSetSupport.require(actor.sustainTicks(1)
                                     .blockAt(sites.lava.x(), sites.lava.y(), sites.lava.z())
                                     .equals(new BlockState(11, 0)),
          "pre-lava still lava 11 absent");
      EnvDeathSetSupport.dieZero(actor, "lava");
      actor.close();
      EnvDeathSetSupport.awaitPlayers(server, 0);
      server.save();
      String evidence = "causes=lava+drown+suffocate,column=" + sites.column
          + ",lava=" + sites.lava.x() + ":" + sites.lava.y() + ":" + sites.lava.z()
          + ":11:0,water=" + sites.lower.x() + ":" + sites.lower.y() + ":" + sites.lower.z() + "+"
          + sites.upper.x() + ":" + sites.upper.y() + ":" + sites.upper.z()
          + ",head=" + sites.head.x() + ":" + sites.head.y() + ":" + sites.head.z()
          + ":12:0,deaths=drown:20->0+suffocate:20->0+lava:20->0,packet8=0,status=2,logins=3,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+two-still-water+falling-sand12+still-lava11|cause=submerged-eye-air-deplete+stand-under-falling-sand12+stand-in-lava|wire=packet38-status2+packet8-health20->0|oracle=lava+drown+suffocate-deaths-not-m307-hurt-or-m461-fall-or-m469-void|"
          + evidence;
      System.out.println("WORLDLINE_M465_SET=" + evidence);
      System.out.println("WORLDLINE_M465_TRACE=" + trace);
      System.out.println("WORLDLINE_M465_SIGNATURE=" + EnvDeathSetSupport.sha(trace));
    } finally {
      if (actor != null)
        actor.close();
      server.close();
    }
  }
}
