/** Exact reviewed successors for the complete hostile behavior matrix. */
final class HostileBehaviorMatrixSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "290ed80ff03523707b255d09f40a14a5a94ef3f4de91da0bd73b546f43311508",
                "102d98b4b92cd22bffc45b56bea6ce6bd6884bddfa74c5aceb3ebe0bc8d0a2b2"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "be90ecf433f555173168fe62212da0cbee77940ccb7a333b4145f1207fc7a890",
                "4a271d2aa884cc137d52e1388bbe034fd87002bda38be524c5a9f5e6f76fd8b2"},
        {"docs/ATLAS.md",
                "eb6979180bf3e3cd054bcee3120fb528471d338229710a889d50e2d2108e911c",
                "e892408debfea76b0326bb293cb83af7d1ae9f91cec82886f2300a14465fd30d"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "37dd93e3d2eb50df59d0012861f55b8555f599e50d89f9f2466c4fe4353bfc5a",
                "7dc3155ace9b8019e277a5c2239a0684018d36c87f8439aa0bdb1428d839541e"},
        {"docs/TESTKIT.md",
                "61bcc7fdf9cd5fd88229497aacd5f5f8b74953f8f16cd787b9c0d30f126ddab8",
                "767dd805a5f09a538071c63d43d3a0762e6b669c1af5a709c7329bbf592d84e0"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "f30b4818eb558e932bd8df61eb28e0c6843740424aef6ad2f38742f336d7183d",
                "1dd03c06ea2bfaf8bf875c4a414a063415c1eaabacf0f2555007720095911a94"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "69a515df8c897ca027415ebbdc496deb9fb6d62b1240a6ff4b82c807fca961b4",
                "e04f8e0a8264a03718ab8507e5e38a80f79ebf8ca7a254a815a55edadea52255"},
        {"release/testkit-artifacts.lock",
                "d7588e3aa6c69c1f1700861c59bace3de0152fda14f0b44fd2cc5add9837d384",
                "24d9376916cc918d147574d393d121d58494c86096a31faa2eabfbeb892f82fe"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "4939c2f7fa3e3930ce85fb52c1d679e91c91a79c26c5a2e2487878a1220dd8de",
                "df722fd8f8c0fb7fbbb221183dcfaacf273af6c566b29ce0d9cba36ee472106f"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "aa96c81ba5c9e2d53628f54a05bdc16cc7a061001bb3cf88f65ad3f958ce7511",
                "26f34ac8dbf78f692cf9448d2b05f6b7e5d1141c9b68098cec1d165ffd5eb3fa"}
    };

    private HostileBehaviorMatrixSuccessor() { }

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
                    "hostile behavior matrix successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "hostile behavior matrix successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "hostile behavior matrix successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
