package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Deterministic package, execution, and comparison boundaries for Worldline mods. */
final class ModEcosystemSemantics {
    private ModEcosystemSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                mapping("MOD_DESCRIPTOR_READ", "mods/ModDescriptor", "read",
                        "(Ljava/nio/file/Path;)Lworldline/mods/ModDescriptor;",
                        "m7-mod-loading"),
                mapping("MOD_ARTIFACT_INSPECT", "mods/ModLoader", "inspect",
                        "(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;)"
                                + "Lworldline/mods/ModArtifact;", "m7-mod-loading"),
                mapping("MOD_ENTRYPOINT_LOAD", "mods/ModLoader", "load",
                        "(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;"
                                + "Ljava/lang/Class;)Lworldline/mods/LoadedMod;",
                        "m7-mod-loading"),
                mapping("MOD_DEPENDENCY_ORDER", "mods/ModGraph", "order",
                        "(Ljava/util/List;)Ljava/util/List;", "m13-mod-graph"),
                mapping("MOD_TEST_RUN", "modtest/ModTestRunner", "run",
                        "(Ljava/nio/file/Path;JI)Lworldline/modtest/ModTestResult;",
                        "m12-mod-run"),
                mapping("MOD_TEST_RESULT_RECORD", "modtest/ModTestResult",
                        "createExecuted", "(Lworldline/mods/ModArtifact;"
                                + "Lworldline/trace/CanonicalStateDocument;JI)"
                                + "Lworldline/modtest/ModTestResult;", "m12-mod-run"),
                mapping("MOD_TEST_RESULT_PARSE", "modtest/ModTestResult", "parse",
                        "([B)Lworldline/modtest/ModTestResult;",
                        "m8-mod-version-diff,m12-mod-run"),
                mapping("MOD_TEST_RESULT_COMPARE", "modtest/ModTestComparison", "compare",
                        "(Lworldline/modtest/ModTestResult;Lworldline/modtest/ModTestResult;)"
                                + "Lworldline/modtest/ModTestComparison;",
                        "m8-mod-version-diff,m12-mod-run")));
    }

    private static SemanticMapping mapping(String role, String owner, String name,
            String descriptor, String evidence) {
        return SemanticMapping.of("mod-ecosystem", role, "worldline/" + owner,
                "method", name, descriptor, "MOD_ECOSYSTEM", "MOD_ECOSYSTEM",
                "MOD_ECOSYSTEM", evidence, "", 9998);
    }
}
