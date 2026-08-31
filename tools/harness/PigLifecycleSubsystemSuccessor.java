/** Exact reviewed successors for the complete pig lifecycle subsystem. */
final class PigLifecycleSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "102d98b4b92cd22bffc45b56bea6ce6bd6884bddfa74c5aceb3ebe0bc8d0a2b2",
                "b1d2a2f10a419664b64b7580f846f3a17a3b2662890ef375a25ca965956bc674"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "4a271d2aa884cc137d52e1388bbe034fd87002bda38be524c5a9f5e6f76fd8b2",
                "ef9bf97a3f4dcf7a53d92c8f69156d23e9caa3ebb2ab718f97b73d48a0ec3f23"},
        {"docs/ATLAS.md",
                "e892408debfea76b0326bb293cb83af7d1ae9f91cec82886f2300a14465fd30d",
                "93e94841c73ede0667e8bd011cf6e189317c84f52fa150ca1d0b0b4c7e4b99ed"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "7dc3155ace9b8019e277a5c2239a0684018d36c87f8439aa0bdb1428d839541e",
                "c9c3dc5dc0167d5db5b144ab8aaa70d6c59968a1ed96706a5e9031848c18cae5"},
        {"docs/TESTKIT.md",
                "767dd805a5f09a538071c63d43d3a0762e6b669c1af5a709c7329bbf592d84e0",
                "233206a2dd7d3d4d15dbec1a34ec62af5c16a6e4a2d150283fbbd70d6b06537d"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "1dd03c06ea2bfaf8bf875c4a414a063415c1eaabacf0f2555007720095911a94",
                "c9e3756c416470a23311e3913b37025ca961e8ec806b8a5dbe527a81803541ee"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "e04f8e0a8264a03718ab8507e5e38a80f79ebf8ca7a254a815a55edadea52255",
                "16e285747241cbe189d36445327f77bec751510576aa839b36a88d24fde32f5c"},
        {"release/testkit-artifacts.lock",
                "75829a44bda0a2a01abff67824fbc99ddbbde2420d9d178798af6d82e25424f1",
                "ab9bcdbee1f6f519e3f20162600991b57a18f749d7df33cd488a4cb10872beba"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "947d66ff5299a13c4cdf57c4a7b84ae677dc4eb4770f811c1e125f257da1a233",
                "a2ac76e433d6ff6808583b718d73db9cafe618051f4072df33ef3766300fbea3"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "460fd0cd6eb5b571a10a65f3f452200684a07c9566a588dcdccf6da65da008b4",
                "4c845306ee1c2ee1fd01be017464798589c401efd27d565d6c73bc9a7835e6ca"}
    };

    private PigLifecycleSubsystemSuccessor() { }

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
                    "pig lifecycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "pig lifecycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "pig lifecycle successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
