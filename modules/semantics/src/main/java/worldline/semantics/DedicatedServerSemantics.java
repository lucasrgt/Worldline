package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public lifecycle and admission boundaries for the official dedicated server. */
final class DedicatedServerSemantics {
    private DedicatedServerSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("dedicated-server", "DEDICATED_SERVER_BOOT_API",
                        "worldline/api/DedicatedServerRuntime", "method", "boot", "()V",
                        "DEDICATED_SERVER", "DEDICATED_SERVER", "DEDICATED_SERVER",
                        "m20-server-bootstrap", "", 9998),
                SemanticMapping.of("dedicated-server", "DEDICATED_SERVER_STATE_API",
                        "worldline/api/DedicatedServerRuntime", "method", "state",
                        "()Lworldline/api/ServerState;",
                        "DEDICATED_SERVER", "", "DEDICATED_SERVER",
                        "m20-server-bootstrap", "", 9998),
                SemanticMapping.of("dedicated-server", "DEDICATED_SERVER_ENTRY_POLICY_TESTKIT",
                        "worldline/testkit/ServerEntryPolicyFixture", "method", "observe",
                        "(IILjava/lang/String;ZLjava/lang/String;Z)"
                                + "Lworldline/testkit/ServerEntryPolicyFixture$Evidence;",
                        "DEDICATED_SERVER", "", "DEDICATED_SERVER",
                        "m656-server-admission-matrix", "", 9998),
                SemanticMapping.of("dedicated-server", "DEDICATED_SERVER_ENTRY_COMPARE_TESTKIT",
                        "worldline/testkit/ServerEntryPolicyFixture", "method", "compare",
                        "(Lworldline/testkit/ServerEntryPolicyFixture$Evidence;"
                                + "Lworldline/testkit/ServerEntryPolicyFixture$Evidence;)V",
                        "DEDICATED_SERVER", "", "DEDICATED_SERVER",
                        "m656-server-admission-matrix", "", 9998)));
    }
}
