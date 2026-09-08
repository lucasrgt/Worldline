package worldline.testkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.test.WorldlineSpec;
import static worldline.test.Worldline.test;

final class RunnerProgressTest {
    private RunnerProgressTest() {}

    static void run() throws Exception {
        Path root = Files.createTempDirectory("worldline-progress-test-");
        Recorder on = new Recorder(), off = new Recorder();
        TestRunResult visible = execute(root.resolve("on"), on, true);
        TestRunResult hidden = execute(root.resolve("off"), off, false);
        require(visible.tests().size() == hidden.tests().size(), "overlay changed selection");
        for (int index = 0; index < visible.tests().size(); index++)
            require(visible.tests().get(index).status() == hidden.tests().get(index).status(),
                    "overlay changed test outcomes");
        require(off.frames.isEmpty(), "disabled overlay published frames");
        require(on.opened == on.closed && on.opened == 6, "overlay leaked runtime sessions");
        require(has(on, "Step: outer") && has(on, "Step: inner") && has(on, "Step: Cleanup"),
                "step lifecycle was not published");
        require(has(on, "Attempt: 2/2") && has(on, "FLAKY:") && has(on, "SKIPPED:"),
                "attempt states were not published");
        require(has(on, "Done: 5/5") && has(on, "Flaky: 1") && has(on, "Last failure:") && has(on, "Failed: 1"),
                "completed counters include attempts or stale failures");
        require(has(on, "Seed: 173"), "seed missing");
        for (List<String> frame : on.frames) {
            require(frame.size() <= 16, "overlay frame is unbounded");
            for (String line : frame) require(line.chars().allMatch(value -> value >= 32 && value <= 126),
                    "unsafe text reached the native font renderer");
        }
        Recorder broken = new Recorder(); broken.broken = true;
        TestRunResult failed = execute(root.resolve("broken"), broken, true);
        require(!failed.passed() && broken.closed == broken.opened,
                "HUD failure bypassed runtime cleanup");
        RunnerProgress progress = new RunnerProgress(1, true, "progress-test");
        RunnerProgress.Attempt attempt = progress.attempt("suite", "test", 1, 1, 173, 1000);
        Recorder stale = new Recorder(); attempt.open(stale.open(null)); attempt.close();
        int frames = stale.frames.size(); attempt.tick(); attempt.phase("late callback");
        require(stale.frames.size() == frames, "closed attempt published stale progress");
    }

    private static TestRunResult execute(Path root, Recorder provider, boolean overlay) {
        return new TestRunner().run(new Sample(), new RunnerOptions().provider(provider)
                .artifacts(root.resolve("artifacts")).runtimeLock(root.resolve("runtime.lock"))
                .overlay(overlay).timeout(10000), null);
    }

    private static final class Sample extends WorldlineSpec {
        private int attempts;
        @Override protected void define() {
            test("nested machine steps", context -> context.step("outer", outer -> {
                outer.step("inner", inner -> { });
            }));
            test("retry then pass", context -> {
                if (++attempts == 1) throw new AssertionError("first attempt failed\nwith a newline");
            }).retry(1);
            test("skip", context -> context.skip("fixture unavailable"));
            test("failed machine", context -> { throw new AssertionError("processing failed"); });
            test("after failure", context -> { });
        }
    }

    private static final class Recorder implements TestRuntimeProvider, TestRuntimeSession {
        final List<List<String>> frames = Collections.synchronizedList(new ArrayList<>());
        int opened, closed; boolean broken;
        @Override public String runtimeId() { return "progress-test"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) { opened++; return this; }
        @Override public void close() { closed++; }
        @Override public void showProgress(List<String> lines) {
            if (broken) throw new IllegalStateException("display unavailable");
            frames.add(new ArrayList<>(lines));
        }
    }

    private static boolean has(Recorder recorder, String text) {
        return recorder.frames.stream().flatMap(List::stream).anyMatch(line -> line.contains(text));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
