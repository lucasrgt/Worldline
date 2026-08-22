package worldline.api;

final class WorldlineContractTest {
    private WorldlineContractTest() {}

    static void run() {
        if (WorldlineContract.all().size() != 32
                || WorldlineContract.require("trace-divergence") != WorldlineContract.TRACE_DIVERGENCE
                || !WorldlineContract.MOD_LOADING.subject().contains("mod loading")
                || WorldlineContract.require("scenario-coverage") != WorldlineContract.SCENARIO_COVERAGE
                || WorldlineContract.require("runtime-census") != WorldlineContract.RUNTIME_CENSUS)
            throw new AssertionError("TestKit contract catalog drifted");
        try { WorldlineContract.all().clear(); throw new AssertionError("mutable TestKit contract catalog"); }
        catch (UnsupportedOperationException expected) { }
        try { WorldlineContract.require("m5-reproduction-bundle");
            throw new AssertionError("progress ID accepted as TestKit contract"); }
        catch (IllegalArgumentException expected) { }
    }
}
