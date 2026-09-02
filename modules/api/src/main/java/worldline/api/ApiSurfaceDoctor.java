package worldline.api;

import worldline.api.query.ClientTopology;
import worldline.api.query.EntityQuery;
import worldline.api.query.EventQuery;
import worldline.api.query.WeatherQuery;
import worldline.api.scenario.PoweredCreeperActions;
import worldline.api.scenario.PoweredCreeperEvidence;
import worldline.api.scenario.SpiderDaylightAggressionActions;
import worldline.api.scenario.SpiderDaylightAggressionEvidence;

/** Fail-closed layout check for general primitives versus scenario-specific types. */
public final class ApiSurfaceDoctor {
    private ApiSurfaceDoctor() { }

    public static void verify() {
        requirePackage(EntityQuery.class, "worldline.api.query");
        requirePackage(EventQuery.class, "worldline.api.query");
        requirePackage(WeatherQuery.class, "worldline.api.query");
        requirePackage(ClientTopology.class, "worldline.api.query");
        requirePackage(PoweredCreeperEvidence.class, "worldline.api.scenario");
        requirePackage(PoweredCreeperActions.class, "worldline.api.scenario");
        requirePackage(SpiderDaylightAggressionEvidence.class, "worldline.api.scenario");
        requirePackage(SpiderDaylightAggressionActions.class, "worldline.api.scenario");
        String hole = remainingHole();
        if (!hole.isEmpty()) throw new IllegalStateException(hole);
    }

    public static String remainingHole() {
        return "";
    }

    private static void requirePackage(Class<?> type, String expected) {
        String actual = type.getPackage().getName();
        if (!expected.equals(actual))
            throw new IllegalStateException(type.getName() + " lives in " + actual
                    + ", expected " + expected);
    }
}
