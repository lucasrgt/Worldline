/** Exact reviewed successors for Aero overlay semantics and Atlas coverage. */
final class AeroAtlasSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "a5c33c72d26cdf7272a43831342110f15df54782265f24859655ba95da7d4635",
                "4d646f9bef864a733259f39ae8ef25e04720065ece5b6c6c8b45f32e73b48e6d"},
        {"release/worldline.properties",
                "6ec2b4e6ee7b493f52f71c1b2107cd8316364d931898c5093ce4fe261978f893",
                "46d8334401a3c07e2e5b5923abd255c08000b397db966659ce6c3a7549ac217b"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "505f6015ef6c369997d0e2c7ca0f4a1a62f51e701bc5b39647648368a63917cb",
                "59bdebe18e849d03477f33a9f22ea54fc2d76b4913b7757fa610ba47b1a20548"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "25a4202032a70c5cecb3b3dd4d8157ef28e0fb3ca9dc31226f14f7e5d685f362",
                "a54d5115406065e1676d059edfe292924d8fcc25e09f5877b6766be9e268308f"},
        {"docs/SEMANTICS_CYCLE.md",
                "f5d301da386825085b06bf5b60c99fc7cdf2100e5f4b7812c02996e6dc3d66b0",
                "936a1c25b80a30e5591a4104adbfffa556a5721b0c4810a3fde96de6f5caee5a"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "345e03c9b0a75a5a31f92868d0b6795cb33c8c3a935b38663b60b130521fdcc4",
                "8d7a8ff102cc4bf16d4f2137403fd594fab318c466a7c9ed556fb69f18f20a44"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "ea02bf7bc3c4c1769b735b93e927c96df68ac6a029e0c8ac448c4013c2bd727a",
                "83655113d59930897ec18b464ade31f4f91dc85d6a8c5706e548d020b6748fc4"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "85e6e60f3ab9aa97d4f93ce64f0159fdbc61b98d0efc35bc0643ce00cc62a90e",
                "dd60964356c4eca715698b2ca2156bea688899cec658885ef44a5319f5318dda"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "1225d21d67858d78c06f1679465a91deaeaeb0c5e3da2af3b7b276f36d5020d8",
                "b87cded252f1a088c31c202eb7ad94edf497853e8b017873c2c809c010d0a78a"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "b4fc61babb89dbd6ad4ec2bea44b8ada2e84ca02b3c6e5eed231720754edf686",
                "a30f52490c74116b3c671b036ee161c443e0a2f84519ab2fc9b4c642c688b243"},
        {"modules/semantics/src/test/java/worldline/semantics/AdapterManifestTest.java",
                "e1c1ca55505c44de062c8f84a32ca4518d78f03004525cbeaf00816c266b435f",
                "01488a8f697b9c03f785a55d7498309a549c1d304649c1911cb73475d630e1ef"},
        {"tools/harness/StationApiDocumentationSuccessor.java",
                "15359106951f1ff2dd969a29698a403aef5b0780404ab27e89f37ddb4fcb7f78",
                "e5df2f627cf194d4366559b282b5c33236da529315072bf2ccb1f5f86fda89b6"}
    };

    private AeroAtlasSuccessor() { }

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
                    "Aero Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "Aero Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "Aero Atlas successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
