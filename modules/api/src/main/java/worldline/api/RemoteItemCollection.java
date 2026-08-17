package worldline.api;

import java.util.Objects;

/** Immutable completed collection of one exact dropped item by a named player. */
public final class RemoteItemCollection {
    private final RemoteDroppedItem droppedItem;
    private final int collectorEntityId;
    private final String collectorUsername;

    public RemoteItemCollection(RemoteDroppedItem droppedItem, int collectorEntityId,
            String collectorUsername) {
        this.droppedItem = Objects.requireNonNull(droppedItem, "droppedItem");
        if (collectorEntityId < 0 || collectorEntityId == droppedItem.entityId())
            throw new IllegalArgumentException("invalid collector entity ID");
        if (collectorUsername == null || !collectorUsername.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid collector username");
        this.collectorEntityId = collectorEntityId; this.collectorUsername = collectorUsername;
    }

    public RemoteDroppedItem droppedItem() { return droppedItem; }
    public int collectorEntityId() { return collectorEntityId; }
    public String collectorUsername() { return collectorUsername; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteItemCollection)) return false;
        RemoteItemCollection value = (RemoteItemCollection) other;
        return droppedItem.equals(value.droppedItem) && collectorEntityId == value.collectorEntityId
                && collectorUsername.equals(value.collectorUsername);
    }
    @Override public int hashCode() { return Objects.hash(droppedItem, collectorEntityId, collectorUsername); }
    @Override public String toString() { return "RemoteItemCollection[item=" + droppedItem.entityId()
            + ",collector=" + collectorUsername + ":" + collectorEntityId + ",terminal=destroyed]"; }
}
