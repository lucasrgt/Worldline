package worldline.testkit;

import worldline.api.scenario.SpiderDaylightAggressionActions;
import worldline.api.scenario.SpiderDaylightAggressionEvidence;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineHostileBehaviors;

final class SpiderDaylightAggressionFixtureTest {
    private SpiderDaylightAggressionFixtureTest() {
    }

    static void execute() {
        SpiderDaylightAggressionEvidence first =
                SpiderDaylightAggressionFixture.exercise(new Fake());
        SpiderDaylightAggressionEvidence second =
                SpiderDaylightAggressionFixture.exercise(new Fake());
        SpiderDaylightAggressionFixture.compare(first, second);
        require(first.daylightTargetAbsent() && first.nightTargetPlayer()
                        && first.geometryPreserved()
                        && first.maximumAttempts() == 4,
                "spider daylight evidence drifted");
        require(WorldlineBehavior.require("spider-daylight-aggression")
                        == WorldlineHostileBehaviors.SPIDER_DAYLIGHT_AGGRESSION,
                "spider daylight behavior registration drifted");
        fail(() -> SpiderDaylightAggressionEvidence.capture(
                new Fake().wrongDaylight(), 4));
        fail(() -> SpiderDaylightAggressionEvidence.capture(
                new Fake().wrongGeometry(), 4));
    }

    private static final class Fake implements SpiderDaylightAggressionActions {
        @Override public SpiderDaylightAggressionEvidence.Trial trial(int maximumAttempts) {
            return valid(maximumAttempts, -1, spider());
        }

        SpiderDaylightAggressionEvidence.Trial wrongDaylight() {
            return valid(4, 7, spider());
        }

        SpiderDaylightAggressionEvidence.Trial wrongGeometry() {
            return valid(4, -1,
                    new SpiderDaylightAggressionEvidence.ActorState(7, 12, 65, 8));
        }

        private static SpiderDaylightAggressionEvidence.Trial valid(
                int maximumAttempts, int dayTarget,
                SpiderDaylightAggressionEvidence.ActorState after) {
            return new SpiderDaylightAggressionEvidence.Trial(
                    spider(), player(), dayTarget, true,
                    after, player(), 8, true, maximumAttempts);
        }

        private static SpiderDaylightAggressionEvidence.ActorState spider() {
            return new SpiderDaylightAggressionEvidence.ActorState(7, 11, 65, 8);
        }

        private static SpiderDaylightAggressionEvidence.ActorState player() {
            return new SpiderDaylightAggressionEvidence.ActorState(8, 8, 65, 8);
        }
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid spider daylight evidence accepted");
        } catch (IllegalStateException expected) {
            return;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
