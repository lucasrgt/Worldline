package worldline.api;

/** Completed window workflow extended with exact leather armor equipment and peer observation. */
public interface ArmorEquipmentSession extends WorkbenchOutputSession {
    RemoteArmorEquip equipLeatherArmor(int personalSlot, RemoteArmorSlot slot);
    RemoteArmorPiece awaitPeerArmor(RemoteArmorPiece expected);
}
