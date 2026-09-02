package worldline.testapi;

import worldline.api.RemoteObjectSpawn;

/** Qualified primed-TNT Packet23 materialization plus exact internal fuse checkpoints. */
public final class TntFuseLifecycleObservation {
    private final RemoteObjectSpawn primed;
    private final int actorEntityId;
    private final int seedFuse;
    private final int tick1Fuse;
    private final int tick40Fuse;
    private final int tick79Fuse;
    private final int tick80Fuse;
    private final int tick81Fuse;
    private final boolean seedPresent;
    private final boolean midPresent;
    private final boolean midDead;
    private final boolean terminalPresent;
    private final boolean terminalDead;
    private final boolean motionZeroed;
    private final int unprimedBlockId;
    private final boolean unprimedEntityObserved;

    public TntFuseLifecycleObservation(RemoteObjectSpawn primed, int actorEntityId,
            int seedFuse, int tick1Fuse, int tick40Fuse, int tick79Fuse,
            int tick80Fuse, int tick81Fuse, boolean seedPresent, boolean midPresent,
            boolean midDead, boolean terminalPresent, boolean terminalDead,
            boolean motionZeroed, int unprimedBlockId, boolean unprimedEntityObserved) {
        this.primed = primed;
        this.actorEntityId = actorEntityId;
        this.seedFuse = seedFuse;
        this.tick1Fuse = tick1Fuse;
        this.tick40Fuse = tick40Fuse;
        this.tick79Fuse = tick79Fuse;
        this.tick80Fuse = tick80Fuse;
        this.tick81Fuse = tick81Fuse;
        this.seedPresent = seedPresent;
        this.midPresent = midPresent;
        this.midDead = midDead;
        this.terminalPresent = terminalPresent;
        this.terminalDead = terminalDead;
        this.motionZeroed = motionZeroed;
        this.unprimedBlockId = unprimedBlockId;
        this.unprimedEntityObserved = unprimedEntityObserved;
    }

    public RemoteObjectSpawn primed() { return primed; }
    public int actorEntityId() { return actorEntityId; }
    public int seedFuse() { return seedFuse; }
    public int tick1Fuse() { return tick1Fuse; }
    public int tick40Fuse() { return tick40Fuse; }
    public int tick79Fuse() { return tick79Fuse; }
    public int tick80Fuse() { return tick80Fuse; }
    public int tick81Fuse() { return tick81Fuse; }
    public boolean seedPresent() { return seedPresent; }
    public boolean midPresent() { return midPresent; }
    public boolean midDead() { return midDead; }
    public boolean terminalPresent() { return terminalPresent; }
    public boolean terminalDead() { return terminalDead; }
    public boolean motionZeroed() { return motionZeroed; }
    public int unprimedBlockId() { return unprimedBlockId; }
    public boolean unprimedEntityObserved() { return unprimedEntityObserved; }
}
