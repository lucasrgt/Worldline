/** Exact train successors introduced by the reviewed tile-entity Atlas integration. */
final class TileEntityAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "8a8c97cab9d3ae0972250927904947bb63d848c5617e13cc3d55669334aa1ae8"},
        {"docs/generated/STATUS.md",
                "8144afdcdf3bcdca847dbe9ab6ef4b9fb3168be9438bfca08a8983f09ceae03b",
                "87f2915d19b939c829cde86622255a2596e82dd84b177c4d0fe98c421edf4416"},
        {"release/testkit-artifacts.lock",
                "7b0d1ed09822479a89f61c0554ccd14639f65aead5923dab37aa77cecbd55002",
                "abcdd8baad3ef00a4190c0abea2e2812286656554d7179cdaa25a8c084bece69"},
        {"release/worldline.properties",
                "2accc9480681e069d7a24231c778804ca52f0f40fcbc3642cb8a1cb1843753f4",
                "db47951a429d19aa2230a1e5f77ea33850e74262b7e8c5176a9237bcac0fe10c"},
        {"smokes/b173-sign-subsystem-lifecycle-cycle/smoke.properties",
                "c3a2e7dde24b7b38ff2336438c40610a7694475aa1f7921597a51dea5b787fb5",
                "2b5d174e8a5ee0be859c3831e33a5926e6a1d0093abfff7c24fec81c5620a9fa"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "ad88b64f65d4dd058c000dc7ad6f84dd3303baebdc3d1de3798445c6e1f26679",
                "1f1ccd230c9a664d1240fc8f4b1e48f763d35ecf5cf9c9a894c0bcc1e93ea0b2"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "e7e8ba938341964ff044082d85327113ea0e15428a81f0f9c5c2a7b98c5439a7",
                "d8903528386d183a98c9063d685839a301e7733b177caff536c78b98e5f47dfa"},
        {"smokes/b173-piston-subsystem-conformance-cycle/smoke.properties",
                "1ad7d3410ace3f85f2eba02b99c176a67aebe62bccdc756dfd60e2edf90d671b",
                "9bbfd9f98ab5ae43df02528c6e6f72bf852d081b2f081b1617cabd2c13f9c492"},
        {"smokes/b173-furnace-subsystem-conformance-cycle/smoke.properties",
                "fd60acf7677fe333179d7d70374268e82c411b2ed65acaa5636ad54cbb2bf812",
                "2e73acc49458ec18cd77d42ef5f200d345e93bda007fe327869bdcf348821a81"},
        {"smokes/b173-mob-spawner-subsystem-conformance-cycle/smoke.properties",
                "bae9f47ba3b772d019ab96d537a4d931b473d16f30307c8b73e9a57a2bc2b65d",
                "d196c9d6c8ed95f5bafd30eb3b79811289f56f662c19dbf3123b8f575f41f1df"},
        {"docs/SEMANTICS_CYCLE.md",
                "b1be5292e6a1ac64e2a2d6734bf69c5f1c6e8ebdcbb512af455ebd23f776a1fa",
                "98b5b93b968bebc6900ade4bdff83da00d63d5894df07e21498a3b75b377a8de"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "0081ac105bc7360b19784d39659b03f8c9a8c3da9a4c39876097d3281ab96c0d",
                "a821c7c3e56ca1d8b84766e453452307a847b54232d4dcbcb7063b51d2daeab2"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "5634b826194748b127918381bd1a649d687961c0ebd2dd901634e02576ba5bc5",
                "df2b2f617f2f354f2185623c679bb49a38bad8e14755e5b985cc862128915d41"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "e24d8db1a1186ec61609d94ef347b2cef4b32c0c9d669676ad1743bb0056da2a",
                "a921ac769d10ea303a3f29779820e5689f73b1ac39d04f164fb0e735a51c8b82"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "3c15077c8cf2b99f8c48e03bfa6fe4b826f543a3a58978007cff029a06c012c1",
                "bbc30b3f85fae24d99536c980a626b6982d5d53c242999903c3df232194d523c"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "6cc60edeb84d29979b2d11e7d1f0d9ed9eb59adb707d7dfaf247863456ef0ec2",
                "76c5a896684502eff22e952897af0ecd801ad822c6470c662acd17cbf6d52235"},
        {"tools/harness/CraftingAtlasDocumentationSuccessor.java",
                "343a8d81bddb22b8d46ee1c6c603d79200f859dd65c0df6fba346149429bd49a",
                "edc8c620794674c76744acca4bb4b025ebd5bae1be8305df46c05513638a163d"}
    };

    private TileEntityAtlasTrainSourceSuccessor() { }

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
                    "tile entity Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "tile entity Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "tile entity Atlas train successor allowlist drifted");
        TileEntityAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
