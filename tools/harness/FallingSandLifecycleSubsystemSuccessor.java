/** Exact reviewed successors for the complete public falling-sand lifecycle subsystem. */
final class FallingSandLifecycleSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "cd461359548560cf3c94b85e0ef3d13dea5b34d44ab910bceecbd880bb4f6cbd",
                "56faa523f3c594f3f3065356a4d7a9adbd6b7288b5cb3761e1b68eeb7b6f860a"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "e6556022fe6ae9e7a7954c723d9c23adf835b57eaf2ee11ae87a186208f417cd",
                "f47dcdfb758a6900a8f6aaa93ba560e472a11831999266216c69d1ffba0bba3f"},
        {"docs/ATLAS.md",
                "ba90445bb51b14afbccd821657dff36de28ed12e4c303d5427b4725a82056e0a",
                "c62d81c1b22672581d185e63528066f2e1c8898a7fc0c10f15e96f493f496b83"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "bbccce6d41a6209b2523b18e7f406fce410c3c57a3b828ebf6ee91e05790f701",
                "aedbc200f5e3521a6629740450b333fa8b3e6a9048e456b1e2b2653e02f6c5af"},
        {"docs/TESTKIT.md",
                "a73aadd7e9aec0b2be70f49daf6101d86158a94496a90e72fb66b6f1173df44b",
                "203fd41de7c037a48e4cdfc4b642d73056c5246bdd824296da690a26d1500db1"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "9504e059651ca3e72b8a50bf9537d3b03e615b51eb133985088c9e00c86bcd7d",
                "09448beec934844a111ba587a89b9b27f77a9023146f799112eecdd3f66c6338"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "f7de2a8efe1f3b899a802a98483a46228788199b4e87a9224a5068fa85927dc8",
                "cf95cddad7d7371391b6ed5726d158f37ae986f1a552495bc7dd6275dfb4e019"},
        {"release/testkit-artifacts.lock",
                "7fd18904c2f51a75ef4dbd349f8724f0ba58d8e90195b623b838c9a25d3f16bc",
                "bb6215035808ba5c496eaaead021a5bb7242a906552ea7130ccd73576267e6c9"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "3079633fc6069f9dec18b09026ce6da334d4364a0cf31ad7282f659da5249646",
                "f617c9d4ba3f740c5034c2d7409a516c0d6205a2efd4e7d7f5c809fd8ab7e067"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "da62fd8123880c3d8e25a18610723cb7e4896ab8ce248f3b437e7e0a13b0ae2b",
                "cab6d6f5ba8879b068c4bbb53eb2579ef1e60bb7ff9d4c7666e0210809532c3e"}
    };

    private FallingSandLifecycleSubsystemSuccessor() { }

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
                    "falling sand lifecycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "falling sand lifecycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "falling sand lifecycle successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
