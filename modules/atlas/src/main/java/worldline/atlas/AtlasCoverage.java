package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Declares subsystem-by-dimension coverage units. Numerators are evidence-backed. */
final class AtlasCoverage {
    private AtlasCoverage() {}

    static List<AtlasRecord> subsystems() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        for (String subsystem : AtlasSubsystems.ALL) {
            records.add(AtlasRecord.of("atlas.subsystem." + subsystem, AtlasKind.SUBSYSTEM,
                    AtlasStatus.UNKNOWN, AtlasSchema.CLIENT, AtlasSchema.SCOPE, subsystem, "", 0,
                    Collections.<String>emptyList(), Collections.<String>emptyList()));
        }
        return Collections.unmodifiableList(records);
    }

    static List<AtlasRecord> units(List<AtlasRecord> existing) {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        for (String subsystem : AtlasSubsystems.ALL) {
            for (String dimension : AtlasSubsystems.DIMENSIONS) {
                records.add(unit(existing, subsystem, dimension));
            }
        }
        return Collections.unmodifiableList(records);
    }

    static boolean filled(List<AtlasRecord> existing, String subsystem, String dimension) {
        if ("SEMANTIC".equals(dimension)) {
            return hasKind(existing, subsystem, AtlasKind.ROLE)
                    || hasKind(existing, subsystem, AtlasKind.MAPPING_SET)
                    || hasKind(existing, subsystem, AtlasKind.NAMESPACE);
        }
        if ("CONTROL".equals(dimension)) return controlled(existing, subsystem);
        if ("TESTABILITY".equals(dimension)) return hasKind(existing, subsystem, AtlasKind.EXPERIMENT)
                || signedKind(existing, subsystem, AtlasKind.CLAIM);
        if ("ORACLE".equals(dimension)) return oracle(existing, subsystem);
        if ("REPRODUCIBILITY".equals(dimension)) return signed(existing, subsystem);
        if ("OBSERVABILITY".equals(dimension)) {
            return hasKind(existing, subsystem, AtlasKind.ROLE)
                    || hasKind(existing, subsystem, AtlasKind.EXPERIMENT)
                    || hasKind(existing, subsystem, AtlasKind.INVARIANT)
                    || hasKind(existing, subsystem, AtlasKind.LOADER)
                    || hasKind(existing, subsystem, AtlasKind.API)
                    || hasKind(existing, subsystem, AtlasKind.MAPPING_SET)
                    || hasKind(existing, subsystem, AtlasKind.NAMESPACE)
                    || hasKind(existing, subsystem, AtlasKind.CLAIM);
        }
        if ("DETERMINISM".equals(dimension)) return determinism(existing, subsystem);
        throw new IllegalArgumentException("dimension " + dimension);
    }

    private static AtlasRecord unit(List<AtlasRecord> existing, String subsystem, String dimension) {
        boolean filled = filled(existing, subsystem, dimension);
        String status = filled ? filledStatus(dimension) : AtlasStatus.UNKNOWN;
        List<String> evidence = filled
                ? Collections.singletonList("declared-coverage-unit")
                : Collections.<String>emptyList();
        List<String> refs = Collections.singletonList("atlas.subsystem." + subsystem);
        return AtlasRecord.of("atlas.coverage-unit." + subsystem + "." + dimension,
                AtlasKind.COVERAGE_UNIT, status, AtlasSchema.WORLDLINE, AtlasSchema.SCOPE,
                subsystem + " " + dimension, filled ? "1" : "0", 1, evidence, refs);
    }

    private static String filledStatus(String dimension) {
        if ("SEMANTIC".equals(dimension) || "CONTROL".equals(dimension)
                || "ORACLE".equals(dimension) || "DETERMINISM".equals(dimension)) {
            return AtlasStatus.STRONG;
        }
        return AtlasStatus.OBSERVATIONAL;
    }

    private static boolean hasKind(List<AtlasRecord> existing, String subsystem, String kind) {
        for (AtlasRecord record : existing) {
            if (kind.equals(record.kind()) && refers(record, subsystem)) return true;
        }
        return false;
    }

    private static boolean controlled(List<AtlasRecord> existing, String subsystem) {
        for (AtlasRecord record : existing) {
            if (!AtlasKind.BOUNDARY.equals(record.kind()) || !refers(record, subsystem)) continue;
            if ("CONTROLLED".equals(record.control()) || "VIRTUALIZED".equals(record.control())
                    || "INTERCEPTED".equals(record.control())) {
                return true;
            }
        }
        return false;
    }

    private static boolean oracle(List<AtlasRecord> existing, String subsystem) {
        if (hasKind(existing, subsystem, AtlasKind.INVARIANT)
                || signedKind(existing, subsystem, AtlasKind.CLAIM)) return true;
        return signed(existing, subsystem);
    }

    private static boolean signed(List<AtlasRecord> existing, String subsystem) {
        for (AtlasRecord record : existing) {
            if ((!AtlasKind.EXPERIMENT.equals(record.kind())
                    && !AtlasKind.CLAIM.equals(record.kind())) || !refers(record, subsystem)) continue;
            for (String item : record.evidence()) {
                if (item.startsWith("expected.signature=") || AtlasSchema.shaToken(item)) return true;
            }
        }
        return false;
    }

    private static boolean signedKind(List<AtlasRecord> existing, String subsystem, String kind) {
        for (AtlasRecord record : existing) {
            if (!kind.equals(record.kind()) || !refers(record, subsystem)) continue;
            for (String item : record.evidence()) {
                if (item.startsWith("expected.signature=") || AtlasSchema.shaToken(item)) return true;
            }
        }
        return false;
    }

    private static boolean determinism(List<AtlasRecord> existing, String subsystem) {
        if (!controlled(existing, subsystem)) return false;
        if ("tick-lifecycle".equals(subsystem)) {
            return virtualized(existing, "CLOCK") || virtualized(existing, "RNG");
        }
        return true;
    }

    private static boolean virtualized(List<AtlasRecord> existing, String token) {
        for (AtlasRecord record : existing) {
            if (("atlas.boundary." + token).equals(record.id())
                    && "VIRTUALIZED".equals(record.control())) {
                return true;
            }
        }
        return false;
    }

    private static boolean refers(AtlasRecord record, String subsystem) {
        return record.refs().contains("atlas.subsystem." + subsystem);
    }
}
