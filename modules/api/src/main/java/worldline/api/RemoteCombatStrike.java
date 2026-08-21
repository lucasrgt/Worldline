package worldline.api;

import java.util.Objects;

/** Local attack requester followed by a matching target hurt observation in the bounded session. */
public final class RemoteCombatStrike {
    private final String attacker, target; private final int attackerEntityId, targetEntityId;
    public RemoteCombatStrike(String attacker, int attackerEntityId, String target, int targetEntityId) {
        if (!name(attacker) || !name(target) || attacker.equals(target)
                || attackerEntityId < 0 || targetEntityId < 0 || attackerEntityId == targetEntityId)
            throw new IllegalArgumentException("invalid combat strike identity");
        this.attacker = attacker; this.attackerEntityId = attackerEntityId;
        this.target = target; this.targetEntityId = targetEntityId;
    }
    public String attacker() { return attacker; } public int attackerEntityId() { return attackerEntityId; }
    public String target() { return target; } public int targetEntityId() { return targetEntityId; }
    public int weaponId() { return 276; } public int hurtStatus() { return 2; }
    private static boolean name(String value) { return value != null && value.matches("[A-Za-z0-9_]{1,16}"); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteCombatStrike)) return false;
        RemoteCombatStrike value = (RemoteCombatStrike) other; return attacker.equals(value.attacker)
                && target.equals(value.target) && attackerEntityId == value.attackerEntityId
                && targetEntityId == value.targetEntityId; }
    @Override public int hashCode() { return Objects.hash(attacker, attackerEntityId, target, targetEntityId); }
}
