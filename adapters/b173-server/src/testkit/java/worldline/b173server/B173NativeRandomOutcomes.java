package worldline.b173server;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import worldline.testapi.RandomOutcomeObservation;

/** Executes every bounded draw through native gravel and fishing methods in a controlled scene. */
public final class B173NativeRandomOutcomes {
    private B173NativeRandomOutcomes() { }
    public static List<RandomOutcomeObservation> observe(Path mappings, boolean official) throws Exception {
        B173NativeServerSymbols names = new B173NativeServerSymbols(mappings, official);
        for (String type : new String[] {"Block", "Item", "BlockGravel", "EntityFish", "World"}) {
            String origin = names.type(type).getProtectionDomain().getCodeSource().getLocation().toString();
            B173NativeEntityScene.require(origin.contains(official ? "jars/minecraft_server.jar" : "minecraft_server/bin/"),
                    "native random outcome provenance differs: " + origin);
        }
        List<RandomOutcomeObservation> rows = new ArrayList<RandomOutcomeObservation>();
        Object gravel = names.constant("Block", "gravel");
        for (int draw = 0; draw < 10; draw++) {
            B173FiniteDraws random = new B173FiniteDraws(new int[] {10}, new int[] {draw});
            int drop = (Integer) names.call(gravel, "Block", "idDropped", "(ILjava/util/Random;)I", 0, random);
            rows.add(new RandomOutcomeObservation("gravel/" + draw, random.trace(), 0, drop, 0, 0, false));
        }
        for (int depth : new int[] {1, 3, 5}) for (int bound : new int[] {500, 300}) {
            try (B173FishingScene scene = new B173FishingScene(names, depth, bound == 300)) {
                for (int draw = 0; draw < bound; draw++) {
                    scene.fresh();
                    rows.add(scene.tick("bite/" + depth + "/" + bound + "/" + draw,
                            draw == 0 ? new int[] {bound, 30} : new int[] {bound},
                            draw == 0 ? new int[] {0, 0} : new int[] {draw}, true));
                }
            }
        }
        try (B173FishingScene scene = new B173FishingScene(names, 3, false)) {
            for (int draw = 0; draw < 30; draw++) {
                scene.fresh(); String key = "window/" + draw + "/";
                rows.add(scene.tick(key + 0, new int[] {500, 30}, new int[] {0, draw}, false));
                for (int tick = 1; tick <= draw + 10; tick++)
                    rows.add(scene.tick(key + tick, new int[0], new int[0], false));
                rows.add(scene.observe(key + "expired", "", true));
            }
            for (int first : new int[] {1, 5, 17}) {
                scene.fresh();
                for (int tick = 1; tick <= first; tick++) rows.add(scene.tick("first/" + first + "/" + tick,
                        tick == first ? new int[] {500, 30} : new int[] {500},
                        tick == first ? new int[] {0, 0} : new int[] {1}, false));
            }
        }
        return rows;
    }
}
