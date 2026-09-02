/** Exact reviewed source successors produced by final reconciliation maintenance. */
final class FinalReconciliationTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "c73d99b23c87ea3181ad817314e066381f7d5c6bcb31b32895bc1ee5832f6712",
                "c0659775af139015bb755cc16ee6d51d410f6b4273a93dfd500e9196ef65d6de"},
        {"docs/milestones/INDEX.md",
                "c77e9b2a4b2d4c8f5163c626ae1a41bc0ba4867e78de35f54ca31deb42b32c06",
                "e01d173ad4dfb8afab18d9f8a99b979209433cf8d2162cf2f362f3426d567665"},
        {"smokes/gui-workbench.lock",
                "89cd17f83bfe5a49ccfb90734f87cf8139d697f2633edae4f7f074949429e993",
                "4e343064c8fbd89813ff1af36ccd1bb1c4a9c66c3fa71dffc1d9ecdf08f310b0"},
        {"smokes/telemetry-migration.lock",
                "88b4ca3003fba6f601dd5c7b187e78846cd8b7887a77a5269c37c3c56d3233fd",
                "84081487540d97edd268993fff125ceef8fdd025aba36fd0fe3c498fce39d9de"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "76cf6e91d27ce26b13c6e14e96003f6907ca7c999c9be3aabfbdfe8d7acd8975",
                "c1cba67598c00568905d6daaab77e8631548bae35c7b218a57b80259bbd5e359"}
    };

    private FinalReconciliationTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "final reconciliation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "final reconciliation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "final reconciliation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
