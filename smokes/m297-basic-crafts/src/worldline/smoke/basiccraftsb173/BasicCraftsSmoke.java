package worldline.smoke.basiccraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteInventoryView;
import worldline.b173server.B173BasicCraftsClick;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Crafts planks 5x4, sticks 280x4, and torches 50x4 in personal window 0. */
public final class BasicCraftsSmoke {
    private static final String SIGNAL = "result=5x4:0+280x4:0+50x4:0,taken=true,"
            + "stored=36:5x2:0+37:50x4:0+38:280x3:0,actions=17,persisted=true,clients=2,disconnect=clean";
    private BasicCraftsSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) throw new IllegalArgumentException(
                "usage: BasicCraftsSmoke server.jar workspace port seed username");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String user = arguments[4]; Duration timeout = Duration.ofSeconds(90);
        require(user.length() <= 16, "username exceeds 16");
        require(B173BasicCraftsClick.STICKS.legacyId() == 280 && B173BasicCraftsClick.TORCHES.legacyId() == 50
                && B173BasicCraftsClick.PLANKS4.legacyId() == 5 && B173BasicCraftsClick.COAL.legacyId() == 263,
                "basic craft identities drifted");
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = client(port, user, timeout), reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D,
                    new int[] {0, 1}, new int[] {17, 263}, new int[] {1, 1}, new int[] {0, 0});
            actor.connect(); actor.synchronizePose();
            RemoteInventoryView initial = actor.awaitInventory();
            require(initial.occupiedSlots() == 2 && initial.slot(36).item().equals(B173BasicCraftsClick.LOG)
                    && initial.slot(37).item().equals(B173BasicCraftsClick.COAL)
                    && B173BasicCraftsClick.emptyCraft(initial), "log/coal seed drifted");
            B173BasicCraftsClick.planks(actor);
            require(actor.inventory().slot(36).item().equals(B173BasicCraftsClick.PLANKS4)
                    && B173BasicCraftsClick.emptyCraft(actor.inventory()), "crafted planks 5x4 drifted");
            B173BasicCraftsClick.sticks(actor);
            require(actor.inventory().slot(38).item().equals(B173BasicCraftsClick.STICKS)
                    && actor.inventory().slot(38).item().legacyId() == 280
                    && actor.inventory().slot(36).item().equals(B173BasicCraftsClick.PLANKS2)
                    && B173BasicCraftsClick.emptyCraft(actor.inventory()), "crafted sticks 280 drifted");
            B173BasicCraftsClick.torches(actor);
            requireStored(actor.inventory());
            actor.close(); awaitPlayers(server, 0); server.save();
            require(server.player(user).inventoryItems() == 3, "basic craft persistence count drifted");
            reader = client(port, user, timeout); reader.connect(); reader.synchronizePose();
            requireStored(reader.awaitInventory());
            reader.close(); awaitPlayers(server, 0);
        } finally {
            actor.close(); if (reader != null) reader.close(); server.close();
        }
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|fixture=personal-2x2-log17+coal263|window0=log-to-planks5+vertical-1+3-sticks280+coal1-stick3-torch50"
                + "|cause=packet102-window0-left+button1-right-place|wire=packet106-accepted"
                + "|oracle=result5x4+result280x4+result50x4+fresh-login|" + SIGNAL;
        System.out.println("WORLDLINE_M297_CRAFTS=" + SIGNAL);
        System.out.println("WORLDLINE_M297_TRACE=" + trace);
        System.out.println("WORLDLINE_M297_SIGNATURE=" + sha256(trace));
    }

    private static B173WireClient client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void requireStored(RemoteInventoryView view) {
        require(!view.slot(36).empty() && view.slot(36).item().equals(B173BasicCraftsClick.PLANKS2)
                && !view.slot(37).empty() && view.slot(37).item().equals(B173BasicCraftsClick.TORCHES)
                && view.slot(37).item().legacyId() == 50
                && !view.slot(38).empty() && view.slot(38).item().equals(B173BasicCraftsClick.STICKS3)
                && view.slot(38).item().legacyId() == 280
                && B173BasicCraftsClick.emptyCraft(view) && view.occupiedSlots() == 3,
                "stored basic crafts 5+280+50 drifted"); }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L; while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return; Thread.sleep(100L); }
        throw new IllegalStateException("player count did not become " + count); }
    private static String sha256(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString(); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message); }
}
