/** Exact reviewed documentation refresh after integrating qualified entity and Atlas evidence. */
final class EntityAtlasDocumentationSuccessor {
    private static final String[][] SOURCES = {
        {"README.md",
                "cb8406dac33ce465749cc6ddf2a7a670d80ddd92b2015703881f0dd4bb656c12",
                "9772cf5be33b5916e1d8f9b9757471434a363dd11f2f4ae2105a5ccfd03e6f20"},
        {"docs/generated/MILESTONES.md",
                "98dfc6d51af18b5fe677fe8693a5df99e3dc2e617f976de93a2e5b06d00d0ae0",
                "6d10f6dd0c6f68300a59b97180d69cfb9f33706b136bca0065c492fc2a4489ed"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "e24c28e004552e666d5e3aa3b89973517d16ea56aa725bf1ee067893406515ac",
                "1578a86c8134ba778e8315aeb8bfd674ecac1e03675283936e61ec79aecb3cf1"},
        {"tools/harness/TrainSourceSuccessors.java",
                "f4e599158e15c363168e2297115210c7aa03f061fd61c448e1e631761c85f45c",
                "dba99bea338dbb9e40eb71b168042086493355694523cf30728ff289af0dd2d2"}
    };
    private EntityAtlasDocumentationSuccessor() { }
    static boolean carries(String path, String prior, String current) {
        for (String[] source : SOURCES) {
            if (path.equals(source[0]) && prior.equals(source[1]) && current.equals(source[2])) return true;
        }
        return false;
    }
    static void selfTest() {
        for (String[] source : SOURCES) {
            if (!carries(source[0], source[1], source[2]) || carries(source[0], source[1], "unreviewed")) {
                throw new IllegalStateException("entity Atlas documentation source drift: " + source[0]);
            }
        }
        if (carries("unreviewed", "old", "new")) throw new IllegalStateException("source scope drift");
    }
}
