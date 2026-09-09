/** Closed registry of exact reviewed source transitions for train reconciliation. */
final class TrainSourceSuccessors {
    private TrainSourceSuccessors() { }

    static void selfTest() {
        boolean launcher = carries("tools/replay/Replay.java",
                "52fff7782a711481254b526af31b967fcb63a96b008b8b0f57b830e9dc7ea3e1",
                "d5eefbdb24b53509dfe3902f4cfd6cc70c44d362c779f801fd6f7ab61db4782f");
        boolean sdk = carries("README.md",
                "20af1156f488c2af8fbcf68adcf08c1eca8045476f3a2b158f34c25f0d2c7612",
                "2a402179a8cf5bec79d58129cdde09bb87cb716789e26862468a5af0e815c8ff");
        if (!launcher || !sdk || carries("unreviewed", "old", "new")
                || carries("README.md", "unreviewed", "new")) {
            throw new IllegalStateException("closed train successor routing drifted");
        }
    }

    static boolean carries(String relative, String prior, String current) {
        return TestKitArtifactTrainSourceSuccessor.carries(relative, prior, current)
                || RedstoneAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || RedstoneAtlasDocumentationSuccessor.carries(relative, prior, current)
                || CraftingAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || CraftingAtlasDocumentationSuccessor.carries(relative, prior, current)
                || TileEntityAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || TileEntityAtlasDocumentationSuccessor.carries(relative, prior, current)
                || FluidAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || FluidAtlasDocumentationSuccessor.carries(relative, prior, current)
                || LightingAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || LightingAtlasDocumentationSuccessor.carries(relative, prior, current)
                || LightingCoverageTrainSourceSuccessor.carries(relative, prior, current)
                || WeatherAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || WeatherAtlasDocumentationSuccessor.carries(relative, prior, current)
                || MobAiAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || MobAiAtlasDocumentationSuccessor.carries(relative, prior, current)
                || MobAiBehaviorManifestSuccessor.carries(relative, prior, current)
                || MobAiBehaviorCatalogPlacementSuccessor.carries(relative, prior, current)
                || DimensionAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || DimensionAtlasDocumentationSuccessor.carries(relative, prior, current)
                || DimensionAtlasFormattingSuccessor.carries(relative, prior, current)
                || DimensionArtifactSuccessor.carries(relative, prior, current)
                || WorldgenAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || WorldgenAtlasDocumentationSuccessor.carries(relative, prior, current)
                || WorldgenArtifactSuccessor.carries(relative, prior, current)
                || WorldgenDataCycleAttestationSuccessor.carries(relative, prior, current)
                || DedicatedServerAtlasTrainSourceSuccessor.carries(relative, prior, current)
                || DedicatedServerAtlasDocumentationSuccessor.carries(relative, prior, current)
                || DedicatedServerArtifactSuccessor.carries(relative, prior, current)
                || MappingAtlasSuccessor.carries(relative, prior, current)
                || MappingReleaseSuccessor.carries(relative, prior, current)
                || MappingDocumentationSuccessor.carries(relative, prior, current)
                || StationApiAtlasSuccessor.carries(relative, prior, current)
                || StationApiDocumentationSuccessor.carries(relative, prior, current)
                || AeroAtlasSuccessor.carries(relative, prior, current)
                || AeroDocumentationSuccessor.carries(relative, prior, current)
                || ModEcosystemAtlasSuccessor.carries(relative, prior, current)
                || ModEcosystemDocumentationSuccessor.carries(relative, prior, current)
                || EntityCensusAtlasSuccessor.carries(relative, prior, current)
                || EntityLifecycleTestKitSuccessor.carries(relative, prior, current)
                || ProviderDiscoveryTrainSuccessor.carries(relative, prior, current)
                || MilestoneCatalogTrainSuccessor.carries(relative, prior, current)
                || IntegratedMilestoneTrainDocumentationSuccessor.carries(
                        relative, prior, current)
                || FinalReconciliationTrainSourceSuccessor.carries(
                        relative, prior, current)
                || FinalArtifactHashTrainSourceSuccessor.carries(
                        relative, prior, current)
                || GithubActionsTrainSourceSuccessor.carries(relative, prior, current)
                || TemurinPinTrainSourceSuccessor.carries(relative, prior, current)
                || ReconciledTrainSourceSuccessor.carries(relative, prior, current)
                || EntityLifecycleArtifactTrainSuccessor.carries(relative, prior, current)
                || BoundedEntityArchetypeSuccessor.carries(relative, prior, current)
                || ObjectMaterializationMatrixSuccessor.carries(relative, prior, current)
                || PaintingLifecycleSubsystemSuccessor.carries(relative, prior, current)
                || PaintingAdapterSplitSuccessor.carries(relative, prior, current)
                || SlimeLifecycleSubsystemSuccessor.carries(relative, prior, current)
                || EntityDynamicsMatrixSuccessor.carries(relative, prior, current)
                || SheepLifecycleSubsystemSuccessor.carries(relative, prior, current)
                || SheepLifecycleTestPlanSuccessor.carries(relative, prior, current)
                || ChickenEggFamilySubsystemSuccessor.carries(relative, prior, current)
                || HonestEntityVerificationFloorSuccessor.carries(relative, prior, current)
                || WolfOwnerStateSubsystemSuccessor.carries(relative, prior, current)
                || FallingSandLifecycleSubsystemSuccessor.carries(relative, prior, current)
                || TntLifecycleSubsystemSuccessor.carries(relative, prior, current)
                || HonestArrowLifecycleCorrectionSuccessor.carries(relative, prior, current)
                || HonestSquidLandResponseCorrectionSuccessor.carries(relative, prior, current)
                || HostileBehaviorMatrixSuccessor.carries(relative, prior, current)
                || HostilePacketIdentityCollisionSuccessor.carries(relative, prior, current)
                || PigLifecycleSubsystemSuccessor.carries(relative, prior, current)
                || EntityPhysicalEnvelopeSubsystemSuccessor.carries(relative, prior, current)
                || EntityPhysicalEnvelopeDocumentationSuccessor.carries(relative, prior, current)
                || EntityRegistryCensusSuccessor.carries(relative, prior, current)
                || EntityRegistrySchemaSuccessor.carries(relative, prior, current)
                || EntityRegistryDocumentationSuccessor.carries(relative, prior, current)
                || AtlasLauncherSourceSuccessor.carries(relative, prior, current)
                || AtlasLauncherPortabilitySuccessor.carries(relative, prior, current)
                || ProviderRefreshAncestrySuccessor.carries(relative, prior, current)
                || SourceLauncherQualificationSuccessor.carries(relative, prior, current)
                || EntityAtlasDocumentationSuccessor.carries(relative, prior, current)
                || ExtensionSdkProgressTrainSuccessor.carries(relative, prior, current);
    }
}
