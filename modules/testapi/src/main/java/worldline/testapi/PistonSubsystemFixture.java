package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineRedstoneBehaviors;

/** Executes and validates the complete reusable Beta 1.7.3 piston fixture. */
public final class PistonSubsystemFixture {
    private static final String DOMAINS = "29=0..5+8..13,33=0..5+8..13,"
            + "34=0..5+8..13,36=0..5+8..13";
    private static final String MATERIALIZATION = "normal=33:5>36:5>34:5,"
            + "sticky=29:5>36:13>34:13,items=34:none+36:none";
    private static final String BREAK_DROPS = "head=34:5->0:0+base=33:13->0:0+drop=33x1:0,"
            + "moving=36:0->0:0+drop=4x1:0";
    private static final String PERSISTENCE = "head=34:5,moving=36:5+te=34:5:5:true";
    private static final String COLLISION = "base=1:full,head=2:plate+rod,moving=1:translated";
    private static final String LIGHT = "29=0:0,33=0:0,34=0:0,36=0:0";
    private static final String TICKS = "random=FFFF,idle=33:5+29:5+34:5@20-window,"
            + "moving=36:5->34:5@3-te";
    private static final String NEIGHBORS = "normal=33:5->13->5,sticky=29:5->13->5,"
            + "head=34:5->0:0,moving-te=held";

    private PistonSubsystemFixture() { }

    public static PistonSubsystemEvidence execute(PistonSubsystemScenario scenario) {
        PistonSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(MATERIALIZATION, actual.materialization(), "gameplay materialization");
        expect(BREAK_DROPS, actual.breakAndDrops(), "break and drops");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(COLLISION, actual.collision(), "collision");
        expect(LIGHT, actual.light(), "light");
        expect(TICKS, actual.ticks(), "tick policy");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("piston-subsystem")
                != WorldlineRedstoneBehaviors.PISTON_SUBSYSTEM)
            throw new IllegalStateException("piston-subsystem behavior registration drifted");
        return new PistonSubsystemEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
