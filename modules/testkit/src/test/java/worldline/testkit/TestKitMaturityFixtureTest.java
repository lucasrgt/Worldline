package worldline.testkit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import worldline.testapi.ExternalConsumerUse;

/** Time, repository identity and public provenance are independent release barriers. */
public final class TestKitMaturityFixtureTest {
    private TestKitMaturityFixtureTest() { }
    public static void execute() {
        List<ExternalConsumerUse> baseline = new ArrayList<>(List.of(
                use("Alpha", "2026-08-25", true, "alpha-old"),
                use("Alpha", "2026-09-08", true, "alpha-current"),
                use("Beta", "2026-08-25", false, "beta-private"),
                use("Beta", "2026-09-08", true, "beta-current")));
        TestKitMaturityEvidence blocked = assess(baseline);
        require(!blocked.readyForOne() && blocked.publicConsumers() == 2
                && blocked.sustainedConsumers().size() == 1, "private history promoted a release");
        reject(blocked::requireReadyForOne);
        baseline.add(use("Beta", "2026-09-08", true, "beta-repeat"));
        require(!assess(baseline).readyForOne(), "same-day repetitions counted as sustained use");
        baseline.add(use("Beta", "2026-08-25", true, "beta-public-old"));
        assess(baseline).requireReadyForOne();
        List<ExternalConsumerUse> reversed = new ArrayList<>(baseline); java.util.Collections.reverse(reversed);
        require(assess(baseline).equals(assess(reversed)), "maturity evidence ordering differs");
        baseline.add(use("Beta", "2026-09-09", true, "future")); reject(() -> assess(baseline));
        reject(() -> assess(List.of(use("Alpha", "2026-08-25", true, "one"),
                use("alpha", "2026-09-08", true, "two"))));
        reject(() -> assess(List.of(use("Alpha", "2026-08-25", true, "same"),
                use("Beta", "2026-09-08", true, "same"))));
    }
    private static ExternalConsumerUse use(String repo, String date, boolean publicOnly, String proof) {
        return new ExternalConsumerUse("https://github.com/synthetic/" + repo, "a".repeat(40),
                "0.3.0", date, 4, publicOnly, proof);
    }
    private static TestKitMaturityEvidence assess(List<ExternalConsumerUse> observations) {
        return TestKitMaturityFixture.assess(observations, LocalDate.parse("2026-09-08"), 7);
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid maturity observation accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
