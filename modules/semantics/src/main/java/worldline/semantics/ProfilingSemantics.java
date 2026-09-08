package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Sample aggregates and budget decisions; raw elapsed times remain machine-relative. */
final class ProfilingSemantics {
    private ProfilingSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("profiling", "PROFILE_SAMPLES",
                        "worldline/profiling/TickProfile", "method", "of",
                        "([J[J)Lworldline/profiling/TickProfile;",
                        "PROFILING", "", "PROFILING", "m17-profile", "", 9998),
                SemanticMapping.of("profiling", "PROFILE_PERCENTILE",
                        "worldline/profiling/TickProfile", "method", "p95", "()J",
                        "PROFILING", "", "PROFILING", "m17-profile", "", 9998),
                SemanticMapping.of("profiling", "PROFILE_BUDGET",
                        "worldline/profiling/ProfileBudget", "method", "violations",
                        "(Lworldline/profiling/TickProfile;)Ljava/util/List;",
                        "PROFILING", "", "PROFILING", "m17-profile", "", 9998)));
    }
}
