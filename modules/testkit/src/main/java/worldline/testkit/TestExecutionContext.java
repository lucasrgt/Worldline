package worldline.testkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.GamePosition;
import worldline.api.GamePlayer;
import worldline.api.GameUi;
import worldline.api.SnapshotMinecraftRuntime;
import worldline.minimization.Scenario;
import worldline.test.TestContext;
import worldline.test.TestHook;
import worldline.test.TestObservation;
import worldline.trace.CanonicalStateTrace;

/** Mutable state owned by exactly one isolated attempt. */
final class TestExecutionContext implements TestContext {
    private static final String[] TRACE_FIELDS = {"tick", "time", "health", "slot",
            "x_bits", "y_bits", "z_bits", "entities"};
    private final long seed;
    private final int attempt;
    private final AutomatedMinecraftRuntime runtime;
    private final ArtifactStore artifacts;
    private final CanonicalStateTrace trace;
    private final List<TestHook> dynamicFailed = new ArrayList<>(), dynamicFinished = new ArrayList<>();
    private final List<String> steps = new ArrayList<>();
    private final Set<String> allowed;
    private int ticks, sequence;

    TestExecutionContext(long seed, int attempt, AutomatedMinecraftRuntime runtime,
            ArtifactStore artifacts, List<String> allowedSteps) {
        this.seed = seed; this.attempt = attempt; this.runtime = runtime; this.artifacts = artifacts;
        allowed = allowedSteps == null ? null : new HashSet<>(allowedSteps);
        trace = runtime == null ? null : new CanonicalStateTrace(seed, TRACE_FIELDS);
        if (trace != null) record("start");
    }

    @Override public long seed() { return seed; }
    @Override public int attempt() { return attempt; }
    @Override public AutomatedMinecraftRuntime runtime() {
        if (runtime == null) throw new IllegalStateException("test did not configure a runtime provider");
        return runtime;
    }
    @Override public java.nio.file.Path artifactDirectory() { return artifacts.directory(); }
    @Override public void attach(String name, byte[] bytes) {
        try { artifacts.write(name, bytes); }
        catch (IOException error) { throw new IllegalStateException("artifact write failed", error); }
    }
    @Override public void skip(String reason) {
        if (reason == null || reason.trim().isEmpty()) throw new IllegalArgumentException("skip reason is blank");
        throw new SkipSignal(reason.trim());
    }
    @Override public void step(String name, TestAction action) throws Exception {
        if (name == null || name.trim().isEmpty() || action == null) {
            throw new IllegalArgumentException("step requires a name and action");
        }
        String clean = name.trim();
        for (int index = 0; index < clean.length(); index++) {
            char item = clean.charAt(index);
            if (item < 0x20 || item > 0x7e) throw new IllegalArgumentException("step name must be visible ASCII");
        }
        String token = String.format("%04d:%s", ++sequence, clean); steps.add(token);
        if (allowed == null || allowed.contains(token)) action.run(this);
    }
    @Override public void onFinished(TestHook hook) {
        if (hook == null) throw new NullPointerException("hook"); dynamicFinished.add(0, hook);
    }
    @Override public void onFailed(TestHook hook) {
        if (hook == null) throw new NullPointerException("hook"); dynamicFailed.add(0, hook);
    }
    @Override public void tick() { runtime().tick(); ticks++; record("tick" + ticks); }
    @Override public GameUi ui() {
        observe("ui.tree", "GUI_TREE");
        return TestContext.super.ui();
    }
    @Override public void tick(int count) {
        if (count < 0) throw new IllegalArgumentException("tick count must not be negative");
        for (int index = 0; index < count; index++) tick();
    }
    @Override public GamePlayer player() { observe("player", "PLAYER"); return runtime().player(); }
    @Override public int health() { observe("player.health", "PLAYER_HEALTH"); return runtime().player().health(); }
    @Override public int selectedHotbarSlot() {
        observe("player.hotbar.selected", "HOTBAR_SLOT"); return runtime().player().selectedHotbarSlot();
    }
    @Override public GamePosition position() {
        observe("player.position", "PLAYER_POSITION"); return runtime().player().position();
    }
    @Override public worldline.api.BlockState block(worldline.api.BlockPosition position) {
        observe("block[" + position.x() + "," + position.y() + "," + position.z() + "]", "BLOCK_STATE");
        return runtime().world().block(position);
    }
    @Override public void setBlock(worldline.api.BlockPosition position, worldline.api.BlockState state) {
        observe("block[" + position.x() + "," + position.y() + "," + position.z() + "]", "BLOCK_STATE");
        TestContext.super.setBlock(position, state);
    }

    List<TestHook> dynamicFailed() { return dynamicFailed; }
    List<TestHook> dynamicFinished() { return dynamicFinished; }
    List<String> steps() { return new ArrayList<>(steps); }
    String trace() { return trace == null ? null : trace.value(); }
    byte[] snapshot() {
        return runtime instanceof SnapshotMinecraftRuntime
                ? ((SnapshotMinecraftRuntime) runtime).snapshot().bytes() : null;
    }
    Scenario scenario() { return steps.isEmpty() ? null : Scenario.of(steps); }

    void writeFailure(Throwable error) {
        String text = error.getClass().getName() + ": " + String.valueOf(error.getMessage()) + "\n";
        attach("failure.txt", text.getBytes(StandardCharsets.UTF_8));
        GameUiFailureArtifacts.capture(runtime, artifacts);
        if (trace != null) attach("failure.wltrace", trace.value().getBytes(StandardCharsets.UTF_8));
        byte[] captured = snapshot(); if (captured != null) attach("failure.wlsnapshot", captured);
        Scenario value = scenario(); if (value != null) attach("failure.wlscenario", value.bytes());
    }

    void writeTimeout() {
        GameUiFailureArtifacts.capture(runtime, artifacts);
        attach("timeout.wltrace", trace == null ? new byte[0]
                : trace.value().getBytes(StandardCharsets.UTF_8));
        byte[] captured = snapshot(); if (captured != null) attach("timeout.wlsnapshot", captured);
    }

    private void observe(String field, String role) { TestObservation.record(ticks, field, role); }

    private void record(String label) {
        GamePosition position = runtime.player().position();
        trace.record(label, ticks, runtime.world().time(), runtime.player().health(),
                runtime.player().selectedHotbarSlot(), Double.doubleToLongBits(position.x()),
                Double.doubleToLongBits(position.y()), Double.doubleToLongBits(position.z()),
                runtime.world().entities().size());
    }
}
