/** Exact reviewed successors for dedicated-server semantics and Atlas coverage. */
final class DedicatedServerAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/SEMANTICS_CYCLE.md",
                "ba340bf5101f7d43c813b2b015416e4ae9cccab6f4b5d0efae6f74a4500e49a0",
                "17610228202d25c2d87e3eae01a6726c563b3c51de96cb188c6acaec8ff30800"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "d5f8b7b71becb82c97afc4a4481e61c77d7e99a6edd2ad7d6e12a618dcbfd203",
                "019f292b92ad1f08c2c038d1fbc02fbb8f526d20dd002ffa23e27231a9a89f64"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "1daaff45e6f5b83e444eeec961efeb1315d41fc8dd5c970d2ade07d2a6289de0",
                "30b0e422438b75cf487bd049f5fa51117a0b5daf59542806b9c45616564cc95f"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "27fbda1746d5377f8ba2fa00eb777b55123ca4c534cfe205b6b9762e3a408c8d",
                "9b0f1c685d2cf4a655e23fd7ff8ec29a1ec923f453324ea13a5e6d8a4c3ce509"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "0da2f1e6996fa500e730ad72d2879c407d2b3efb995d78256a2073a162532f08",
                "6c26dfb69a418db38df2e17406bfba7bc479057037d116948dd98f14389ee268"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "92783af8bc364c726f923a14c4bd5df490f97aad399f70fe49b022ea788ba5ea",
                "cd2a77b87f5d20cf4030eb49b0534c633cc46f11d11313da993e8b8649a8a2a6"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "0911593669484c32158d72bff34472fd10ee182ceab38c4c2948e74ffa7035d0",
                "872c718e521a261b3d8671f6132efe2b5ded8e0ddf697e6a829c5142c9bad8ee"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "b839f1ffa1bd7adb4f441c3efe96624d54faa9a79366f118ab3c14d6f61a2216",
                "d084458812d845f615917b1be3698d81c86787245a436d48d021e318bf85b16a"},
        {"tools/harness/WorldgenDataCycleAttestationSuccessor.java",
                "6d9050c372bde734a2a37fcd307e506ced50e0c089a8d46ce40b3d251e25ee1b",
                "6dbf69416bbad8283a6cd01179b7c1dc8293065e038eca0aba847f2c20d60b81"}
    };

    private DedicatedServerAtlasTrainSourceSuccessor() { }

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
                    "dedicated server Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "dedicated server Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "dedicated server Atlas successor allowlist drifted");
        DedicatedServerAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
