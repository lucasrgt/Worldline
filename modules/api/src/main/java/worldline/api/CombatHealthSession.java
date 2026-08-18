package worldline.api;

/** Peer armor extended with one fresh PvP strike and ordered local damage observation. */
public interface CombatHealthSession extends ArmorEquipmentSession {
    RemoteCombatStrike attackPlayer(String targetUsername);
    RemoteIncomingHit awaitIncomingHit(int expectedHealth);
}
