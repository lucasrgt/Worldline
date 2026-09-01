/** Exact reviewed successors for the complete public slime lifecycle subsystem. */
final class SlimeLifecycleSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "f884b07f718ae7709de3460073affac0a824eac5b566903560cc8a2aae387d0e",
                "dfc04f18b345ad0fc5b5d5b00ae6fcb1e6e3b93a93ccea9da52ccec579addac9"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "60e21dc4fa2cad491ede1f2fb8e823b859879501d6b72036a944dce9b6d02716",
                "64acfe3fc58ccbf54546cc2fcb1d008b750d2324117a67d5ae7c6cd3b372ed67"},
        {"docs/ATLAS.md",
                "917ded6d50dfe742bf5678d07486029a492aa1a163bbe622fae9b2f074354375",
                "8419b4dcd49bc4b0b467768b6f6198ff36a46e61afacc6d934d0fbf1094c4763"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "44cadd0af89e127e8893f8950289c483dae64c774ba1ecd96ae921388dad5693",
                "dd4a9d7fcf3301717be1025475f9a183bc38aa294fefdc5f2128ba1c786054a8"},
        {"docs/TESTKIT.md",
                "f5ce25dc632fd470c8ee0a06432a969f4ce1a8749506479021acac9bff087420",
                "61bd4c072ce1cdb85e372c6538929d3dbdedcf28a6a493c4067f7ef7167ddf64"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "413852e90c1186a3e3a743265c7bf1a9e0b4d7dc7af48237d605c435465b76c6",
                "37df0e1f6889d45efbd05cbfe1fa80e5a2654bffac10b7499d0fce686a7da357"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "8d3742cbf05d8f9b586078842ebdfcfed37a2d47ce6ccf868398d167061e0f72",
                "26cb09994dc2ba43c62599dc500263dc7f56039d33d2c8d4073112206601fe19"},
        {"release/testkit-artifacts.lock",
                "739bd226a6d35bdc24bcb905dfb17927016327e3bb088d6b6235b590840db5fa",
                "1c73d417d887adbc976b338fbb7312a389d432420e3c144b3f608d86083dc7c9"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "6352a92b3afc7b1e18bf6d41bbecf187ec86dd587fe7660993a4108c2227596f",
                "5855f4d9917a2a4fe66bdfd3ff3f9c7ee4f5f0da921906b59351a50e324a7c88"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "a0f298328afd9bee544775b3a8d1b2cfcb7c1a9b3f5decfe0490cc84f3d9a743",
                "e950a0f19554c8c6261e36d52a0d568e09df0ed4a0e7aa862480c030886724d4"}
    };

    private SlimeLifecycleSubsystemSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS) {
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        }
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "slime lifecycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "slime lifecycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "slime lifecycle successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
