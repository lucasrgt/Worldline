package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Vanilla sound manager and headless audio-boundary symbols for b1.7.3.
 */
final class AudioSemantics {
    private AudioSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("audio", "SOUND_MANAGER", "net/minecraft/src/SoundManager",
                        "class", "SoundManager", "-", "", "AUDIO", "AUDIO",
                        "controlled-client-tick", "", 9200),
                SemanticMapping.of("audio", "HEADLESS_AUDIO", "worldline/b173/B173Boundaries",
                        "class", "B173Boundaries", "-", "", "AUDIO", "AUDIO",
                        "lab-cycle,controlled-client-tick", "", 9850)));
    }
}
