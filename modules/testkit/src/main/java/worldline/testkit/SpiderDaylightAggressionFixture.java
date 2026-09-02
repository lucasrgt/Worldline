package worldline.testkit;

import worldline.api.scenario.SpiderDaylightAggressionActions;
import worldline.api.scenario.SpiderDaylightAggressionEvidence;

/** Reusable bounded fixture for a spider daylight-to-night target differential. */
public final class SpiderDaylightAggressionFixture {
    private SpiderDaylightAggressionFixture() {
    }

    public static SpiderDaylightAggressionEvidence exercise(
            SpiderDaylightAggressionActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("missing spider daylight actions");
        }
        return SpiderDaylightAggressionEvidence.capture(
                actions.trial(SpiderDaylightAggressionActions.MAXIMUM_TARGET_ATTEMPTS),
                SpiderDaylightAggressionActions.MAXIMUM_TARGET_ATTEMPTS);
    }

    public static void compare(
            SpiderDaylightAggressionEvidence expected,
            SpiderDaylightAggressionEvidence observed) {
        if (expected == null || !expected.equals(observed)) {
            throw new IllegalStateException("spider daylight aggression evidence mismatch");
        }
    }
}
