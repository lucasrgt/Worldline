/** Exact reviewed successors for generated StationAPI Atlas documentation. */
final class StationApiDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/STATUS.md",
                "e5fde01e122abd2c63cec8ad17e13fa6361144d270a5cd481c1d0b0c4f8b073d",
                "2b5606191532e88aa2ed21afd37319e401b52a75220bfa83566bf4704fa4c9a6"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "9c04662e454d0c6047dfdcf48620561e7ad2ade769da9ebc351a655de25d090c",
                "b4fc61babb89dbd6ad4ec2bea44b8ada2e84ca02b3c6e5eed231720754edf686"},
        {"tools/harness/StationApiAtlasSuccessor.java",
                "bb545392d231715417c0c298d9d5838ad80d13f8552ac81ba47e760253513ba4",
                "eb11d4dd8fa3f9134afc6945f14b5304d73a65b0924daa0b65ee88f990cdf420"}
    };

    private StationApiDocumentationSuccessor() { }

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
                    "StationAPI documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "StationAPI documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "StationAPI documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
