/** Exact reviewed documentation successors for the integrated milestone train. */
final class IntegratedMilestoneTrainDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"README.md",
                "e204b148af092b6db315a27102b5b5c704a0642cb37fdbb524232644b92bf5e2",
                "2507db2b23b0f395440862449243aec927a4519c1f2deaa2f0f7995c02a3e699"},
        {"docs/generated/MILESTONES.md",
                "095466834caa66218a1ca8a2c612157bb583b024166bc4f155d448824d0271aa",
                "119032dd989fc764e95f72089def2d64288e7893d2a642668c63ceae355b082d"}
    };

    private IntegratedMilestoneTrainDocumentationSuccessor() { }

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
                    "integrated train documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "integrated train documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "integrated train documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
