package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Deterministic coverage and qualification boundaries for maintained mappings. */
final class MappingSemantics {
    private MappingSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("mappings", "MAPPINGS_COVERAGE_REPORT",
                        "worldline/symbolgraph/MappingCoverageReport", "method", "create",
                        "(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;"
                                + "Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)"
                                + "Lworldline/symbolgraph/MappingCoverageReport;",
                        "MAPPINGS", "", "MAPPINGS", "sem-m6-mapping-coverage", "", 9998),
                SemanticMapping.of("mappings", "MAPPINGS_COVERAGE_GATE",
                        "worldline/symbolgraph/MappingCoverageGate", "method", "verify",
                        "(Lworldline/symbolgraph/MappingCoverageReport;Ljava/nio/file/Path;)V",
                        "MAPPINGS", "MAPPINGS", "MAPPINGS",
                        "sem-m6-mapping-coverage", "", 9998),
                SemanticMapping.of("mappings", "MAPPINGS_BATCH_REPORT",
                        "worldline/symbolgraph/MappingBatchReport", "method", "create",
                        "(Lworldline/symbolgraph/MappingCoverageReport;"
                                + "Lworldline/symbolgraph/TinyMapping;"
                                + "Lworldline/symbolgraph/TinyMapping;"
                                + "Lworldline/symbolgraph/TinyMapping;"
                                + "Lworldline/symbolgraph/SymbolGraph;I)"
                                + "Lworldline/symbolgraph/MappingBatchReport;",
                        "MAPPINGS", "", "MAPPINGS", "sem-m13-complete-mapping-batch", "", 9998),
                SemanticMapping.of("mappings", "MAPPINGS_BATCH_GATE",
                        "worldline/symbolgraph/MappingBatchGate", "method", "verify",
                        "(Lworldline/symbolgraph/MappingBatchReport;Ljava/nio/file/Path;)V",
                        "MAPPINGS", "MAPPINGS", "MAPPINGS",
                        "sem-m13-complete-mapping-batch", "", 9998)));
    }
}
