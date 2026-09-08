package worldline.atlas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Joins explicit catalog evidence IDs to existing experiments without inventing evidence. */
final class AtlasSemanticEvidenceLinks {
    private AtlasSemanticEvidenceLinks() { }

    static List<AtlasRecord> link(List<AtlasRecord> records) {
        Map<String, Set<String>> links = new LinkedHashMap<String, Set<String>>();
        for (AtlasRecord role : records) {
            if (!AtlasKind.ROLE.equals(role.kind())) continue;
            for (String evidence : role.evidence()) {
                String id = "atlas.experiment." + evidence;
                Set<String> refs = links.get(id);
                if (refs == null) {
                    refs = new LinkedHashSet<String>();
                    links.put(id, refs);
                }
                refs.add(role.id());
                for (String ref : role.refs()) {
                    if (ref.startsWith("atlas.subsystem.")) refs.add(ref);
                }
            }
        }
        List<AtlasRecord> result = new ArrayList<AtlasRecord>();
        for (AtlasRecord record : records) {
            Set<String> extra = links.get(record.id());
            if (!AtlasKind.EXPERIMENT.equals(record.kind()) || extra == null) {
                result.add(record);
                continue;
            }
            Set<String> refs = new LinkedHashSet<String>(record.refs());
            refs.addAll(extra);
            result.add(AtlasRecord.of(record.id(), record.kind(), record.status(),
                    record.artifact(), record.scope(), record.subject(), record.control(),
                    record.denominator(), record.evidence(), new ArrayList<String>(refs)));
        }
        return result;
    }
}
