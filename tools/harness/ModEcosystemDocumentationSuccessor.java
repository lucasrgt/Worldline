/** Exact reviewed successors for generated mod ecosystem Atlas documentation. */
final class ModEcosystemDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/STATUS.md",
                "c61da676177f869588feac86b018440e9d9ca37c3bcc8fe12629d4bd46ffa97f",
                "ecda9c340434300086f5da7079b5bc43f0ec01bf3618bd7db12b89ebe11f6c5a"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "fde54406e4a019ad5ae19a1ace4dbaa82dd84b2f525e8bba2cc0649f6610b702",
                "066b4012c8c04c20b0b8044929cfcc1e524423f8c20e9d3f54f7dc9b890d614f"},
        {"tools/harness/ModEcosystemAtlasSuccessor.java",
                "09a8e83ab27b574ccd8ca18af905ae421ef49eeb4a22ea965ed55ee3392e9637",
                "bdf614237cb8cc22b3527a538d538e274fa3a54b9ca05a8c66acb53401a6b311"}
    };

    private ModEcosystemDocumentationSuccessor() { }

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
                    "mod ecosystem documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mod ecosystem documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mod ecosystem documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
