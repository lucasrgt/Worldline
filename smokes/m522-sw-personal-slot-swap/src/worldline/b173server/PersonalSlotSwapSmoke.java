package worldline.b173server;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteRejectedTransaction;

/** Exchanges two occupied personal slots and proves reject recovery plus relogin persistence. */
public final class PersonalSlotSwapSmoke {
    private PersonalSlotSwapSmoke() {}

    public static void main(String[] a) throws Exception {
        if (a.length != 5) throw new IllegalArgumentException(
                "usage: PersonalSlotSwapSmoke server.jar workspace port seed username");
        Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
        int port = Integer.parseInt(a[2]); long seed = Long.parseLong(a[3]); String user = a[4];
        Duration timeout = Duration.ofSeconds(90); RemoteItemStack stone = item(1), dirt = item(3);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = client(port, user, timeout), reader = null;
        try {
            server.boot();
            B173PlayerSeed.writeInventory(workspace, user, 4.5D, 80D, 4.5D,
                    new int[]{0, 1}, new int[]{1, 3}, new int[]{1, 1}, new int[]{0, 0});
            actor.connect(); actor.synchronizePose();
            RemoteInventoryView initial = actor.awaitInventory();
            require(stack(initial, 36).equals(stone) && stack(initial, 37).equals(dirt), "seed slots drifted");

            RemotePersonalTransaction first = actor.clickPersonalSlot(36);
            RemotePersonalTransaction second = actor.clickPersonalSlot(37);
            RemotePersonalTransaction third = actor.clickPersonalSlot(36);
            require(first.actionId() == 1 && first.after().slot(36).empty()
                    && first.cursorAfter().equals(stone), "take transition drifted");
            require(second.actionId() == 2 && stack(second.after(), 37).equals(stone)
                    && second.cursorAfter().equals(dirt), "occupied swap transition drifted");
            require(third.actionId() == 3 && stack(third.after(), 36).equals(dirt)
                    && stack(third.after(), 37).equals(stone) && third.cursorAfterEmpty(), "swap completion drifted");

            RemoteRejectedTransaction rejected = actor.rejectedTakeProbe(36);
            require(rejected.actionId() == 4 && rejected.stalePredictionEmpty()
                    && rejected.authoritative().slot(36).empty()
                    && stack(rejected.authoritative(), 37).equals(stone)
                    && rejected.cursorAfter().equals(dirt), "stale rejection recovery drifted");
            RemotePersonalTransaction restored = actor.clickPersonalSlot(36);
            require(restored.actionId() == 5 && stack(restored.after(), 36).equals(dirt)
                    && stack(restored.after(), 37).equals(stone) && restored.cursorAfterEmpty(), "restore drifted");

            actor.close(); awaitPlayers(server, 0); server.save();
            reader = client(port, user, timeout); reader.connect(); reader.synchronizePose();
            RemoteInventoryView persisted = reader.awaitInventory();
            require(stack(persisted, 36).equals(dirt) && stack(persisted, 37).equals(stone)
                    && persisted.occupiedSlots() == 2, "relogin swap persistence drifted");
            reader.close(); reader = null; awaitPlayers(server, 0); server.save();
            require(server.player(user).inventoryItems() == 2, "player NBT inventory count drifted");
        } finally { actor.close(); if (reader != null) reader.close(); server.close(); }

        String signal = "slots=36<->37,items=stone<->dirt,actions=1+2+3,reject=4-false,"
                + "resync=applied-take,restore=5-accepted,relogin=swapped,persisted=2,cursor=empty";
        String trace = "v1|server=official-b1.7.3|seed=" + seed + "|window=0|button=left"
                + "|positive=occupied-slot-exchange|negative=stale-prediction-authoritative-recovery|" + signal;
        System.out.println("WORLDLINE_M522_SET=" + signal);
        System.out.println("WORLDLINE_M522_TRACE=" + trace);
        System.out.println("WORLDLINE_M522_SIGNATURE=" + sha(trace));
    }

    private static B173WireClient client(int port, String user, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, user, timeout);
    }
    private static RemoteItemStack item(int id) { return new RemoteItemStack(id, 1, 0); }
    private static RemoteItemStack stack(RemoteInventoryView view, int slot) {
        require(!view.slot(slot).empty(), "slot " + slot + " is empty"); return view.slot(slot).item();
    }
    private static void awaitPlayers(B173DedicatedServer server, int expected) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        do { if (server.players().size() == expected) return; Thread.sleep(100L); }
        while (System.nanoTime() < deadline);
        throw new IllegalStateException("server player count did not reach " + expected);
    }
    private static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255)); return result.toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
