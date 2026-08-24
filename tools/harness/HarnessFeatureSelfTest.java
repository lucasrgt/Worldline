/** Runs focused self-tests for optional harness entry points. */
final class HarnessFeatureSelfTest {
    private HarnessFeatureSelfTest() { }
    static void execute() throws Exception {
        SafeTreeDelete.selfTest();
        CacheUsage.selfTest();
        NightlyQualityCampaign.selfTest();
        MilestoneScaffold.selfTest();
        ChangelogCheck.selfTest();
        ReadmeStatus.selfTest();
        SmokeScheduleHistory.selfTest();
        LaneDifferential.selfTest();
        SmokeScheduleBaselineCheck.execute(java.nio.file.Path.of("").toAbsolutePath().normalize());
        SmokeStatementBudgetTest.execute();
        AeroSceneBudgetTest.execute();
        AeroLogRowTest.execute();
        ModuleCacheMaintenance.main(new String[] {"--self-test"});
        SharedCacheMaintenance.main(new String[] {"--self-test"});
        CacheRebuildDrill.selfTest();
        SmokeProductRootTest.execute();
        VerificationStageCache.selfTest();
    }
}
