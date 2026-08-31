/** Exact reviewed successors for the complete public primed-TNT lifecycle subsystem. */
final class TntLifecycleSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "56faa523f3c594f3f3065356a4d7a9adbd6b7288b5cb3761e1b68eeb7b6f860a",
                "fd866b26a86af525eaaf57908896be6128085c360bc69f4c882080ee8c1a7048"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "f47dcdfb758a6900a8f6aaa93ba560e472a11831999266216c69d1ffba0bba3f",
                "be90ecf433f555173168fe62212da0cbee77940ccb7a333b4145f1207fc7a890"},
        {"docs/ATLAS.md",
                "c62d81c1b22672581d185e63528066f2e1c8898a7fc0c10f15e96f493f496b83",
                "6713a247318781820010e07831fd686c42b8da150ef21f1f37f1d86ca1e49172"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "aedbc200f5e3521a6629740450b333fa8b3e6a9048e456b1e2b2653e02f6c5af",
                "a0d633279a49fcc55bb5e6954794d7255897b0a330f4c7739400380dc307216d"},
        {"docs/TESTKIT.md",
                "203fd41de7c037a48e4cdfc4b642d73056c5246bdd824296da690a26d1500db1",
                "61bcc7fdf9cd5fd88229497aacd5f5f8b74953f8f16cd787b9c0d30f126ddab8"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "09448beec934844a111ba587a89b9b27f77a9023146f799112eecdd3f66c6338",
                "ab544cecb07dca2ad0bf58247d0b693d8a358713cfddeb6530bbb196a980e2b6"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "cf95cddad7d7371391b6ed5726d158f37ae986f1a552495bc7dd6275dfb4e019",
                "69a515df8c897ca027415ebbdc496deb9fb6d62b1240a6ff4b82c807fca961b4"},
        {"release/testkit-artifacts.lock",
                "bb6215035808ba5c496eaaead021a5bb7242a906552ea7130ccd73576267e6c9",
                "d7588e3aa6c69c1f1700861c59bace3de0152fda14f0b44fd2cc5add9837d384"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "f617c9d4ba3f740c5034c2d7409a516c0d6205a2efd4e7d7f5c809fd8ab7e067",
                "c1a2ce059264063e9b2961c24b7023c591d70316f10d80bd5668a0857f1c89b4"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "cab6d6f5ba8879b068c4bbb53eb2579ef1e60bb7ff9d4c7666e0210809532c3e",
                "be3511c41e281930283919ccf852c5146fd93ccdfd60741164859eec9690f3e1"}
    };

    private TntLifecycleSubsystemSuccessor() { }

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
                    "TNT lifecycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "TNT lifecycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "TNT lifecycle successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
