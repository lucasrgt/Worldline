/** Exact train successors introduced by the reviewed dimension Atlas integration. */
final class DimensionAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/SEMANTICS_CYCLE.md", "cb63d401f092255bb8433e7898e97ee464f9445a28e0d388a2c669803620946f", "7483c41b2e666959dc888569643bf62ef96236abc52bccec3c7a3dcb2760e7ba"},
        {"docs/generated/MILESTONES.md", "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02", "19663fbd297d32c75f743ca42f35a54700bf8caa9530f30f713ad17c48cff8a7"},
        {"docs/generated/STATUS.md", "5fc212904252ca56b6266547f9097f7f7ddcfc720a275385605793b5cd5b12b2", "39d859fe63fac3e6246201ad3f90ff9a06d4beace9049b01acc54cb663d86fc6"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java", "057e5787d78763077f63aa69e104ade4d57c214cc069f73aa55db154e5bc794b", "8bf6b72b6a207739eef7a2455154accdcaefda7f8931d273266fa2844a3fcf1a"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java", "5c2473eb959c7fe6bd0ccc41ab6382fdfdf501d6c695ee31a1133a6c817f1945", "b211867b9cfa0ab54797fbf83dfb8a789c97b4acde1fe17c998241f367c6dd9c"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java", "0b801d7acc5f83349af33ff4568ada0c20bd3aaab1b0424a2d1eefa7dcd3558f", "94b7fdec9d468f77ad5eedfcc081250b74745caf1eb504ffdd188ccb0e5d2814"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java", "aa5a7e16544a47c77e17bea42d6926e5dbc25cd1d372cd59a7880ab92ff5931a", "f075cb76d1f7ab52c2c499cf68aa16ab2c3759a3ad7dd4c1fc970b0e27b408e6"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java", "b35107065b7cf2c1151780c210ceaa2efb5f108602ef9d5ecf017fc3f055a249", "db32e4f5e4f9c90e9d582fd232cd45679b8bc642b17533ae58914dbe772bac11"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java", "7d2bf32ff224bf1deb27db6d07efe49db79529c3a39e64dbda1dabf4db7dd406", "286ed7e8f26f4468e482c4bd96375afd2eba2b14b38307854ab337d12b1d7dd4"},
        {"release/worldline.properties", "add62c0ef31576c803e24ea5d2ca5fcfa9ae794745c9a8ab39254a1fdba5de46", "7adeba786473853c2a94392b4dc34042bef91b2f80ea3a8a8f32a56b997f277c"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java", "42731ad51b4e1786a893bd021dac1952cc40526f930fae80eceab31cecd4c3d3", "2bf204e702a0c2c64dd437edd5403d01ff4152b512d6de67fa51433fdbcb1df3"},
        {"tools/harness/MobAiBehaviorCatalogPlacementSuccessor.java", "0ad249d166bd154474fb50572a7967d686b95d03ac2878d2ca21058582a363e4", "77ce3c96e242ac5562489ba5da46b2faeb276076c2ab32b91847fc3bc4749757"}
    };

    private DimensionAtlasTrainSourceSuccessor() { }
    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }
    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]), "dimension Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"), "dimension Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"), "dimension Atlas train successor allowlist drifted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
