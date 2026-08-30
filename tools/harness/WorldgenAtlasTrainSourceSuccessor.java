/** Exact reviewed successors for the public worldgen TestKit and Atlas boundary. */
final class WorldgenAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/SEMANTICS_CYCLE.md",
                "7483c41b2e666959dc888569643bf62ef96236abc52bccec3c7a3dcb2760e7ba",
                "ba340bf5101f7d43c813b2b015416e4ae9cccab6f4b5d0efae6f74a4500e49a0"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "8bf6b72b6a207739eef7a2455154accdcaefda7f8931d273266fa2844a3fcf1a",
                "d5f8b7b71becb82c97afc4a4481e61c77d7e99a6edd2ad7d6e12a618dcbfd203"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "b211867b9cfa0ab54797fbf83dfb8a789c97b4acde1fe17c998241f367c6dd9c",
                "1daaff45e6f5b83e444eeec961efeb1315d41fc8dd5c970d2ade07d2a6289de0"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "94b7fdec9d468f77ad5eedfcc081250b74745caf1eb504ffdd188ccb0e5d2814",
                "27fbda1746d5377f8ba2fa00eb777b55123ca4c534cfe205b6b9762e3a408c8d"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "f075cb76d1f7ab52c2c499cf68aa16ab2c3759a3ad7dd4c1fc970b0e27b408e6",
                "0da2f1e6996fa500e730ad72d2879c407d2b3efb995d78256a2073a162532f08"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "db32e4f5e4f9c90e9d582fd232cd45679b8bc642b17533ae58914dbe772bac11",
                "92783af8bc364c726f923a14c4bd5df490f97aad399f70fe49b022ea788ba5ea"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "286ed7e8f26f4468e482c4bd96375afd2eba2b14b38307854ab337d12b1d7dd4",
                "0911593669484c32158d72bff34472fd10ee182ceab38c4c2948e74ffa7035d0"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "7a8cd36956c3f1cbdc829d1a8f961ae1a4f7f4b02cb49718903f670f38a42775",
                "2a52826ca6be8d830a87757a9b47c7f6e5da302d8f098a87d93f1136a0b4e6b8"},
        {"smokes/m621-save-worldgen-set/smoke.properties",
                "262ccbd9f09d679a8d51a6db2cb9e42368312b3af09c428a3da5b66941fd56f9",
                "08f661b2e42a4b40e1feb49415a187681b02e3e308ead510da66c27733dbb602"},
        {"smokes/m621-save-worldgen-set/src/worldline/smoke/"
                + "saveworldgensetb173/WorldgenCensus.java",
                "0c071e6bafa628b57d7ed62045ed7f002efe822b14b2c0a1c57819aa0136a87f",
                "d28207d9067b3c8fbca2e3e8df57e4d5f1e69a09387af4c09f0787671ab8e8a5"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "e52ec8b121ecbb17d9a8d88490b032d31f4fbaab7628d87ceaae73bf02f6d7e6",
                "e630a1086619c89152c1cdc7ee3f02f0eab2d6a4f87d55db4c4c1351e0212526"},
        {"tools/harness/DimensionArtifactSuccessor.java",
                "5c25deb363fe7c914dfe4bce7f47e558eea16b3a839ac2e714eece6984391813",
                "f7be2560b701aeb473cc7de1447b61c37612276e132565e936680b85d44b3305"}
    };

    private WorldgenAtlasTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS) {
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) {
                return true;
            }
        }
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "worldgen Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "worldgen Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "worldgen Atlas successor allowlist drifted");
        WorldgenAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
