package worldline.b173server;

import java.lang.reflect.Array;
import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Player pickup, milking, reciprocal mounting and unsupported interaction controls. */
final class B173NativeEntityContact {
    private B173NativeEntityContact() { }
    static void observe(B173NativeServerSymbols n, B173NativePlayerFactory players,
            NativeEntityDynamicsCase c, Map<String, Long> out) throws Exception {
        for (int active = 0; active < 2; active++) {
            B173NativeEntityScene s = new B173NativeEntityScene(n, c);
            Object player = players.create(s.world);
            String p = active == 0 ? "control-" : "active-";
            int id = c.legacyId();
            if (id == 1 || id == 10) {
                if (id == 1) n.set(s.entity, "delayBeforeCanPickup", active == 1 ? 0 : 10);
                else {
                    n.set(s.entity, "inGround", true); n.set(s.entity, "owner", player);
                    n.set(s.entity, "field_28012_a", true); n.set(s.entity, "arrowShake", active == 1 ? 0 : 2);
                }
                n.call(s.entity, "Entity", "onCollideWithPlayer", "(Lnet/minecraft/src/EntityPlayer;)V", player);
                inventory(n, player, p, out);
            } else if (id == 92) {
                Object inventory = n.getAs(player, "EntityPlayer", "inventory");
                Object stack = n.type("ItemStack").getConstructor(int.class, int.class, int.class)
                        .newInstance(active == 1 ? 325 : 1, 1, 0);
                n.call(inventory, "IInventory", "setInventorySlotContents",
                        "(ILnet/minecraft/src/ItemStack;)V", 0, stack);
                out.put(p + "accepted", interact(n, s.entity, player));
                inventory(n, player, p, out);
            } else if (id == 40 || id == 41) {
                out.put(p + "accepted", active == 1 ? interact(n, s.entity, player) : 0L);
                out.put(p + "mounted", n.getAs(player, "Entity", "ridingEntity") == s.entity ? 1L : 0L);
                out.put(p + "rider", n.get(s.entity, "riddenByEntity") == player ? 1L : 0L);
                if (active == 1) interact(n, s.entity, player);
                out.put(p + "dismounted", n.getAs(player, "Entity", "ridingEntity") == null
                        && n.get(s.entity, "riddenByEntity") == null ? 1L : 0L);
            } else {
                out.put(p + "accepted", active == 1 ? interact(n, s.entity, player) : 0L);
                out.put(p + "position", s.milli("posX"));
                out.put(p + "player-health", ((Number) n.getAs(player, "EntityLiving", "health")).longValue());
            }
            out.put(p + "dead", s.flag("isDead"));
        }
    }
    private static long interact(B173NativeServerSymbols n, Object entity, Object player) throws Exception {
        return (Boolean) n.call(entity, "Entity", "interact", "(Lnet/minecraft/src/EntityPlayer;)Z", player) ? 1 : 0;
    }
    private static void inventory(B173NativeServerSymbols n, Object player, String p, Map<String, Long> out)
            throws Exception {
        Object slots = n.get(n.getAs(player, "EntityPlayer", "inventory"), "mainInventory");
        long count = 0, id = 0;
        for (int i = 0; i < Array.getLength(slots); i++) {
            Object stack = Array.get(slots, i);
            if (stack == null) continue;
            id += ((Number) n.get(stack, "itemID")).longValue();
            count += ((Number) n.get(stack, "stackSize")).longValue();
        }
        out.put(p + "inventory-id", id); out.put(p + "inventory-count", count);
    }
}
