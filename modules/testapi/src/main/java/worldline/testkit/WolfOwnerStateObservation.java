package worldline.testkit;

import worldline.api.RemoteMobSpawn;

/** One qualified wolf materialization, tame, collar and owner sit/stand sequence. */
public final class WolfOwnerStateObservation {
    private final RemoteMobSpawn wolf;
    private final int actorEntityId;
    private final int tameStatus;
    private final int boneItemId;
    private final int collarDyeDamage;
    private final boolean redCollarObserved;
    private final int interactionItemId;
    private final int interactionButton;
    private final int initialSittingFlags;
    private final int standingFlags;
    private final int sittingFlags;
    private final int finalStandingFlags;
    private final boolean deathObserved;

    public WolfOwnerStateObservation(RemoteMobSpawn wolf, int actorEntityId, int tameStatus,
            int boneItemId, int collarDyeDamage, boolean redCollarObserved,
            int interactionItemId, int interactionButton, int initialSittingFlags,
            int standingFlags, int sittingFlags, int finalStandingFlags,
            boolean deathObserved) {
        this.wolf = wolf;
        this.actorEntityId = actorEntityId;
        this.tameStatus = tameStatus;
        this.boneItemId = boneItemId;
        this.collarDyeDamage = collarDyeDamage;
        this.redCollarObserved = redCollarObserved;
        this.interactionItemId = interactionItemId;
        this.interactionButton = interactionButton;
        this.initialSittingFlags = initialSittingFlags;
        this.standingFlags = standingFlags;
        this.sittingFlags = sittingFlags;
        this.finalStandingFlags = finalStandingFlags;
        this.deathObserved = deathObserved;
    }

    public RemoteMobSpawn wolf() { return wolf; }
    public int actorEntityId() { return actorEntityId; }
    public int tameStatus() { return tameStatus; }
    public int boneItemId() { return boneItemId; }
    public int collarDyeDamage() { return collarDyeDamage; }
    public boolean redCollarObserved() { return redCollarObserved; }
    public int interactionItemId() { return interactionItemId; }
    public int interactionButton() { return interactionButton; }
    public int initialSittingFlags() { return initialSittingFlags; }
    public int standingFlags() { return standingFlags; }
    public int sittingFlags() { return sittingFlags; }
    public int finalStandingFlags() { return finalStandingFlags; }
    public boolean deathObserved() { return deathObserved; }
}
