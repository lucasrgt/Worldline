package worldline.b173server;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsObservation;

/** Original controlled fixture setup dispatching all observed actions into native server code. */
public final class B173NativeEntityDynamics {
    private B173NativeEntityDynamics() { }
    public static List<NativeEntityDynamicsObservation> observe(Path mappings, boolean official,
            List<NativeEntityDynamicsCase> cases) throws Exception {
        B173NativeServerSymbols names = new B173NativeServerSymbols(mappings, official);
        List<NativeEntityDynamicsObservation> result = new ArrayList<>();
        for (NativeEntityDynamicsCase scene : cases) {
            try {
                Map<String, Long> measurements = new TreeMap<>();
                if (scene.rule().template().equals("tick-lifecycle")) {
                    B173NativeEntityTimedState.observe(names, scene, measurements);
                } else B173NativeEntityMotion.observe(names, scene, measurements);
                result.add(new NativeEntityDynamicsObservation(scene, measurements));
            } catch (Exception error) {
                throw new IllegalStateException("native dynamics scene: " + scene.canonical(), error);
            }
        }
        return result;
    }
}
