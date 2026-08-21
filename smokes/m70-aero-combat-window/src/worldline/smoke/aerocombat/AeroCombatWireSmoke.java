package worldline.smoke.aerocombat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import worldline.api.MovementOutcome;
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
import worldline.b173server.B173WireClient;

/** Holds an M66 fixture open while a real Aero observer brackets Packet18/38. */
public final class AeroCombatWireSmoke {
    private AeroCombatWireSmoke() {}
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: AeroCombatWireSmoke server.jar workspace port seed attacker victim");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]); int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]); String attackerName = arguments[4], victimName = arguments[5];
        Duration timeout = Duration.ofSeconds(120); B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true); PeerSwingSession victim = client(port, victimName, timeout);
        PeerSwingSession attacker = client(port, attackerName, timeout); BufferedReader control = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            server.boot(); server.operator(victimName); server.operator(attackerName); victim.connect(); victim.synchronizePose();
            require(victim.awaitInventory().occupiedSlots() == 0, "victim inventory drifted"); victim.look(0F, 90F);
            for (RemoteArmorSlot slot : RemoteArmorSlot.values()) { acquire(victim, victimName, slot.leatherItemId());
                int source = find(victim.inventory(), new RemoteItemStack(slot.leatherItemId(), 1, 0));
                require(source >= 36, "leather source absent: " + slot);
                victim.equipLeatherArmor(source, slot); }
            for (int step = 0; step < 4; step++) victim.moveAndObserve(2.5D, 5D, 0D, 3);
            attacker.connect(); attacker.synchronizePose(); require(attacker.awaitInventory().occupiedSlots() == 0,
                    "attacker inventory drifted"); attacker.look(0F, 90F); acquire(attacker, attackerName, 276);
            int sword = find(attacker.inventory(), new RemoteItemStack(276, 1, 0)); require(sword >= 36, "sword absent");
            attacker.selectHeldSlot(sword - 36); victim.awaitPeerHeldItem(new RemoteHeldItem(attackerName, 276, 0));
            for (RemoteArmorSlot slot : RemoteArmorSlot.values()) attacker.awaitPeerArmor(
                    new worldline.api.RemoteArmorPiece(victimName, slot, slot.leatherItemId(), 0));
            PlayerPose victimAir = raise(victim), aligned = align(attacker, raise(attacker), victimAir);
            require(distance(victimAir, aligned) < 6D, "combat alignment drifted"); victim.sustainTicks(80); attacker.sustainTicks(2);
            System.out.println("WORLDLINE_M70_WIRE_ARMED=attacker=" + attacker.state().entityId()
                    + ";victim=" + victim.state().entityId()); System.out.flush(); awaitGo(control, attacker, victim);
            RemoteSwingRequest swing = attacker.swingHeldItem(); RemoteCombatStrike strike = attacker.attackPlayer(victimName);
            victim.sustainTicks(2); RemoteIncomingHit hit = victim.awaitIncomingHit(18); attacker.sustainTicks(2);
            require(swing.entityId() == attacker.state().entityId() && strike.targetEntityId() == victim.state().entityId()
                    && hit.healthBefore() == 20 && hit.healthAfter() == 18 && attacker.inventory().slot(sword).item().damage() == 1,
                    "wire event evidence drifted");
            System.out.println("WORLDLINE_M70_WIRE_HIT=attacker=" + swing.entityId() + ";victim="
                    + strike.targetEntityId() + ";health=20->18;sword=0->1"); System.out.flush();
            require("RELEASE".equals(control.readLine()), "RELEASE absent"); attacker.close(); victim.close();
            awaitPlayers(server, 0); server.save(); System.out.println("WORLDLINE_M70_WIRE_COMPLETE=shutdown=clean");
        } finally { attacker.close(); victim.close(); server.close(); }
    }
    private static PeerSwingSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void awaitGo(BufferedReader control, PeerSwingSession attacker, PeerSwingSession victim) throws Exception {
        AtomicBoolean stop = new AtomicBoolean(); Throwable[] failure = new Throwable[1]; Thread heartbeat = new Thread(() -> { try {
            while (!stop.get()) { attacker.sustainTicks(2); victim.sustainTicks(2); }
        } catch (Throwable error) { failure[0] = error; } }, "m70-wire-heartbeat");
        heartbeat.start(); String command;
        try { command = control.readLine(); } finally { stop.set(true); heartbeat.join(5000L); }
        require(!heartbeat.isAlive(), "wire heartbeat did not stop");
        if (failure[0] != null) throw new IllegalStateException("wire heartbeat failed", failure[0]);
        require("GO".equals(command), "GO absent");
    }
    private static void acquire(PeerSwingSession client, String username, int item) { int occupied = client.inventory().occupiedSlots() + 1;
        for (int step = 0; step < 10; step++) client.moveAndObserve(0D, 5D, 0D, 3);
        client.sendChat("/give " + username + " " + item + " 1"); client.sustainTicks(40);
        for (int step = 0; step < 25 && client.inventory().occupiedSlots() < occupied; step++)
            client.moveAndObserve(0D, -5D, 0D, 3); client.sustainTicks(10); }
    private static int find(RemoteInventoryView view, RemoteItemStack expected) { for (int slot = 9; slot <= 44; slot++)
        if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected)) return slot; return -1; }
    private static PlayerPose raise(PeerSwingSession client) { MovementOutcome result = null;
        for (int step = 0; step < 4; step++) result = client.moveAndObserve(0D, 5D, 0D, 3); return result.resulting(); }
    private static PlayerPose align(PeerSwingSession client, PlayerPose start, PlayerPose target) { PlayerPose current = start;
        for (int step = 0; step < 16 && distance(current, target) > 3D; step++) { double dx = target.x() + 2D - current.x();
            double dy = target.y() - current.y(), dz = target.z() - current.z(); double scale = Math.max(1D,
                    Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) / 4D);
            current = client.moveAndObserve(dx / scale, dy / scale, dz / scale, 3).resulting(); } return current; }
    private static double distance(PlayerPose a, PlayerPose b) { double x = a.x()-b.x(), y = a.y()-b.y(), z = a.z()-b.z();
        return Math.sqrt(x*x+y*y+z*z); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception { long end = System.currentTimeMillis()+5000L;
        while (System.currentTimeMillis()<end) { if (server.players().size()==count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count drifted"); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
