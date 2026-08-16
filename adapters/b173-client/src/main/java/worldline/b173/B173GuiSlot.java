package worldline.b173;

/** Semantic inventory-slot selection result, independent of screen coordinates. */
public final class B173GuiSlot {
    private final int index;
    private final int itemId;
    private final int count;

    B173GuiSlot(int index, int itemId, int count) {
        this.index = index;
        this.itemId = itemId;
        this.count = count;
    }

    public int index() { return index; }

    public boolean empty() { return itemId < 0; }

    public int itemId() { return itemId; }

    public int count() { return count; }
}
