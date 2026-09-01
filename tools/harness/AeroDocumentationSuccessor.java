/** Exact reviewed successors for generated Aero Atlas documentation. */
final class AeroDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/STATUS.md",
                "2b5606191532e88aa2ed21afd37319e401b52a75220bfa83566bf4704fa4c9a6",
                "c61da676177f869588feac86b018440e9d9ca37c3bcc8fe12629d4bd46ffa97f"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "a30f52490c74116b3c671b036ee161c443e0a2f84519ab2fc9b4c642c688b243",
                "ebd423ee411bfc27c0daf2bc41a2eff9bcadef7a8e2a0d84a47b81c19cc9c1b3"},
        {"tools/harness/AeroAtlasSuccessor.java",
                "0258a9c9d47fafc7b756edbb821362bbfe91152fec59695cdf86072cbeb02c1d",
                "c208913cd73acf1d7a6f4b42592d88be48efc54a4fa12c18a79f2d2400d1f3a5"}
    };

    private AeroDocumentationSuccessor() { }

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
                    "Aero documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "Aero documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "Aero documentation successor allowlist drifted");
        ModEcosystemAtlasSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
