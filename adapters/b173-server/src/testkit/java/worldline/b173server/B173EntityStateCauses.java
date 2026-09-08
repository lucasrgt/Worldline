package worldline.b173server;

import java.util.Map;
import worldline.testapi.NativeEntityDynamicsCase;

/** Native environmental entry points applied to fresh, paired living entities. */
final class B173EntityStateCauses {
    private B173EntityStateCauses() { }
    static B173NativeEntityScene scene(B173NativeServerSymbols n, int id, String type) throws Exception {
        return new B173NativeEntityScene(n, new NativeEntityDynamicsCase(id, type, "SINGULAR",
                NativeEntityDynamicsCase.Rule.DAMAGE));
    }
    static void observe(B173NativeServerSymbols n, Map<String, Long> out) throws Exception {
        for (int id : new int[] {90, 57}) for (String cause : new String[] {
                "control", "fire", "lava", "fall", "drown", "suffocate"}) {
            B173NativeEntityScene s = scene(n, id, id == 90 ? "EntityPig" : "EntityPigZombie");
            String key = id + "-" + cause;
            out.put(key + "-before", (long) s.number("health"));
            switch (cause) {
                case "fire": n.call(s.entity, "Entity", "dealFireDamage", "(I)V", 1); break;
                case "lava": n.call(s.entity, "Entity", "setOnFireFromLava", "()V"); break;
                case "fall": n.call(s.entity, "Entity", "fall", "(F)V", 6F); break;
                case "drown":
                    s.fill(7, 9, 71, 75, 7, 9, 9); n.set(s.entity, "air", -19);
                    n.call(s.entity, "Entity", "onEntityUpdate", "()V"); break;
                case "suffocate":
                    s.fill(7, 9, 71, 75, 7, 9, 1);
                    n.call(s.entity, "Entity", "onEntityUpdate", "()V"); break;
                default: n.call(s.entity, "Entity", "onEntityUpdate", "()V");
            }
            out.put(key + "-after", (long) s.number("health"));
            out.put(key + "-fire", (long) s.number("fire"));
            out.put(key + "-air", (long) s.number("air"));
        }
    }
}
