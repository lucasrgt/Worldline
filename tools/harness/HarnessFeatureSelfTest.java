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
        GateLatencyCheck.selfTest();
        CsmSuiteCheck.selfTest();
        MilestoneIdUniquenessCheck.selfTest();
        RepositoryVerify.qualificationBoundarySelfTest();
        TrainSourceHistory.selfTest();
        TrainPinMigration.selfTest();
        TrainGeneratedDocumentationMigration.selfTest();
        TestKitArtifactTrainSourceSuccessor.selfTest();
        RedstoneAtlasTrainSourceSuccessor.selfTest();
        EntityCensusAtlasSuccessor.selfTest();
        EntityLifecycleTestKitSuccessor.selfTest();
        ProviderDiscoveryTrainSuccessor.selfTest();
        MilestoneCatalogTrainSuccessor.selfTest();
        EntityLifecycleArtifactTrainSuccessor.selfTest();
        BoundedEntityArchetypeSuccessor.selfTest();
        ObjectMaterializationMatrixSuccessor.selfTest();
        PaintingLifecycleSubsystemSuccessor.selfTest();
        PaintingAdapterSplitSuccessor.selfTest();
        SlimeLifecycleSubsystemSuccessor.selfTest();
        EntityDynamicsMatrixSuccessor.selfTest();
        SheepLifecycleSubsystemSuccessor.selfTest();
        SheepLifecycleTestPlanSuccessor.selfTest();
        CandidateReadiness.selfTest();
        CandidateSourceClosure.selfTest();
        NeighborTestKitPinCheck.selfTest();
        SchemaPinCheck.selfTest();
    }
    static void smoke() throws Exception {
        SmokeScheduleHistory.selfTest();
        SmokeSuiteScheduler.selfTest();
        JavaTokenText.selfTest();
        WorldTemplate.selfTest();
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
