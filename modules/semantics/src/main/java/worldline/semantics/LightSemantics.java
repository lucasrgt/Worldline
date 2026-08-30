package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundary for qualified Beta 1.7.3 light transport and response. */
final class LightSemantics {
    private LightSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("light", "LIGHT_STATIC_TRANSPORT_TESTKIT",
                        "worldline/testkit/BlockLightFixture", "method", "execute",
                        "(Lworldline/testkit/BlockLightScenario;"
                                + "Lworldline/api/BlockLightDriver;)"
                                + "Lworldline/testkit/BlockLightEvidence;",
                        "LIGHT", "LIGHT", "LIGHT",
                        "b173-static-light-transport-cycle", "", 9998),
                SemanticMapping.of("light", "LIGHT_STATIC_FAMILY_TESTKIT",
                        "worldline/testkit/BlockLightFamilyCycle", "method", "run",
                        "([Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;"
                                + "Lworldline/test/TestRuntimeProvider;Ljava/util/List;)"
                                + "Ljava/lang/String;",
                        "LIGHT", "LIGHT", "LIGHT",
                        "b173-static-light-transport-cycle", "", 9998),
                SemanticMapping.of("light", "LIGHT_SKY_BRIGHTNESS_TESTKIT",
                        "worldline/testkit/SkyBrightnessCycleFixture", "method", "observe",
                        "([J[I)Lworldline/api/SkyBrightnessCycleEvidence;",
                        "LIGHT", "LIGHT", "LIGHT",
                        "m654-sky-brightness-cycle", "", 9998),
                SemanticMapping.of("light", "LIGHT_DAYLIGHT_RESPONSE_TESTKIT",
                        "worldline/testkit/SpiderDaylightAggressionFixture", "method", "exercise",
                        "(Lworldline/api/SpiderDaylightAggressionActions;)"
                                + "Lworldline/api/SpiderDaylightAggressionEvidence;",
                        "LIGHT", "LIGHT", "LIGHT",
                        "m661-spider-daylight-aggression", "", 9998)));
    }
}
