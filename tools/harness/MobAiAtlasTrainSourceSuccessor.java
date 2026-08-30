/** Exact train successors introduced by the reviewed mob-AI Atlas integration. */
final class MobAiAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/M622_CYCLE.md",
                "7f787d29b44c70b887d63b6b05b6413a9d748f1cfa6742741356e39ed7ba19b5",
                "f835b8febbace504e0cb0b258f383b1e0eb7e52032db71124ce8b7983a66bcf1"},
        {"docs/SEMANTICS_CYCLE.md",
                "79d2a5adf65a59f4005b3f572accd5321b406d22e248f2f97806883789f82707",
                "cb63d401f092255bb8433e7898e97ee464f9445a28e0d388a2c669803620946f"},
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "96df6b7e5bdcafb99db21c4c16179d0e0686d00c5b7106f1ea4507bc3d1ce82a"},
        {"docs/generated/STATUS.md",
                "012cf5899fbbc4d110e66b228a920f22dff9d2082711cbbbcfeefb16dc0626a8",
                "5fc212904252ca56b6266547f9097f7f7ddcfc720a275385605793b5cd5b12b2"},
        {"modules/api/src/main/java/worldline/api/WorldlineBehavior.java",
                "12fafb9abd30402c7cc7c9d0c78ef9bdbc5879bc5ff6d9d6aea5e387498551aa",
                "2a27dd3f6e5857ec7e94dd1d3f47c057ddadd40a675e43e1f6c1bd00f013cf90"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasIndex.java",
                "92b5e32230dd0efeb4979121a2e50e6dc0d92a09ce7849859d01ba455755941e",
                "bb79dd6ba61af92c943f560c85aad89f80662a031633ef1b8324f673ecbc64e4"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "e0f11397a8014ce8b5b33996ab75a3e3d1c942364b5448ea95b3aa1073b52eaf",
                "057e5787d78763077f63aa69e104ade4d57c214cc069f73aa55db154e5bc794b"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "5db94c7c58d1ebe155f5207fa61f7b88a0cd87e4eddc005853e3a92d171e1d21",
                "5c2473eb959c7fe6bd0ccc41ab6382fdfdf501d6c695ee31a1133a6c817f1945"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "2cfb933420b4f8d6c9e3d87c3d60229eb1a1f1382b76820ec787470557aee3f6",
                "0b801d7acc5f83349af33ff4568ada0c20bd3aaab1b0424a2d1eefa7dcd3558f"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "0c39a752eb0e7c1fa9b5fda6a12a75211634226acf179ef36982237c1caeed92",
                "aa5a7e16544a47c77e17bea42d6926e5dbc25cd1d372cd59a7880ab92ff5931a"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "8c1ab29aeeb180ab7fca18aecf7a566ed9c0e5b9e2c9d9ecf760fe438e932f47",
                "b35107065b7cf2c1151780c210ceaa2efb5f108602ef9d5ecf017fc3f055a249"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "70727fdfe154c2408878adf9f3d2086af257dbf26ea1ecbdaead5a5373dfc026",
                "7d2bf32ff224bf1deb27db6d07efe49db79529c3a39e64dbda1dabf4db7dd406"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "214b212f24b19a1dfd6b44c63fbaac1e76f0d289b86398cd35157ebc9365d968",
                "7a8cd36956c3f1cbdc829d1a8f961ae1a4f7f4b02cb49718903f670f38a42775"},
        {"release/testkit-artifacts.lock",
                "70dc35b5369ba7a7df5c60b7a56d48daece2cd7c8e0ff96acd5279e5a09c1db7",
                "a7e3db6a0ca5f4ccb667540bf803ae47cce696c17b28753194c59b6757445936"},
        {"release/worldline.properties",
                "709833ec765b2a366c0c7a165975f3acd63068359fdbcb1bb1f94d519d008db2",
                "add62c0ef31576c803e24ea5d2ca5fcfa9ae794745c9a8ab39254a1fdba5de46"},
        {"smokes/m622-pathfinding-matrix/MAP.md",
                "5ff95744a38fe2f6ff07f6926dccd4fe4df0b94c9fecfbd168a6d5a82b1a9760",
                "e0abb1e42333fba2d4a7228ee677789548793463f36e71c759786a37c45c55bd"},
        {"smokes/m622-pathfinding-matrix/smoke.properties",
                "c96864b6b2268d2f6165617827e9e877a21b47f72e640ea0d86d3f3c2fa76725",
                "37151e3b3534f2ed85bec1557fbaee0a73aad30c3a2333d1c1f6bae453248921"},
        {"smokes/m622-pathfinding-matrix/src/worldline/smoke/pathfindingmatrixb173/PathfindingMatrixBackend.java",
                "ecf55fbefa7d0de0da54fc309a1c39143c632cff02c02c6836f2eed759d96288",
                "555cb005099963f9110485f5b5477a21cf5ce6f12f111f763e2cd46a97473097"},
        {"smokes/m622-pathfinding-matrix/src/worldline/smoke/pathfindingmatrixb173/PathfindingMatrixSmoke.java",
                "eac9e030d33713041dee3c47f55f0974c16a0ca1f09d0d5840a72c0d9dfa74f0",
                "b2247e9135d143297d15ce8241748aa92b020bfc5f249e1964795e373b2ee6f3"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "913a5a76b8c914d6853b18185528b526513bd1362d7a75670813f49c5963af01",
                "796b10e2bda5503e945d54e33db35a08ba966cd62a0c6be634d76c719b48e9ff"},
        {"tools/harness/WeatherAtlasDocumentationSuccessor.java",
                "aa1c616155bd80f2edb722bd1470d7804610fccf1e4b09f7ed65c03708fdfab5",
                "9b694de3de967419c6cd6a5acedeed4b7d2fbf1ee7eed8279df05a08b74c51e9"}
    };

    private MobAiAtlasTrainSourceSuccessor() { }

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
                    "mob-AI Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mob-AI Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mob-AI Atlas train successor allowlist drifted");
        MobAiAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
