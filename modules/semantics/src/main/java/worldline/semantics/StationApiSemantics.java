package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Process-isolated StationAPI TestKit driver boundaries qualified by M620. */
final class StationApiSemantics {
    private StationApiSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                mapping("STATIONAPI_PROVIDER_OPEN", "StationApiTestRuntimeProvider", "open",
                        "(Lworldline/test/TestRuntimeRequest;)Lworldline/test/TestRuntimeSession;",
                        "", "STATIONAPI"),
                mapping("STATIONAPI_RUNTIME_TICK", "StationApiRuntime", "tick", "()V",
                        "STATIONAPI", "STATIONAPI"),
                mapping("STATIONAPI_RUNTIME_CLOSE", "StationApiRuntime", "close", "()V",
                        "", "STATIONAPI"),
                mapping("STATIONAPI_WORLD_TIME", "StationApiWorld", "time", "()J",
                        "STATIONAPI", ""),
                mapping("STATIONAPI_PLAYER_NAME", "StationApiPlayer", "username",
                        "()Ljava/lang/String;", "STATIONAPI", ""),
                mapping("STATIONAPI_PLAYER_HEALTH", "StationApiPlayer", "health", "()I",
                        "STATIONAPI", "")));
    }

    private static SemanticMapping mapping(String role, String owner, String name,
            String descriptor, String reads, String writes) {
        return SemanticMapping.of("stationapi", role, "worldline/stationapi/" + owner,
                "method", name, descriptor, reads, writes, "STATIONAPI",
                "m620-stationapi-testkit-driver", "", 9998);
    }
}
