package worldline.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import worldline.invariants.InvariantFields;
import worldline.semantics.SemanticFields;

/** Indexes closed trace-field aliases as Atlas field records. */
final class AtlasFieldImport {
    private AtlasFieldImport() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        for (Map.Entry<String, String> entry : SemanticFields.aliases().entrySet()) {
            List<String> refs = new ArrayList<String>();
            refs.add("atlas.role." + entry.getValue());
            String rule = InvariantFields.rule(entry.getKey());
            if (!rule.isEmpty()) refs.add("atlas.invariant." + rule);
            records.add(AtlasRecord.of("atlas.field." + entry.getKey(), AtlasKind.FIELD,
                    AtlasStatus.STRONG, AtlasSchema.WORLDLINE, AtlasSchema.SCOPE, entry.getValue(),
                    "OBSERVABILITY", 0, Collections.singletonList("semantic-fields"), refs));
        }
        for (Map.Entry<String, String> entry : InvariantFields.aliases().entrySet()) {
            if (SemanticFields.aliases().containsKey(entry.getKey())) continue;
            records.add(AtlasRecord.of("atlas.field." + entry.getKey(), AtlasKind.FIELD,
                    AtlasStatus.STRONG, AtlasSchema.WORLDLINE, AtlasSchema.SCOPE, entry.getValue(),
                    "ORACLE", 0, Collections.singletonList("invariant-fields"),
                    Arrays.asList("atlas.invariant." + entry.getValue())));
        }
        return Collections.unmodifiableList(records);
    }
}
