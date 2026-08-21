package worldline.api;

/** Legacy personal-window armor slot and its remote Packet5 equipment slot. */
public enum RemoteArmorSlot {
    HELMET(5, 4, 298),
    CHESTPLATE(6, 3, 299),
    LEGGINGS(7, 2, 300),
    BOOTS(8, 1, 301);

    private final int containerSlot, equipmentSlot, leatherItemId;
    RemoteArmorSlot(int containerSlot, int equipmentSlot, int leatherItemId) {
        this.containerSlot = containerSlot; this.equipmentSlot = equipmentSlot;
        this.leatherItemId = leatherItemId;
    }
    public int containerSlot() { return containerSlot; }
    public int equipmentSlot() { return equipmentSlot; }
    public int leatherItemId() { return leatherItemId; }
    public static RemoteArmorSlot fromEquipmentSlot(int value) {
        for (RemoteArmorSlot slot : values()) if (slot.equipmentSlot == value) return slot;
        throw new IllegalArgumentException("invalid armor equipment slot");
    }
}
