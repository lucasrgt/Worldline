/** Exact train successors introduced by the reviewed lighting Atlas integration. */
final class LightingAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/SEMANTICS_CYCLE.md",
                "0f1ab0219b5a94d9855067cf265d453356221d8d4134d8959443bcd47af1df9b",
                "03303c01c7531860339dc43acfbc8120c88fa9bfbab04d924744fc848fb0ad00"},
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "f189928b8b5c14fe66b69d7726d6804cffc74e41829562f2b47fffedd1baf182"},
        {"docs/generated/STATUS.md",
                "d3d828bc8d02d43048efbb2aa2a18b2c1c16296f84d45db8680f408b70f592c0",
                "9ec1038f2d8c2e6716a990675a4b1b75199e34d228d00cfe336387085c08bb9b"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "89f97c82756b255cec6dcd4fdb41b05ae05aa54edb7ca555c9bc9ac8a40eee56",
                "b2861b9800e254b7af22bdef7197126222cc944b6ed27c2491b61b50684486f0"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "f00d3ab0b949f34d24ac8e6168f043c05d12143ca33c8134bc7fe1be5ae37733",
                "6ef96380b66f4ace90c41cb3742f556d59f8fc49fe7a29c2b2f5b120fb873ec0"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "a5309c42833406a7b626a1595519e1fa54601cb41807e46550916a295ecf9e0a",
                "96ace55225d9833e766c725bf960f0f91415e8a078f4926f9e4581aa08940d24"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "fc49df973b7c489a122b890f52295ee7048cabef0eecb1ad7f0878eddb7c72b6",
                "b4e6d0afbd590d69877bcf57c1fcff3c61129fd33f2789d6b089aaafb3b8ef2c"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "8174d6507ef18f654adc8f4405dd178d9e15b3669f1b5c417a9450140b5304ef",
                "41a25139954ef37a6aa3f17f4f5e75e234cbfd15fa7ef0b94519144bf011ce25"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "719f8e16cddf532e1151995dd2267d97feae49f420e794f04f3ba379da8d4b86",
                "8addcee8c18593918f02819e82f9c3469c51c1ec5bfdc2118794ce1d608a65fc"},
        {"release/testkit-artifacts.lock",
                "8b4d3bfa80227d938800b74c2836e50f677599862990194c714e3156bb032b28",
                "72c45ddad253f4586efda36b5fda8463e37184922afa495abd0f63aff1909519"},
        {"release/worldline.properties",
                "109749ff82be55709b990759989327e87d862dc85397f79093dcf51f44a951cf",
                "f73263e83b68c6e974d5ea6a602d9c2f6fc5aff9e5d8e296e05cec0e95f51c6e"},
        {"smokes/b173-static-light-transport-cycle/smoke.properties",
                "4bd106232d485ec7d9345c4da042d02a905c8f3f8314a5e554811db6d93fe551",
                "984ec3a44f15eceac1d15d8c28cec45407c1ea5136ab4739e533797caa0c2a88"},
        {"smokes/m654-sky-brightness-cycle/smoke.properties",
                "2ab1bcc3bc1544ee934d69878fedb4c1e4e16e0591d2b2021dc5788201072c50",
                "c048b9f1d2e661f7fefacdc399fd063ba0024a7a9ed176ad5de5fe61421cd26a"},
        {"smokes/m661-spider-daylight-aggression/smoke.properties",
                "24c1651c801a5ad4079ac34029dfe47edbab2b3ec163ac40d7372f913d7c0cd6",
                "af1db30775ac00b740b8c960e68e8a9c6d1c6ac93fb606f9f00d8af774fb3f95"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "2d7628b37317264c1462eaff947b5bf953a1fba46098f8076314c3c171905eca",
                "fce6db68190bab49ffa27c7f145d7df9610770a2075f2a9ba0aefa1ed0b9148c"},
        {"tools/harness/FluidAtlasDocumentationSuccessor.java",
                "3aedfa17e48ae86a661a6182bff77f99d1a9f9818e50eccbcc61865c7829fcc3",
                "da19fc3def2767e7e39ffa5c783f49988704816de68ac80003b6672c383306bc"}
    };

    private LightingAtlasTrainSourceSuccessor() { }

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
                    "lighting Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "lighting Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "lighting Atlas train successor allowlist drifted");
        LightingAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
