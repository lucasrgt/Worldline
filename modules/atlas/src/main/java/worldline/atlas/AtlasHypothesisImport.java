package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Factory for closed hypothesis records. Evidence tokens cannot contain commas. */
final class AtlasHypothesisImport {
    private AtlasHypothesisImport() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        records.addAll(AtlasAeroHypotheses.load());
        records.addAll(AtlasScopeHypotheses.load());
        records.addAll(AtlasClusterHypotheses.load());
        return Collections.unmodifiableList(records);
    }

    static AtlasRecord item(String token, String status, String subject, String control,
            String evidence, String... refs) {
        List<String> evidenceTokens = new ArrayList<String>();
        for (String item : evidence.split(" ")) {
            if (!item.isEmpty()) evidenceTokens.add(item.replace(',', '+'));
        }
        List<String> refList = new ArrayList<String>();
        Collections.addAll(refList, refs);
        return AtlasRecord.of("atlas.hypothesis." + token, AtlasKind.HYPOTHESIS, status,
                AtlasSchema.WORLDLINE, AtlasSchema.SCOPE, subject, control, 0,
                evidenceTokens, refList);
    }
}
