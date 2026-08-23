package worldline.smoke.aerocombat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import worldline.api.PeerSwingSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteCombatStrike;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteSwingRequest;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173LevelDatWeather;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Holds an M66 fixture open while a real Aero observer brackets Packet18/38. */
public final class AeroCombatWireSmoke {
  private AeroCombatWireSmoke() {
  }
  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: AeroCombatWireSmoke server.jar workspace port seed attacker victim observer");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String attackerName = arguments[4], victimName = arguments[5], observerName = arguments[6];
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    PeerSwingSession victim = client(port, victimName, timeout);
    PeerSwingSession attacker = client(port, attackerName, timeout);
    BufferedReader control =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    try {
      server.boot();
      server.save();
      server.operator(victimName);
      server.operator(attackerName);
      B173LevelDatWeather.Weather world =
          B173LevelDatWeather.read(workspace.resolve("world/level.dat"));
      double x = world.spawnX() + 0.5D, y = world.spawnY() + 20D, z = world.spawnZ() + 0.5D;
      B173PlayerSeed.writeInventory(workspace, victimName, x, y, z, new int[] {0, 1, 2, 3},
          new int[] {298, 299, 300, 301}, new int[] {1, 1, 1, 1}, new int[] {0, 0, 0, 0});
      B173PlayerSeed.writeInventory(workspace, attackerName, x + 3D, y, z, new int[] {0},
          new int[] {276}, new int[] {1}, new int[] {0});
      B173PlayerSeed.write(workspace, observerName, x + 6D, y, z);
      victim.connect();
      PlayerPose victimAir = victim.synchronizePose();
      require(victim.awaitInventory().occupiedSlots() == 4, "victim inventory seed drifted");
      victim.look(0F, 90F);
      for (RemoteArmorSlot slot : RemoteArmorSlot.values()) {
        int source = find(victim.inventory(), new RemoteItemStack(slot.leatherItemId(), 1, 0));
        require(source >= 36, "leather source absent: " + slot);
        victim.equipLeatherArmor(source, slot);
      }
      attacker.connect();
      PlayerPose aligned = attacker.synchronizePose();
      require(attacker.awaitInventory().occupiedSlots() == 1, "attacker inventory seed drifted");
      attacker.look(0F, 90F);
      int sword = find(attacker.inventory(), new RemoteItemStack(276, 1, 0));
      require(sword >= 36, "sword absent");
      attacker.selectHeldSlot(sword - 36);
      victim.awaitPeerHeldItem(new RemoteHeldItem(attackerName, 276, 0));
      for (RemoteArmorSlot slot : RemoteArmorSlot.values())
        attacker.awaitPeerArmor(
            new worldline.api.RemoteArmorPiece(victimName, slot, slot.leatherItemId(), 0));
      require(distance(victimAir, aligned) < 6D, "combat alignment drifted");
      worldline.test.WorldlineSmokeAwait.observe(victim, 80);
      worldline.test.WorldlineSmokeAwait.observe(attacker, 2);
      System.out.println("WORLDLINE_M70_WIRE_ARMED=attacker=" + attacker.state().entityId()
          + ";victim=" + victim.state().entityId());
      System.out.flush();
      awaitGo(control, attacker, victim);
      RemoteSwingRequest swing = attacker.swingHeldItem();
      RemoteCombatStrike strike = attacker.attackPlayer(victimName);
      worldline.test.WorldlineSmokeAwait.observe(victim, 2);
      RemoteIncomingHit hit = victim.awaitIncomingHit(18);
      worldline.test.WorldlineSmokeAwait.observe(attacker, 2);
      require(swing.entityId() == attacker.state().entityId()
              && strike.targetEntityId() == victim.state().entityId() && hit.healthBefore() == 20
              && hit.healthAfter() == 18 && attacker.inventory().slot(sword).item().damage() == 1,
          "wire event evidence drifted");
      System.out.println("WORLDLINE_M70_WIRE_HIT=attacker=" + swing.entityId()
          + ";victim=" + strike.targetEntityId() + ";health=20->18;sword=0->1");
      System.out.flush();
      require("RELEASE".equals(control.readLine()), "RELEASE absent");
      attacker.close();
      victim.close();
      awaitPlayers(server, 0);
      server.save();
      System.out.println("WORLDLINE_M70_WIRE_COMPLETE=shutdown=clean");
    } finally {
      attacker.close();
      victim.close();
      server.close();
    }
  }
  private static PeerSwingSession client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static void awaitGo(
      BufferedReader control, PeerSwingSession attacker, PeerSwingSession victim) throws Exception {
    AtomicBoolean stop = new AtomicBoolean();
    Throwable[] failure = new Throwable[1];
    Thread heartbeat = new Thread(() -> {
      try {
        while (!stop.get()) {
          worldline.test.WorldlineSmokeAwait.observe(attacker, 2);
          worldline.test.WorldlineSmokeAwait.observe(victim, 2);
        }
      } catch (Throwable error) {
        failure[0] = error;
      }
    }, "m70-wire-heartbeat");
    heartbeat.start();
    String command;
    try {
      command = control.readLine();
    } finally {
      stop.set(true);
      heartbeat.join(5000L);
    }
    require(!heartbeat.isAlive(), "wire heartbeat did not stop");
    if (failure[0] != null)
      throw new IllegalStateException("wire heartbeat failed", failure[0]);
    require("GO".equals(command), "GO absent");
  }
  private static int find(RemoteInventoryView view, RemoteItemStack expected) {
    for (int slot = 9; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected))
        return slot;
    return -1;
  }
  private static double distance(PlayerPose a, PlayerPose b) {
    double x = a.x() - b.x(), y = a.y() - b.y(), z = a.z() - b.z();
    return Math.sqrt(x * x + y * y + z * z);
  }
  private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
    long end = System.currentTimeMillis() + 5000L;
    while (System.currentTimeMillis() < end) {
      if (server.players().size() == count)
        return;
      Thread.sleep(100L);
    }
    throw new IllegalStateException("player count drifted");
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
