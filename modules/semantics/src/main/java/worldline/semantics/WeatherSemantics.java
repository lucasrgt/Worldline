package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundary for qualified Beta 1.7.3 weather transitions and effects. */
final class WeatherSemantics {
    private WeatherSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("weather", "WEATHER_RAIN_STOP_TESTKIT",
                        "worldline/testkit/RainStopFixture", "method", "observe",
                        "(Lworldline/api/RemoteRainStop;)Lworldline/api/RemoteRainStop;",
                        "WEATHER", "WEATHER", "WEATHER",
                        "m655-rain-stop-event", "", 9998),
                SemanticMapping.of("weather", "WEATHER_SNOW_ACCUMULATION_TESTKIT",
                        "worldline/testkit/SnowAccumulationFixture", "method", "verify",
                        "(ILworldline/testkit/SnowAccumulationFixture$Pass;)"
                                + "Lworldline/testkit/SnowAccumulationFixture$Evidence;",
                        "WEATHER", "WEATHER", "WEATHER",
                        "m640-snow-accumulation", "", 9998),
                SemanticMapping.of("weather", "WEATHER_SNOW_NONSTACKING_TESTKIT",
                        "worldline/testkit/SnowLayerNonstackingFixture", "method", "verify",
                        "(IILworldline/testkit/SnowLayerNonstackingFixture$Pass;)"
                                + "Lworldline/testkit/SnowLayerNonstackingFixture$Evidence;",
                        "WEATHER", "WEATHER", "WEATHER",
                        "m663-snow-layer-nonstacking", "", 9998),
                SemanticMapping.of("weather", "WEATHER_LIGHTNING_CREEPER_TESTKIT",
                        "worldline/testkit/PoweredCreeperFixture", "method", "exercise",
                        "(Lworldline/api/PoweredCreeperActions;Ljava/lang/Runnable;)"
                                + "Lworldline/api/PoweredCreeperEvidence;",
                        "WEATHER", "WEATHER", "WEATHER",
                        "m659-powered-creeper", "", 9998)));
    }
}
