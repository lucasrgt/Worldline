package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public session and TestKit boundaries for Beta 1.7.3 dimension lifecycle. */
final class DimensionSemantics {
    private DimensionSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("dimension", "DIMENSION_TYPED_SESSION_API",
                        "worldline/api/DimensionSession", "method", "awaitDimension", "(I)I",
                        "DIMENSION", "DIMENSION", "DIMENSION",
                        "m131-dual-dimension-session", "", 9998),
                SemanticMapping.of("dimension", "DIMENSION_RESPAWN_SESSION_API",
                        "worldline/api/RespawnSession", "method", "respawn",
                        "()Lworldline/api/RemoteRespawn;",
                        "DIMENSION", "DIMENSION", "DIMENSION",
                        "m136-nether-death-respawn", "", 9998),
                SemanticMapping.of("dimension", "DIMENSION_PORTAL_REENTRY_TESTKIT",
                        "worldline/testkit/PortalReentryCooldownFixture", "method", "verify",
                        "(Lworldline/testkit/PortalReentryCooldownFixture$Trial;"
                                + "Lworldline/testkit/PortalReentryCooldownFixture$Trial;IIII)"
                                + "Lworldline/testkit/PortalReentryCooldownFixture$Evidence;",
                        "DIMENSION", "DIMENSION", "DIMENSION",
                        "m652-portal-reentry-cooldown", "", 9998),
                SemanticMapping.of("dimension", "DIMENSION_PORTAL_BLOCK_TESTKIT",
                        "worldline/testkit/PortalBlockSubsystemFixture", "method", "execute",
                        "(Lworldline/testkit/PortalBlockSubsystemScenario;)"
                                + "Lworldline/testkit/PortalBlockSubsystemEvidence;",
                        "DIMENSION", "DIMENSION", "DIMENSION",
                        "b173-portal-block-subsystem-conformance-cycle", "", 9998)));
    }
}
