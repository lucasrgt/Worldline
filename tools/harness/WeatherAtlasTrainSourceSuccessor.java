/** Exact train successors introduced by the reviewed weather Atlas integration. */
final class WeatherAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/SEMANTICS_CYCLE.md",
                "03303c01c7531860339dc43acfbc8120c88fa9bfbab04d924744fc848fb0ad00",
                "79d2a5adf65a59f4005b3f572accd5321b406d22e248f2f97806883789f82707"},
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "7824c79962893e54dd033b35ef53d3939c9d0e3dcd1dce44a8d8e95c6582938d"},
        {"docs/generated/STATUS.md",
                "9ec1038f2d8c2e6716a990675a4b1b75199e34d228d00cfe336387085c08bb9b",
                "012cf5899fbbc4d110e66b228a920f22dff9d2082711cbbbcfeefb16dc0626a8"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "b2861b9800e254b7af22bdef7197126222cc944b6ed27c2491b61b50684486f0",
                "e0f11397a8014ce8b5b33996ab75a3e3d1c942364b5448ea95b3aa1073b52eaf"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "6ef96380b66f4ace90c41cb3742f556d59f8fc49fe7a29c2b2f5b120fb873ec0",
                "5db94c7c58d1ebe155f5207fa61f7b88a0cd87e4eddc005853e3a92d171e1d21"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "96ace55225d9833e766c725bf960f0f91415e8a078f4926f9e4581aa08940d24",
                "2cfb933420b4f8d6c9e3d87c3d60229eb1a1f1382b76820ec787470557aee3f6"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "b4e6d0afbd590d69877bcf57c1fcff3c61129fd33f2789d6b089aaafb3b8ef2c",
                "0c39a752eb0e7c1fa9b5fda6a12a75211634226acf179ef36982237c1caeed92"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "41a25139954ef37a6aa3f17f4f5e75e234cbfd15fa7ef0b94519144bf011ce25",
                "8c1ab29aeeb180ab7fca18aecf7a566ed9c0e5b9e2c9d9ecf760fe438e932f47"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "8addcee8c18593918f02819e82f9c3469c51c1ec5bfdc2118794ce1d608a65fc",
                "70727fdfe154c2408878adf9f3d2086af257dbf26ea1ecbdaead5a5373dfc026"},
        {"release/testkit-artifacts.lock",
                "72c45ddad253f4586efda36b5fda8463e37184922afa495abd0f63aff1909519",
                "70dc35b5369ba7a7df5c60b7a56d48daece2cd7c8e0ff96acd5279e5a09c1db7"},
        {"release/worldline.properties",
                "f73263e83b68c6e974d5ea6a602d9c2f6fc5aff9e5d8e296e05cec0e95f51c6e",
                "709833ec765b2a366c0c7a165975f3acd63068359fdbcb1bb1f94d519d008db2"},
        {"smokes/m640-snow-accumulation/smoke.properties",
                "743340446a7e928d9213e64a456305c7918da9d1861c2b5678774d943a38fd5e",
                "5047fcd003e6abc877eab8831ddd7366132846125eb98ebcc33b11ac5260db19"},
        {"smokes/m655-rain-stop-event/smoke.properties",
                "4d31347dc1ef893247eed9da6162beffcfa32f1269661bfa3efe1ed799458a29",
                "1409c09ac7caec5ff57ee42b5127eb58a7311ec5c9022fa19ec855f39547459a"},
        {"smokes/m659-powered-creeper/smoke.properties",
                "93db37097fcbe88c3a833d0bbf52c6d558eb91db4f9bbd978ab1ade9b05e2ea0",
                "7546c0e859b489b6bd8997b55addc764e03c5dece12e8718fe7e7380887cd2e3"},
        {"smokes/m663-snow-layer-nonstacking/smoke.properties",
                "9c7410d7888d773c029421a277c51593f038e2b38de12829a49a41d76f60f85d",
                "f7188750cb14ea9e1cd9bc7a4c46643be2158754af11f3fbd7c9ec7bff9a4783"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "447efbf7c5139814fedb162d501a953b85772c641b278ef7e4c4a29bc49467b9",
                "511a6c4da084f61d79b9d351f748660509ac3889132be24ecc00f98e1ece439b"},
        {"tools/harness/LightingCoverageTrainSourceSuccessor.java",
                "e24187a028cf5bef67b01b6dc3232a4bf9350c2104581c7b0eb320a76cba08a9",
                "7c459927c5fbad42e50bcdc5d0b36c86e6a980f4814b9a43d4afe8bb1557d3d7"}
    };

    private WeatherAtlasTrainSourceSuccessor() { }

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
                    "weather Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "weather Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "weather Atlas train successor allowlist drifted");
        WeatherAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
