package worldline.b173;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.List;
import worldline.analysis.CensusDocument;
import worldline.analysis.CensusRunner;

/**
 * Captures the controlled b1.7.3 client registry census: blocks, items,
 * entities, crafting recipes, and furnace smelts as canonical documents.
 */
public final class B173CensusRunner implements CensusRunner {
    private static final List<String> SECTIONS =
            Arrays.asList("blocks", "items", "entities", "recipes", "smelts");

    @Override
    public List<String> sections() { return SECTIONS; }

    @Override
    public String section(String name) {
        TreeMap<String, String> rows = rows(name);
        if (rows == null) throw new IllegalArgumentException("unknown census section " + name);
        return CensusDocument.section(name, rows);
    }

    private static TreeMap<String, String> rows(String name) {
        switch (name) {
            case "blocks": return B173Registries.blocks();
            case "items": return B173Registries.items();
            case "entities": return B173Registries.entities();
            case "recipes": return B173Registries.recipes();
            case "smelts": return B173Registries.smelts();
            default: return null;
        }
    }
}
