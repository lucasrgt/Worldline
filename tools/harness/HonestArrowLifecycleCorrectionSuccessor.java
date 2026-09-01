/** Exact reviewed successors for removing the unproven arrow projectile lifecycle claim. */
final class HonestArrowLifecycleCorrectionSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "fd866b26a86af525eaaf57908896be6128085c360bc69f4c882080ee8c1a7048",
                "3885952ccd3547366672f36326f9fc9c4ea642a91b7ec70e6c45a5755f7081cb"},
        {"behavior/functional-census/families.tsv",
                "921fa11c0fe91aeb2e8dc975b3e0ad2213ab3961b807a74c9cab03aedccafe28",
                "3a8174dccdde65ee2f9e9c7da0589b5666b1d07b5ed249b2f7395f16f61d4a04"},
        {"docs/ATLAS.md",
                "6713a247318781820010e07831fd686c42b8da150ef21f1f37f1d86ca1e49172",
                "c24fdf3a444621371160539f0dfc28fa5ae327e8ea19ffaf23db3b7267c7cb3e"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "a0d633279a49fcc55bb5e6954794d7255897b0a330f4c7739400380dc307216d",
                "925a2e97defc3c44b0298ecb66bde004fb4a61db2b26fb5ca2f733ded5a3d4f0"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "ab544cecb07dca2ad0bf58247d0b693d8a358713cfddeb6530bbb196a980e2b6",
                "3dbd1d46d2264ecf7f424d6d23ac86a53f0f465b3f1f63c286eb6d0cebe440c3"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "c1a2ce059264063e9b2961c24b7023c591d70316f10d80bd5668a0857f1c89b4",
                "2e1b347dae1e196ab987d03cb75fe6c5fe2f51467397899a910e1f6832184417"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "be3511c41e281930283919ccf852c5146fd93ccdfd60741164859eec9690f3e1",
                "f20897935cdf8a0f98739825de706aad0ae16ab84a9aebd666feeab51a2731f7"}
    };

    private HonestArrowLifecycleCorrectionSuccessor() { }

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
                    "honest arrow correction successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "honest arrow correction successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "honest arrow correction successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
