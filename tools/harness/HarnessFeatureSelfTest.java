/** Runs focused self-tests for optional harness entry points. */
final class HarnessFeatureSelfTest {
    private HarnessFeatureSelfTest() { }
    static void execute() throws Exception {
        NightlyQualityCampaign.selfTest();
        MilestoneScaffold.selfTest();
        ChangelogCheck.selfTest();
    }
}
