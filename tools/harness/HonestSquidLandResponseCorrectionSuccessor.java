/** Exact reviewed successors for removing the unproven squid land-response claim. */
final class HonestSquidLandResponseCorrectionSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "3885952ccd3547366672f36326f9fc9c4ea642a91b7ec70e6c45a5755f7081cb",
                "290ed80ff03523707b255d09f40a14a5a94ef3f4de91da0bd73b546f43311508"},
        {"behavior/functional-census/families.tsv",
                "3a8174dccdde65ee2f9e9c7da0589b5666b1d07b5ed249b2f7395f16f61d4a04",
                "51f9a2c6beb4720f58a890bfcc661507fb8ab290ddea8f99f6889523a44b4b11"},
        {"docs/ATLAS.md",
                "c24fdf3a444621371160539f0dfc28fa5ae327e8ea19ffaf23db3b7267c7cb3e",
                "eb6979180bf3e3cd054bcee3120fb528471d338229710a889d50e2d2108e911c"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "925a2e97defc3c44b0298ecb66bde004fb4a61db2b26fb5ca2f733ded5a3d4f0",
                "37dd93e3d2eb50df59d0012861f55b8555f599e50d89f9f2466c4fe4353bfc5a"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "3dbd1d46d2264ecf7f424d6d23ac86a53f0f465b3f1f63c286eb6d0cebe440c3",
                "f30b4818eb558e932bd8df61eb28e0c6843740424aef6ad2f38742f336d7183d"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "2e1b347dae1e196ab987d03cb75fe6c5fe2f51467397899a910e1f6832184417",
                "4939c2f7fa3e3930ce85fb52c1d679e91c91a79c26c5a2e2487878a1220dd8de"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "f20897935cdf8a0f98739825de706aad0ae16ab84a9aebd666feeab51a2731f7",
                "aa96c81ba5c9e2d53628f54a05bdc16cc7a061001bb3cf88f65ad3f958ce7511"}
    };

    private HonestSquidLandResponseCorrectionSuccessor() { }

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
                    "honest squid correction successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "honest squid correction successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "honest squid correction successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
