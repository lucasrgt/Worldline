package worldline.api;

import java.util.Properties;

final class WorldlineEvidenceTest {
    private static final String SIG_A = "702d4dc074d1db9a965d74f49f1318cb05a4397c343a59b8fde15a3ab8f15505";
    private static final String SIG_B = "52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98";
    private static final String SIGNAL = "mob=type50,fuse=proximity-stay+packet40-state1,packet60=followed";

    private WorldlineEvidenceTest() {}

    static void run() {
        WorldlineBehavior fuse = WorldlineBehavior.CREEPER_FUSE;
        if (!"creeper-fuse".equals(fuse.token()) || !"atlas.scenario.creeper-fuse".equals(fuse.atlasId())
                || fuse != WorldlineBehavior.require("m448-creeper-fuse-set")
                || fuse != WorldlineBehavior.require("atlas.scenario.creeper-fuse"))
            throw new AssertionError("behavior catalog must hide progress ids");
        WorldlineEvidence pin = WorldlineEvidence.pin(fuse, SIGNAL, SIG_A);
        WorldlineEvidence modSame = WorldlineEvidence.of(fuse, WorldlineEvidence.MOD, SIGNAL, SIG_A);
        if (!pin.equals(modSame) || !pin.behavior().equals(fuse))
            throw new AssertionError("behavior equality must ignore lane");
        WorldlineEvidenceDiff diff = pin.compare(WorldlineEvidence.of(fuse, WorldlineEvidence.MOD, SIGNAL, SIG_B));
        if (!diff.diverged() || !diff.sameBehavior() || diff.sameSignature()
                || !diff.render().contains("left.behavior=creeper-fuse"))
            throw new AssertionError("evidence diff drifted");
        Properties smoke = new Properties();
        smoke.setProperty("id", "m448-creeper-fuse-set");
        smoke.setProperty("expected.signal", SIGNAL);
        smoke.setProperty("expected.signature", SIG_A);
        if (!pin.equals(WorldlineEvidence.pin(smoke))) throw new AssertionError("smoke pin must use semantic token");
        smoke.setProperty("behavior", "creeper-fuse");
        if (!pin.equals(WorldlineEvidence.pin(smoke))) throw new AssertionError("explicit behavior pin drifted");
        smoke.setProperty("testkit.fixture", "server-hostile-spawner");
        smoke.setProperty("testkit.actions", "spawn-creeper,stay-in-fuse-range,advance-until-explosion");
        smoke.setProperty("testkit.observations", "creeper-fuse-state,explosion-packet");
        smoke.setProperty("testkit.binding", "worldline.b173server.Creeper#stayUntilExplode");
        smoke.setProperty("testkit.evidence", "equatable");
        WorldlineBehaviorContract contract = WorldlineBehaviorContract.from(smoke);
        if (contract.behavior() != fuse || contract.actions().size() != 3
                || !contract.canonical().startsWith("atlas.scenario.creeper-fuse|"))
            throw new AssertionError("behavior contract drifted");
        smoke.setProperty("testkit.evidence", "snapshot-only");
        fail(() -> WorldlineBehaviorContract.from(smoke));
        fail(() -> WorldlineEvidence.pin("m448-creeper-fuse-set", SIGNAL, "pending"));
        fail(() -> WorldlineBehavior.require("m446-zombie-door-break-set"));
        fail(() -> WorldlineBehavior.require("not-a-behavior"));
        fail(() -> WorldlineEvidence.pin(fuse, SIGNAL, SIG_A).compare(null));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("behavior accepted invalid value"); }
        catch (IllegalArgumentException expected) { }
        catch (NullPointerException expected) { }
    }
}
