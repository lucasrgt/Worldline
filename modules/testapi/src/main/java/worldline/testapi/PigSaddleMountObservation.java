package worldline.testapi;

import worldline.api.RemoteMobSpawn;

/** One qualified pig saddle-consumption and rider-attachment observation. */
public final class PigSaddleMountObservation {
    private final RemoteMobSpawn pig;
    private final int actorEntityId;
    private final int saddleItemId;
    private final int saddleCountBefore;
    private final int saddleCountAfter;
    private final int saddleInteractionButton;
    private final int mountInteractionButton;
    private final int attachedRiderEntityId;
    private final int attachedVehicleEntityId;
    private final boolean deathObserved;

    public PigSaddleMountObservation(RemoteMobSpawn pig, int actorEntityId,
            int saddleItemId, int saddleCountBefore, int saddleCountAfter,
            int saddleInteractionButton, int mountInteractionButton,
            int attachedRiderEntityId, int attachedVehicleEntityId,
            boolean deathObserved) {
        this.pig = pig;
        this.actorEntityId = actorEntityId;
        this.saddleItemId = saddleItemId;
        this.saddleCountBefore = saddleCountBefore;
        this.saddleCountAfter = saddleCountAfter;
        this.saddleInteractionButton = saddleInteractionButton;
        this.mountInteractionButton = mountInteractionButton;
        this.attachedRiderEntityId = attachedRiderEntityId;
        this.attachedVehicleEntityId = attachedVehicleEntityId;
        this.deathObserved = deathObserved;
    }

    public RemoteMobSpawn pig() { return pig; }
    public int actorEntityId() { return actorEntityId; }
    public int saddleItemId() { return saddleItemId; }
    public int saddleCountBefore() { return saddleCountBefore; }
    public int saddleCountAfter() { return saddleCountAfter; }
    public int saddleInteractionButton() { return saddleInteractionButton; }
    public int mountInteractionButton() { return mountInteractionButton; }
    public int attachedRiderEntityId() { return attachedRiderEntityId; }
    public int attachedVehicleEntityId() { return attachedVehicleEntityId; }
    public boolean deathObserved() { return deathObserved; }
}
