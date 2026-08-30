/** Exact reviewed successors for the mapping semantic release constitution. */
final class MappingReleaseSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/worldline.properties",
                "7adeba786473853c2a94392b4dc34042bef91b2f80ea3a8a8f32a56b997f277c",
                "2693f75d6bbf1fcf17b32585e20feeb33e38bb7e43ed4def95a67a0613caff7c"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "52df80d18aa47bdabc5be609f112e9a56b2db3e60530c7c74c0fd562a91c713e",
                "b28275545344ea9a7991ccd989be2e37da91e65d4d8d9d8e0c266afae366d069"},
        {"tools/harness/MappingAtlasSuccessor.java",
                "153890dd015d824f9e380a273b36fc4045cd1d90d813f8a1e59ac2cf7747c4a4",
                "aeb8e4a7b8ef7a8e740232a88a17537d7df1d8b2d5be3f4af7dcee775617fe25"}
    };

    private MappingReleaseSuccessor() { }

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
                    "mapping release successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mapping release successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mapping release successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
