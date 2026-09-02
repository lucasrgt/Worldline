package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Equatable normalized evidence for slime spawn, motion, split and slimeball drop. */
public final class SlimeLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final SlimeSplitObservation split;
    private final SlimeDropObservation drop;
    private final int splitMaximumAttempts;
    private final int dropMaximumAttempts;

    public SlimeLifecycleEvidence(List<EntityConformanceCase> claims, SlimeSplitObservation split,
            SlimeDropObservation drop, int splitMaximumAttempts, int dropMaximumAttempts) {
        this.claims = Collections.unmodifiableList(new ArrayList<EntityConformanceCase>(claims));
        this.split = split;
        this.drop = drop;
        this.splitMaximumAttempts = splitMaximumAttempts;
        this.dropMaximumAttempts = dropMaximumAttempts;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public SlimeSplitObservation split() { return split; }
    public SlimeDropObservation drop() { return drop; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.slime-lifecycle-evidence.v1\n");
        for (EntityConformanceCase claim : claims) value.append("claim=")
                .append(claim.claimId()).append('|').append(claim.layer()).append('\n');
        return value.append("spawn=packet:24,type:55,positive-id:true,below-y16:true\n")
                .append("motion=open:air+ground+span>100,low-roof:span<700\n")
                .append("split=parent-size>1,death:3,destroy:29,hurt:true,children>=1,type:55,near:true,max:")
                .append(splitMaximumAttempts).append('\n')
                .append("drop=size:1,item:341,count:1-2,damage:0,death:3,destroy:29,max:")
                .append(dropMaximumAttempts).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof SlimeLifecycleEvidence
                && canonical().equals(((SlimeLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
