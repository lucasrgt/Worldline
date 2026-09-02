package worldline.api;

/** Gate-discovered host tests for Fable P0 helpers, JUnit engine, and API partition. */
public final class ImprovementHostTest {
    private ImprovementHostTest() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new IllegalArgumentException("no arguments expected");
        WorldlineJunitEngine.Result passing = WorldlineJunitEngine.run(
                HostJunitSample.class.getName());
        if (passing.failures != 0)
            throw new AssertionError("JUnit engine failed a real host test:\n" + passing.report);
        if (!passing.report.contains("PASSED") || !passing.report.contains("failures=0"))
            throw new AssertionError("JUnit engine omitted a pass: " + passing.report);
        WorldlineJunitEngine.Result failing = WorldlineJunitEngine.run(
                HostJunitFailure.class.getName());
        if (failing.failures == 0 || !failing.report.contains("FAILED"))
            throw new AssertionError("JUnit engine swallowed a failure:\n" + failing.report);
        String digest = WorldlineJunitEngine.sha256Text("abc");
        if (!"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".equals(digest))
            throw new AssertionError("shared SHA-256 helper drifted");
        ApiSurfaceDoctor.verify();
        System.out.println("ImprovementHostTest passed");
        System.out.print(passing.report);
        System.out.print(failing.report);
    }
}
