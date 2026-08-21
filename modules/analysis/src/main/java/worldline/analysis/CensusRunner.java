package worldline.analysis;

import java.util.List;

/** Neutral contract for capturing b1.7.3 registry census sections. */
public interface CensusRunner {
    /** Section names in canonical order (for example blocks, items, recipes). */
    List<String> sections();

    /** Canonical WORLDLINE-CENSUS/1 document for one section. */
    String section(String name);
}