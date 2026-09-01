/** Exact reviewed successors for the concrete entity physical-envelope subsystem. */
final class EntityPhysicalEnvelopeSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "b8171c36e83f8c8cb4cdb4fec6b857bb26914694a85793e7ec71c6992e35415f",
                "80f13cccaf9576c85675cff05a6097c74307deb94d8819abae3b6de67e3d6638"},
        {"behavior/functional-census/b1.7.3/entities/exceptions.tsv",
                "870462dbe30bc0bf81cde6df1d104e565823ca1bd1534b3ef7f09c4902973726",
                "0380432e3c1ea468c03b0263fd6560828be7d3e8d6e5045901a80b51f2e5e15a"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "91fc76dd926397e7453ecccef7b9ea82d7d946e6acf1e706dd7ff8ea4a49bb87",
                "44f099052b3afc38084a6f52e785b6341bb17d56cecd2cb1906c3140cf79e82b"},
        {"behavior/functional-census/families.tsv",
                "b70755d5878041afd1055bfa68b912d76a8f64ca4c4f577a329f126219e37f10",
                "da65f9d41453ade4a94958f456adcea951d74de4a5e5ec0b320ac46390fc17d3"},
        {"docs/ATLAS.md",
                "e140c94aa7ace1cb1c7f21ae5eea81fbaa50f27989bc00a0483e12259d3c7025",
                "d58aab772498e71ea71f54b5b3797bab2d7047a35a6e5b55beb8835a5d92cdd6"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "0bcc3e5cb9587b3af8eca8ec4b249ec3ca2ec8775047d9926130ed29d5d98b6c",
                "291b5bbd0448094c953a76b05b112717411120890281dade2602d36ce903803c"},
        {"modules/api/src/main/java/worldline/api/WorldlineBehaviorCatalog.java",
                "974789391b9c7d9d380a39f00033f0ae7177c6f5eb0802064a5206593976b91d",
                "6424e67e91e4c61c72620f8130219e6b73f7df2f557903e941feb7246d1b17cd"},
        {"modules/api/src/main/java/worldline/api/WorldlineFamily.java",
                "177261d3f0d3f288db97e3d75d7ef196e9e14dfc667ece0a5d4355c4ca627257",
                "364da6a2de65a627221fdaf592bc89bfe35e34706d67c1e334653a230396b8e8"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasBehaviorImport.java",
                "b22fe1b2b5994d8ef7cfeb737d017f473cd46d6fefc2f3963c7fc525e9619faa",
                "8e03f62de9cb7f04588feed7f99efc03e6db39cdffe41caca517b5daee5f9150"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "23c7689bc32f20a5ddc6cd0de1c9c2cb580c3e8de3d105a7cc175b28f56597af",
                "c134b39986ac2854251dcbdf8fc206719c3fa03ba33bc07d5d1127861da46e1e"},
        {"modules/cli/src/main/java/worldline/cli/CensusCommand.java",
                "50466e319b68078fd0b34418b8f43e30a9a9a5dd50d26e77c7cc9d1eeac2ccf5",
                "c275bddced321ef1730509a537ebfced2b7ca021b2624234ec255fe1f72988d5"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "a22fce8801724c598423d3b7f94913e7e41dd39023959ee06171a6c5e71ffb01",
                "bc3ccdadd2eaa68b4ee9814dd9c6654a157456f39040bacf769798a3ad26a409"},
        {"release/testkit-artifacts.lock",
                "44989fe93025ff21e456cc3b70dffb7f3d152a997aa7da9f9771759a86270cbc",
                "0698bfc5024c0e558b90514ceabd7c725f0cebf9c575e6c1cfc4af3375498342"},
        {"tools/harness/BehaviorCompletenessCheck.java",
                "652e32015f658ad54a8b04471691d00b31d00c90bb08ceeff97bcbaa49401ec5",
                "ab05d06e9961c9efa8b72fea32168223e38b713ec88f9f1146ec887af923f0f4"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "797056371c16b71e63f566a60938416512ba77d03371a82b5f6172d7c22b9bdc",
                "f278bbba0f658872822714af3e42403699d21dd3db35c439e14d460f4b357909"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "06a3bee7b9e7af8ef3b9026b06f385c60544d7e74d964ef2f5580451f4ed2625",
                "4f738e7862fc902e7fc6af7c73c690ec54ea0fde77d0583863a7c4b68285d5d5"}
    };

    private EntityPhysicalEnvelopeSubsystemSuccessor() { }

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
                    "entity physical envelope successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity physical envelope successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity physical envelope successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
