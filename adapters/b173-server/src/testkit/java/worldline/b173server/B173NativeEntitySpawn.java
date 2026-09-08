package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Observes native world insertion and tracker packet serialization, without a network substitute. */
final class B173NativeEntitySpawn {
    private B173NativeEntitySpawn() { }
    static void observe(B173NativeServerSymbols n, NativeEntityDynamicsCase c, Map<String, Long> out)
            throws Exception {
        B173NativeEntityScene s = new B173NativeEntityScene(n, c);
        List<?> loaded = (List<?>) n.get(s.world, "loadedEntityList");
        out.put("before-member", loaded.contains(s.entity) ? 1L : 0L);
        out.put("joined", (Boolean) n.call(s.world, "World", "entityJoinedWorld",
                "(Lnet/minecraft/src/Entity;)Z", s.entity) ? 1L : 0L);
        out.put("after-member", loaded.contains(s.entity) ? 1L : 0L);
        Object tracker = n.type("EntityTrackerEntry").getConstructor(n.type("Entity"),
                int.class, int.class, boolean.class).newInstance(s.entity, 64, 3, true);
        Object packet = n.call(tracker, "EntityTrackerEntry", "getSpawnPacket", "()Lnet/minecraft/src/Packet;");
        String kind = n.named(packet.getClass());
        out.put("packet", kind.endsWith("/Packet21PickupSpawn") ? 21L
                : kind.endsWith("/Packet24MobSpawn") ? 24L : -1L);
        out.put("identity", n.get(packet, "entityId").equals(n.get(s.entity, "entityId")) ? 1L : 0L);
        long coordinates = 1;
        String[] axes = {"X", "Y", "Z"};
        for (String axis : axes) {
            long encoded = ((Number) n.get(packet, axis.toLowerCase(java.util.Locale.ROOT) + "Position")).longValue();
            if (encoded != (long) Math.floor(s.number("pos" + axis) * 32D)) coordinates = 0;
        }
        out.put("coordinates", coordinates);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        n.call(packet, "Packet", "writePacketData", "(Ljava/io/DataOutputStream;)V", new DataOutputStream(bytes));
        Object parsed = packet.getClass().getConstructor().newInstance();
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        n.call(parsed, "Packet", "readPacketData", "(Ljava/io/DataInputStream;)V", input);
        long roundtrip = input.available() == 0 ? 1 : 0;
        for (String field : new String[] {"entityId", "xPosition", "yPosition", "zPosition"}) {
            if (!n.get(packet, field).equals(n.get(parsed, field))) roundtrip = 0;
        }
        String[] payload = c.legacyId() == 1 ? new String[] {"itemID", "count", "itemDamage"}
                : new String[] {"type", "yaw", "pitch"};
        for (String field : payload) {
            String key = field.equals("itemID") ? "item-id" : field.equals("itemDamage") ? "item-damage" : field;
            out.put(key, ((Number) n.get(parsed, field)).longValue());
            if (!n.get(packet, field).equals(n.get(parsed, field))) roundtrip = 0;
        }
        out.put("roundtrip", roundtrip);
    }
}
