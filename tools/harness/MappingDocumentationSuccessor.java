/** Exact reviewed successors for the reconciled mapping milestone catalog. */
final class MappingDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "993cb320c18df60d93f1084f4e37e5b9fd46db8dd8f1ed06b6ec870253bf5429",
                "5f2960f12faa2cbfbb3f1ecf61d98274b9c18b229e067137793f256792206337"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "b28275545344ea9a7991ccd989be2e37da91e65d4d8d9d8e0c266afae366d069",
                "713542164f60ffc5fdfdfec3f7a611803f4be103474ac47b5f00c3f1eb93600c"},
        {"tools/harness/MappingReleaseSuccessor.java",
                "d50809cdacbc32b06c76e6855d6ca65a3c01f4c798bcfe07672a6912ea7770cd",
                "cc7ea826c260b956284af29848ab5e7a915b7f77992e8a480cd2a870d6755a35"}
    };

    private MappingDocumentationSuccessor() { }

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
                    "mapping documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mapping documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mapping documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
