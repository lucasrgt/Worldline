package worldline.mods;

/** Exact compatibility result for a Worldline mod descriptor. */
public enum ModCompatibility {
    COMPATIBLE,
    RUNTIME_MISMATCH,
    WORLDLINE_API_MISMATCH,
    RUNTIME_AND_API_MISMATCH;

    static ModCompatibility compare(boolean runtime, boolean api) {
        if (runtime && api) return COMPATIBLE;
        if (!runtime && !api) return RUNTIME_AND_API_MISMATCH;
        return runtime ? WORLDLINE_API_MISMATCH : RUNTIME_MISMATCH;
    }
}
