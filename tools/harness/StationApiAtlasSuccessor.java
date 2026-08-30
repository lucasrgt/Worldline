/** Exact reviewed successors for StationAPI driver semantics and Atlas coverage. */
final class StationApiAtlasSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "939bf24c13c2d07c6cebde15e7c11cdfafec64a0f431096d8292548e7a4aa1bb",
                "a5c33c72d26cdf7272a43831342110f15df54782265f24859655ba95da7d4635"},
        {"release/worldline.properties",
                "2693f75d6bbf1fcf17b32585e20feeb33e38bb7e43ed4def95a67a0613caff7c",
                "6ec2b4e6ee7b493f52f71c1b2107cd8316364d931898c5093ce4fe261978f893"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "0f6f75859aee4fce8be0e439c8ec8acb67b42796b41bb28b260006cdfd4fe346",
                "505f6015ef6c369997d0e2c7ca0f4a1a62f51e701bc5b39647648368a63917cb"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "5b03363d01c1cbed97c5e41b8df18b09ff0ba54c8745bc2a6d3784ef311eaf9c",
                "25a4202032a70c5cecb3b3dd4d8157ef28e0fb3ca9dc31226f14f7e5d685f362"},
        {"docs/SEMANTICS_CYCLE.md",
                "0ed34dfcf87bc39985de993d32379c293d5620de521f44bf2821dc1d556c07b3",
                "f5d301da386825085b06bf5b60c99fc7cdf2100e5f4b7812c02996e6dc3d66b0"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "7d4ae02ef2ace50a5af93bc102d4f7b132cbb1b6bf2b5d2bf1e9c6c6fa9bdb36",
                "345e03c9b0a75a5a31f92868d0b6795cb33c8c3a935b38663b60b130521fdcc4"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "05e8dfc058966dccf7ad514488e027e44e3aaedf890202c62332b54c337788f0",
                "ea02bf7bc3c4c1769b735b93e927c96df68ac6a029e0c8ac448c4013c2bd727a"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "353dcccdac94972ff0678500810f7fb3bf18d313ad59ed784f47ca4f1849bb7a",
                "85e6e60f3ab9aa97d4f93ce64f0159fdbc61b98d0efc35bc0643ce00cc62a90e"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "e51334a18011a876f0f56e47ce6712a32abab7ef8f9285caadabc6dd61924029",
                "1225d21d67858d78c06f1679465a91deaeaeb0c5e3da2af3b7b276f36d5020d8"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "713542164f60ffc5fdfdfec3f7a611803f4be103474ac47b5f00c3f1eb93600c",
                "9c04662e454d0c6047dfdcf48620561e7ad2ade769da9ebc351a655de25d090c"},
        {"tools/harness/MappingDocumentationSuccessor.java",
                "36cf4e1939426528a48d63298b6ba69adb918f9da06df6f2aa59d7586373b9c5",
                "63d5d7e9eeb24f31545e2f77c3fd79b6840d2a00a8fa38bb9ff83c56701a73c1"}
    };

    private StationApiAtlasSuccessor() { }

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
                    "StationAPI Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "StationAPI Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "StationAPI Atlas successor allowlist drifted");
        StationApiDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
