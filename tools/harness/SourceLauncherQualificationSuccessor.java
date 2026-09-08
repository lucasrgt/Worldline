/** Exact repairs for source-launcher digest visibility and ordered source ancestry. */
final class SourceLauncherQualificationSuccessor {
    private static final String[][] SOURCES = {
        {"tools/harness/HexDigest.java", "dd084ae18c679222aa951a8bbf79a444726e92e6c94d994f24fd7499cc331057",
                "77a27e4874f54bd6e60569295d27dd946bcdeddb8092ce2fb365a79df82f38bc"},
        {"tools/harness/TrainSourceHistory.java", "67f1fe87b5070faa810081efd0ed247b7172e6540d30debaccca328ed19eb296",
                "445a483b577d6524e9679126f9ca8d9b750c157c2437b9809af4e563fcd01e36"},
        {"tools/harness/TrainSourceSuccessors.java", "d2152909fde1463d7cd550ddc6dc22cb40694749b26f50182bb5e498c7e8accd",
                "f4e599158e15c363168e2297115210c7aa03f061fd61c448e1e631761c85f45c"},
    };
    private SourceLauncherQualificationSuccessor() { }
    static boolean carries(String path, String prior, String current) {
        for (String[] source : SOURCES) {
            if (path.equals(source[0]) && prior.equals(source[1])
                    && current.equals(source[2])) return true;
        }
        return false;
    }
    static void selfTest() {
        for (String[] source : SOURCES) {
            if (!carries(source[0], source[1], source[2])
                    || carries(source[0], "unreviewed", source[2])
                    || carries(source[0], source[1], "unreviewed")) {
                throw new IllegalStateException("qualification source ancestry drift: " + source[0]);
            }
        }
    }
}
