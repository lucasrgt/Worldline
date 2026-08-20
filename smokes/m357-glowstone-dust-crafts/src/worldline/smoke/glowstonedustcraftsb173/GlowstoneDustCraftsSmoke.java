package worldline.smoke.glowstonedustcraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteInventoryView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173GlowstoneDustCrafts;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Crafts glowstone 89 from four dust 348 in personal window 0. */
public final class GlowstoneDustCraftsSmoke {
    private static final String SIGNAL = "dust=348x4:0,result=89x1:0,taken=true,"
            + "stored=36:89x1:0,grid=2x2,actions=7,persisted=true,clients=2,disconnect=clean";
    private GlowstoneDustCraftsSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 7) throw new IllegalArgumentException(
                "usage: GlowstoneDustCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        String user = arguments[4]; int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
        Duration timeout = Duration.ofSeconds(90);
        require(user.length() <= 16, "username exceeds 16");
        require(B173GlowstoneDustCrafts.DUST4.legacyId() == 348
                && B173GlowstoneDustCrafts.GLOWSTONE.legacyId() == 89, "glowstone dust identities drifted");
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = client(port, user, timeout), reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D,
                    new int[] {0}, new int[] {348}, new int[] {4}, new int[] {0});
            actor.connect(); actor.synchronizePose();
            RemoteInventoryView initial = actor.awaitInventory();
            require(initial.occupiedSlots() == 1 && initial.slot(36).item().equals(B173GlowstoneDustCrafts.DUST4)
                    && initial.slot(36).item().legacyId() == 348 && B173GlowstoneDustCrafts.emptyCraft(initial),
                    "glowstone dust 348 seed drifted");
            actor.awaitRemoteChunk(cx, cz);
            B173GlowstoneDustCrafts.apply(actor);
            requireStored(actor.inventory());
            actor.close(); awaitPlayers(server, 0); server.save();
            require(server.player(user).inventoryItems() == 1, "glowstone dust craft persistence count drifted");
            reader = client(port, user, timeout); reader.connect(); reader.synchronizePose();
            requireStored(reader.awaitInventory());
            reader.close(); awaitPlayers(server, 0);
        } finally {
            actor.close(); if (reader != null) reader.close(); server.close();
        }
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|fixture=personal-2x2-dust348x4|window0=2x2-dust-to-glowstone89"
                + "|cause=packet102-window0-left+button1-right-place|wire=packet106-accepted"
                + "|oracle=result89x1+fresh-login|" + SIGNAL;
        System.out.println("WORLDLINE_M357_CRAFTS=" + SIGNAL);
        System.out.println("WORLDLINE_M357_TRACE=" + trace);
        System.out.println("WORLDLINE_M357_SIGNATURE=" + sha256(trace));
    }

    private static B173WireClient client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static void requireStored(RemoteInventoryView view) {
        require(!view.slot(36).empty() && view.slot(36).item().equals(B173GlowstoneDustCrafts.GLOWSTONE)
                && view.slot(36).item().legacyId() == 89 && B173GlowstoneDustCrafts.emptyCraft(view)
                && view.occupiedSlots() == 1, "stored glowstone 89 from dust 348 drifted"); }
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
