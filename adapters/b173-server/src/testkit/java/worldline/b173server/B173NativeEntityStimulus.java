package worldline.b173server;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Original causal setup with all resulting behavior dispatched to original server classes. */
public final class B173NativeEntityStimulus {
    private B173NativeEntityStimulus() { }
    public static List<NativeEntityDynamicsObservation> observe(Path mappings, boolean official,
            List<NativeEntityDynamicsCase> cases) throws Exception {
        B173NativeServerSymbols names = new B173NativeServerSymbols(mappings, official);
        List<NativeEntityDynamicsObservation> result = new ArrayList<>();
        try (B173NativePlayerFactory players = new B173NativePlayerFactory(names)) {
            for (NativeEntityDynamicsCase scene : cases) {
                try {
                    Map<String, Long> values = new TreeMap<>();
                    switch (scene.rule()) {
                        case SPAWN: B173NativeEntitySpawn.observe(names, scene, values); break;
                        case DAMAGE: B173NativeEntityDamage.observe(names, players, scene, values); break;
                        case DROPS: B173NativeEntityDrops.observe(names, players, scene, values); break;
                        case INTERACTION: interact(names, players, scene, values); break;
                        default: throw new IllegalArgumentException("unsupported stimulus rule");
                    }
                    result.add(new NativeEntityDynamicsObservation(scene, values));
                } catch (Exception error) {
                    throw new IllegalStateException("native stimulus scene: " + scene.canonical(), error);
                }
            }
        }
        return result;
    }
    private static void interact(B173NativeServerSymbols n, B173NativePlayerFactory p,
            NativeEntityDynamicsCase c, Map<String, Long> values) throws Exception {
        int id = c.legacyId();
        if (id == 11 || id == 93 || id == 94) B173NativeEntityBodyContact.observe(n, c, values);
        else if (id >= 49 && id <= 57) B173NativeEntityCombat.observe(n, p, c, values);
        else B173NativeEntityContact.observe(n, p, c, values);
    }
}
