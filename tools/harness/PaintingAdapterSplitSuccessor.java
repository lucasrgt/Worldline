/** Exact reviewed successors for the painting adapter decomposition receipt. */
final class PaintingAdapterSplitSuccessor {
    private static final String[][] SUCCESSORS = {
        {"smokes/adapter-split.lock",
                "261a72a7b286de525589f87e121a48e731f001e9309c1d6ccbaef8daefbd7b7e",
                "3005cd6d865714000b6334cd7fb563990c5cb80dcc32b6e4d0c9b25b3c577eac"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "f6158bfe5f3d6ec4b75f92d015783e8773890ae57f18886632c3b6cab19a710e",
                "6352a92b3afc7b1e18bf6d41bbecf187ec86dd587fe7660993a4108c2227596f"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "83031d6551430a6ba4eeebc8aa800fcaa1c4f490313b468b7ec44885f63999d2",
                "a0f298328afd9bee544775b3a8d1b2cfcb7c1a9b3f5decfe0490cc84f3d9a743"}
    };

    private PaintingAdapterSplitSuccessor() { }

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
                    "painting adapter successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "painting adapter successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "painting adapter successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
