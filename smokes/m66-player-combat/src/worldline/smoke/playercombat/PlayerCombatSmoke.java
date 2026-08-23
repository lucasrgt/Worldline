package worldline.smoke.playercombat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.CombatHealthSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteCombatStrike;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.ServerPlayerState;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves one armored diamond-sword PvP hit through Packet7, Packet38, and Packet8. */
public final class PlayerCombatSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|pvp=true|monsters=false|clients=2"
            + "|victim=leather298,299,300,301|attacker=diamond-sword276:0"
            + "|invulnerability=80ticks|range=air-aligned-under6|out=packet7-action1"
            + "|attacker-oracle=packet38-victim-status2|victim-order=packet38-status2-before-packet8"
            + "|health=20-18|sword-local=276:0-276:1|persisted-health=18|disconnect=clean";
    private PlayerCombatSmoke() {}
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 6) throw new IllegalArgumentException(
                "usage: PlayerCombatSmoke server.jar workspace port seed attacker victim");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]); int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]); String attackerName = arguments[4], victimName = arguments[5];
        Duration timeout = Duration.ofSeconds(90); B173DedicatedServer server = new B173DedicatedServer(
                jar, workspace, port, seed, timeout, 3, true);
        CombatHealthSession victim = client(port, victimName, timeout), attacker = client(port, attackerName, timeout);
        RemoteCombatStrike strike; RemoteIncomingHit hit; ServerPlayerState saved;
        try {
            server.boot(); B173PlayerSeed.writeInventory(workspace, victimName, 8.5D, 70D, 8.5D,
                    new int[] {0, 1, 2, 3}, new int[] {298, 299, 300, 301},
                    new int[] {1, 1, 1, 1}, new int[] {0, 0, 0, 0});
            B173PlayerSeed.writeInventory(workspace, attackerName, 10.5D, 70D, 8.5D,
                    new int[] {0}, new int[] {276}, new int[] {1}, new int[] {0});
            victim.connect(); victim.synchronizePose();
            require(victim.awaitInventory().occupiedSlots() == 4, "victim inventory seed drifted");
            for (RemoteArmorSlot slot : RemoteArmorSlot.values()) { RemoteItemStack item =
                    new RemoteItemStack(slot.leatherItemId(), 1, 0); int source = find(victim.inventory(), item);
                require(source >= 36, "leather source absent"); victim.equipLeatherArmor(source, slot); }
            require(victim.inventory().occupiedSlots() == 4, "victim armor count drifted");
            attacker.connect(); attacker.synchronizePose(); require(attacker.awaitInventory().occupiedSlots() == 1,
                    "attacker inventory seed drifted");
            int swordSlot = find(attacker.inventory(), new RemoteItemStack(276, 1, 0));
            require(swordSlot >= 36, "diamond sword source absent"); attacker.selectHeldSlot(swordSlot - 36);
            victim.awaitPeerHeldItem(new RemoteHeldItem(attackerName, 276, 0));
            for (RemoteArmorSlot slot : RemoteArmorSlot.values()) attacker.awaitPeerArmor(
                    new worldline.api.RemoteArmorPiece(victimName, slot, slot.leatherItemId(), 0));
            PlayerPose victimAir = null, attackerAir = null;
            for (int step = 0; step < 80; step++) { victimAir = victim.moveAndObserve(0D, 0.3D, 0D, 1).resulting();
                attackerAir = attacker.moveAndObserve(0D, 0.3D, 0D, 1).resulting(); }
            require(distance(victimAir, attackerAir) < 6D, "combat alignment drifted");
            strike = attacker.attackPlayer(victimName); worldline.test.WorldlineSmokeAwait.observe(victim,2); hit = victim.awaitIncomingHit(18);
            require(strike.weaponId() == 276 && strike.hurtStatus() == 2 && strike.target().equals(victimName)
                    && hit.victim().equals(victimName) && hit.healthBefore() == 20 && hit.healthAfter() == 18
                    && hit.damage() == 2, "combat evidence drifted");
            worldline.test.WorldlineSmokeAwait.observe(attacker,2); int swordDamage = attacker.inventory().slot(swordSlot).item().damage();
            require(swordDamage == 1, "diamond sword wear drifted: " + swordDamage);
            attacker.close(); victim.close(); awaitPlayers(server, 0); server.save(); saved = server.player(victimName);
            require(saved.health() == 18, "victim health persistence drifted");
        } finally { attacker.close(); victim.close(); server.close(); }
        System.out.println("WORLDLINE_M66_API=player-combat,packet7,packet38,packet8,armored-health");
        System.out.println("WORLDLINE_M66_HIT=target=" + strike.targetEntityId() + ";health="
                + hit.healthBefore() + "->" + hit.healthAfter() + ";damage=" + hit.damage() + ";saved=" + saved.health());
        System.out.println("WORLDLINE_M66_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M66_SIGNATURE=" + sha256(TRACE));
    }
    private static CombatHealthSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static int find(RemoteInventoryView view, RemoteItemStack expected) { for (int slot = 9; slot <= 44; slot++)
        if (!view.slot(slot).empty() && view.slot(slot).item().equals(expected)) return slot; return -1; }
    private static double distance(PlayerPose a, PlayerPose b) { double x = a.x() - b.x(), y = a.y() - b.y(), z = a.z() - b.z();
        return Math.sqrt(x * x + y * y + z * z); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception { byte[] bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
