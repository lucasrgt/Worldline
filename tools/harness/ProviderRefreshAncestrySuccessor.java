/** Exact source successors for provider refresh validation through sealed train ancestry. */
final class ProviderRefreshAncestrySuccessor {
    private static final String[][] SOURCES = {
        {"tools/harness/ProviderDiscoveryPinCheck.java",
                "6615b732549092f86d38928c518a83da6403b6cc04f88ff3e1643d9bc1d6fe6d",
                "027442d897705c6062c5693973c0981ad15f52c0c08c6b4ec9fc436a1f3a4889"},
        {"tools/harness/TrainSourceSuccessors.java",
                "0bd43f07435f7440a0414a9c3379d6c95c312a167c4c17dad92d78060a550fc6",
                "d2152909fde1463d7cd550ddc6dc22cb40694749b26f50182bb5e498c7e8accd"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "08917360058a3b0cea98a4186214721e1ebc3e9155e8755e8aadbd6a38a4fa54",
                "e0c7b2e6f75d514ab02ba0a6dfcd48acefdeae5f02922c38c5303c21d4cbb8ed"},
    };
    private ProviderRefreshAncestrySuccessor() { }
    static void selfTest() {
        for (String[] source : SOURCES) {
            if (!carries(source[0], source[1], source[2])
                    || carries(source[0], "unreviewed", source[2])
                    || carries(source[0], source[1], "unreviewed")) {
                throw new IllegalStateException("provider refresh ancestry drift: " + source[0]);
            }
        }
        if (carries("unreviewed", "old", "new")) {
            throw new IllegalStateException("provider refresh ancestry scope drift");
        }
    }
    static boolean carries(String path, String prior, String current) {
        for (String[] source : SOURCES) {
            if (path.equals(source[0]) && prior.equals(source[1])
                    && current.equals(source[2])) return true;
        }
        return false;
    }
}
