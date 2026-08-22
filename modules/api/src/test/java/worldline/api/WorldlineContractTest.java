package worldline.api;

final class WorldlineContractTest {
    private WorldlineContractTest() {}

    static void run() {
        if (WorldlineContract.all().size() != 42
                || WorldlineContract.require("trace-divergence") != WorldlineContract.TRACE_DIVERGENCE
                || !WorldlineContract.MOD_LOADING.subject().contains("mod loading")
                || WorldlineContract.require("scenario-coverage") != WorldlineContract.SCENARIO_COVERAGE
                || WorldlineContract.require("runtime-census") != WorldlineContract.RUNTIME_CENSUS
                || WorldlineContract.require("ui-action-equivalence")
                        != WorldlineContract.UI_ACTION_EQUIVALENCE
                || WorldlineContract.require("aero-cache-lifecycle") != WorldlineContract.AERO_CACHE_LIFECYCLE
                || WorldlineContract.require("aero-save-window") != WorldlineContract.AERO_SAVE_WINDOW
                || WorldlineContract.require("logical-item-reference")
                        != WorldlineContract.LOGICAL_ITEM_REFERENCE
                || WorldlineContract.require("state-world-differential")
                        != WorldlineContract.STATE_WORLD_DIFFERENTIAL
                || WorldlineContract.require("testkit-runtime") != WorldlineContract.TESTKIT_RUNTIME)
            throw new AssertionError("TestKit contract catalog drifted");
        try { WorldlineContract.all().clear(); throw new AssertionError("mutable TestKit contract catalog"); }
        catch (UnsupportedOperationException expected) { }
        try { WorldlineContract.require("m5-reproduction-bundle");
            throw new AssertionError("progress ID accepted as TestKit contract"); }
        catch (IllegalArgumentException expected) { }
    }
}
