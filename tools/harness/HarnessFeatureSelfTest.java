/** Runs focused self-tests for optional harness entry points. */
final class HarnessFeatureSelfTest {
    private HarnessFeatureSelfTest() { }
    static void core() throws Exception {
        SafeTreeDelete.selfTest();
        CacheUsage.selfTest();
        NightlyQualityCampaign.selfTest();
        MilestoneScaffold.selfTest();
        ChangelogCheck.selfTest();
        ReadmeStatus.selfTest();
        ModuleCacheMaintenance.main(new String[] {"--self-test"});
        SharedCacheMaintenance.main(new String[] {"--self-test"});
        CacheRebuildDrill.selfTest();
        VerificationStageCache.selfTest();
        TrainPinMigration.selfTest();
    }
    static void smoke() throws Exception {
        SmokeScheduleHistory.selfTest();
        LaneDifferential.selfTest();
        SmokeScheduleBaselineCheck.execute(java.nio.file.Path.of("").toAbsolutePath().normalize());
        SmokeStatementBudgetTest.execute();
        SmokeProductRootTest.execute();
    }
    static void aero() throws Exception {
        AeroSceneBudgetTest.execute();
        AeroLogRowTest.execute();
    }
}
