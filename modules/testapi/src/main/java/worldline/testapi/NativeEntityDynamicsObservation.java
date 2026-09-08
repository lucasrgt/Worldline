package worldline.testapi;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Exact normalized native measurements for one paired causal dynamics case. */
public final class NativeEntityDynamicsObservation {
    private final NativeEntityDynamicsCase scene;
    private final Map<String, Long> measurements;

    public NativeEntityDynamicsObservation(NativeEntityDynamicsCase scene, Map<String, Long> measurements) {
        if (scene == null || measurements == null || measurements.isEmpty()) {
            throw new IllegalArgumentException("empty dynamics observation");
        }
        Map<String, Long> copy = new TreeMap<>();
        for (Map.Entry<String, Long> entry : measurements.entrySet()) {
            if (entry.getKey() == null || !entry.getKey().matches("[a-z][a-z0-9-]+")
                    || entry.getValue() == null) throw new IllegalArgumentException("dynamics measurement");
            copy.put(entry.getKey(), entry.getValue());
        }
        this.scene = scene; this.measurements = Collections.unmodifiableMap(copy);
    }
    public NativeEntityDynamicsCase scene() { return scene; }
    public Map<String, Long> measurements() { return measurements; }
    public long value(String key) {
        Long value = measurements.get(key);
        if (value == null) throw new IllegalArgumentException("missing dynamics measurement: " + key);
        return value;
    }
    public String canonical() {
        StringBuilder value = new StringBuilder(scene.canonical());
        for (Map.Entry<String, Long> entry : measurements.entrySet()) {
            value.append('|').append(entry.getKey()).append('=').append(entry.getValue());
        }
        return value.toString();
    }
}
