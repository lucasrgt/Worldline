/** Exact reviewed successors for the complete public controlled-entity dynamics matrix. */
final class EntityDynamicsMatrixSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "dfc04f18b345ad0fc5b5d5b00ae6fcb1e6e3b93a93ccea9da52ccec579addac9",
                "73d361e3140f1ef19b951b739b92f7a1740b9225942db675ed0e87249252e7ba"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "64acfe3fc58ccbf54546cc2fcb1d008b750d2324117a67d5ae7c6cd3b372ed67",
                "d8f4eeae865de3cd37607559687447ff1fe865e724cb4421b018cf13c5d40c1c"},
        {"docs/ATLAS.md",
                "8419b4dcd49bc4b0b467768b6f6198ff36a46e61afacc6d934d0fbf1094c4763",
                "49ad0095480f2ecbb0ffe3e48628dbde18e012b64887e47f0659a8b0ea910f02"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "dd4a9d7fcf3301717be1025475f9a183bc38aa294fefdc5f2128ba1c786054a8",
                "8b51620c671196d94d2212407b548db05f387e2f87d5d3e5d18c3a9e698c09fa"},
        {"docs/TESTKIT.md",
                "61bd4c072ce1cdb85e372c6538929d3dbdedcf28a6a493c4067f7ef7167ddf64",
                "127da0a855b72ea9e86055e1292487d5b10190ba4044cde67490836420a81cd1"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "37df0e1f6889d45efbd05cbfe1fa80e5a2654bffac10b7499d0fce686a7da357",
                "b61afbba5e89477b0625ea241512c6296ae7b18181aaa6ff1e65987e674306b4"},
        {"modules/testapi/src/main/java/worldline/testapi/SlimeLifecycleFixture.java",
                "0f02f3366886ade709a960f29ac0e294f666bf2c76e1685624c4f1b3c578064f",
                "3d3933b7485140195c7b28d589011df7caf1278596373adfdb0bd6ed66ac85de"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "26cb09994dc2ba43c62599dc500263dc7f56039d33d2c8d4073112206601fe19",
                "b9b8a11e7fea9e45d549325d3bb0b2de3c27810444f358def729200c073f82d0"},
        {"tools/harness/ModuleBuild.java",
                "81e384cef82c76c988d43c7c5d729040def73015df86812b34edfe8b9bed9111",
                "4f7c92322ed7faff95cc2d998fc603b7d33be8969d278a730db2092a766044e6"},
        {"tools/harness/TestBuild.java",
                "94298ce1eecb92c4809bd98eb7ff19027ba1aa64c9fac5cd32f2828b4ba676f8",
                "9cbacd8d0516a6eeb9f9eeb11234a91bc643ca0c5587ec9cb7b94657ae51ca33"},
        {"release/testkit-artifacts.lock",
                "1c73d417d887adbc976b338fbb7312a389d432420e3c144b3f608d86083dc7c9",
                "904d6bcfde3746882f3a6dc00963a5bf26473675488ef48b0052ef4b1af67587"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "5855f4d9917a2a4fe66bdfd3ff3f9c7ee4f5f0da921906b59351a50e324a7c88",
                "f3d89581c500170da18b3dfb5122a98cb8d6b13cb239f5a723611dc75ad8d6a7"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "e950a0f19554c8c6261e36d52a0d568e09df0ed4a0e7aa862480c030886724d4",
                "ecfd5dec1b8c7e77c1e28dd79b143dda15c18e99536b61e8e2d89fe66d2bf9fa"}
    };

    private EntityDynamicsMatrixSuccessor() { }

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
                    "entity dynamics successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity dynamics successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity dynamics successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
