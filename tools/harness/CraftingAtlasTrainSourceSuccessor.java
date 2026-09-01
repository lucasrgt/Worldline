/** Exact train successors introduced by the reviewed crafting Atlas integration. */
final class CraftingAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "19663fbd297d32c75f743ca42f35a54700bf8caa9530f30f713ad17c48cff8a7"},
        {"docs/generated/STATUS.md",
                "4427a26207df2e5fe976d021fcf9cd16fed69b98ce7f4bcae227ff896cc26155",
                "8144afdcdf3bcdca847dbe9ab6ef4b9fb3168be9438bfca08a8983f09ceae03b"},
        {"release/testkit-artifacts.lock",
                "d30282c42992d27c2378d01dfe4c16030f1f0fff5eeefec20b4d64a6b4daba60",
                "7b0d1ed09822479a89f61c0554ccd14639f65aead5923dab37aa77cecbd55002"},
        {"release/worldline.properties",
                "c01204d8d2400237d31b5aa1ea2a84d14311b1fc0bb005f3c675c4f0daf3ac0a",
                "2accc9480681e069d7a24231c778804ca52f0f40fcbc3642cb8a1cb1843753f4"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "75a4567f439360f6dae5ada487cacb30f037eb133a1616aa7197492f70b07495",
                "e7e8ba938341964ff044082d85327113ea0e15428a81f0f9c5c2a7b98c5439a7"},
        {"docs/SEMANTICS_CYCLE.md",
                "e1c58aa2b08e0787ae47c7b4f1f2d5242bf9df31705eee0cead1cd73c088dafa",
                "b1be5292e6a1ac64e2a2d6734bf69c5f1c6e8ebdcbb512af455ebd23f776a1fa"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "e0f385daaf7aeeaec9bddf93af8a732359a46965fe8b51e3dd94d325bd76c5ff",
                "5634b826194748b127918381bd1a649d687961c0ebd2dd901634e02576ba5bc5"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "d9132e8ba75e1abe32fcb1fe5580187ba9c13277f6c0e48f67e190b0c2a2432c",
                "e24d8db1a1186ec61609d94ef347b2cef4b32c0c9d669676ad1743bb0056da2a"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "1dab114d54ec29812273d5e2a36823e2c1fa36508ed11a45ee497449fb5bf563",
                "3c15077c8cf2b99f8c48e03bfa6fe4b826f543a3a58978007cff029a06c012c1"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "a9ea5de994012025eccd33131f8a7dd898cc7068580f14070fb9f483f87dc48a",
                "f7da956f3b153d0c2601e48c85a0ac60c11117e18672c951cdf75fd276467bcb"},
        {"tools/harness/RedstoneAtlasDocumentationSuccessor.java",
                "e6eaf02df2c4cc451430ff0ab44023a6722c204f2f694e32e59dbd07343a59cb",
                "6e3ac2f228a6c5d0fbbd1e708306d6886367aac9c455cd1117d5189843c52403"}
    };

    private CraftingAtlasTrainSourceSuccessor() { }

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
                    "crafting Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "crafting Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "crafting Atlas train successor allowlist drifted");
        CraftingAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
