package worldline.fuzz;

import java.nio.file.Path;
import java.util.List;

/**
 * Neutral provider that binds mod JARs (or none) to fuzzing subjects.
 * Adapters own implementations; the CLI loads one reflectively by name.
 */
public interface FuzzSubjectProvider {
    /**
     * Zero jars yields the vanilla determinism subject. One jar compares
     * vanilla against the mod. Two jars compare the two mods directly.
     */
    List<FuzzSubject> subjects(List<Path> jars);
}
