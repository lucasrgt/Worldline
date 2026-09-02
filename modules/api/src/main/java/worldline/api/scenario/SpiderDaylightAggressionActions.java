package worldline.api.scenario;

/** Reusable action boundary for one daylight-to-night spider target trial. */
public interface SpiderDaylightAggressionActions {
    int MAXIMUM_TARGET_ATTEMPTS = 4;

    SpiderDaylightAggressionEvidence.Trial trial(int maximumAttempts);
}
