/** Exact reviewed successors for mapping semantics, Atlas coverage, and artifacts. */
final class MappingAtlasSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "5f2960f12faa2cbfbb3f1ecf61d98274b9c18b229e067137793f256792206337",
                "993cb320c18df60d93f1084f4e37e5b9fd46db8dd8f1ed06b6ec870253bf5429"},
        {"docs/generated/STATUS.md",
                "460645cca4edee2aba96ae1c234e84fa8f3e37f8126045fda658bc5b83031ada",
                "e5fde01e122abd2c63cec8ad17e13fa6361144d270a5cd481c1d0b0c4f8b073d"},
        {"release/testkit-artifacts.lock",
                "120612d5618446e7d16560153b43eca0a8ffae37095607bb4c2f17b5fbc889d5",
                "939bf24c13c2d07c6cebde15e7c11cdfafec64a0f431096d8292548e7a4aa1bb"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "019f292b92ad1f08c2c038d1fbc02fbb8f526d20dd002ffa23e27231a9a89f64",
                "0f6f75859aee4fce8be0e439c8ec8acb67b42796b41bb28b260006cdfd4fe346"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "30b0e422438b75cf487bd049f5fa51117a0b5daf59542806b9c45616564cc95f",
                "5b03363d01c1cbed97c5e41b8df18b09ff0ba54c8745bc2a6d3784ef311eaf9c"},
        {"docs/SEMANTICS_CYCLE.md",
                "17610228202d25c2d87e3eae01a6726c563b3c51de96cb188c6acaec8ff30800",
                "0ed34dfcf87bc39985de993d32379c293d5620de521f44bf2821dc1d556c07b3"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "9b0f1c685d2cf4a655e23fd7ff8ec29a1ec923f453324ea13a5e6d8a4c3ce509",
                "7d4ae02ef2ace50a5af93bc102d4f7b132cbb1b6bf2b5d2bf1e9c6c6fa9bdb36"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "6c26dfb69a418db38df2e17406bfba7bc479057037d116948dd98f14389ee268",
                "05e8dfc058966dccf7ad514488e027e44e3aaedf890202c62332b54c337788f0"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "cd2a77b87f5d20cf4030eb49b0534c633cc46f11d11313da993e8b8649a8a2a6",
                "353dcccdac94972ff0678500810f7fb3bf18d313ad59ed784f47ca4f1849bb7a"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "872c718e521a261b3d8671f6132efe2b5ded8e0ddf697e6a829c5142c9bad8ee",
                "e51334a18011a876f0f56e47ce6712a32abab7ef8f9285caadabc6dd61924029"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "7940135b8bcca3217205388c1f8e2869d42616875fe06edcc961ad50f0d9d762",
                "52df80d18aa47bdabc5be609f112e9a56b2db3e60530c7c74c0fd562a91c713e"},
        {"tools/harness/DedicatedServerArtifactSuccessor.java",
                "5cafa179da1519afe5af7b06f609695c9f654b9d9ecdc04fcec7f5a2bc7d86d2",
                "23289cffe7c819172e493149ab8424c472d398c92f2c100d3f82ced7cc29ab20"}
    };

    private MappingAtlasSuccessor() { }

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
                    "mapping Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mapping Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mapping Atlas successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
