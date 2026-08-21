# M273 chain boots

M273 isolates one undamaged chain boots stack from the M65 leather set. The
actor left-clicks personal slot 36 onto empty armor slot 8. Item `305` is
distinct from leather boots `301`.

## Contract

`ArmorEquipmentSession.equipArmor` takes a count-one, damage-zero matching
piece from personal storage, left-clicks the empty boots slot, and commits
each predicted transition only after the correlated Packet106 acceptance.

| Personal window 0 | Piece | Item | Packet5 slot |
| ---: | --- | ---: | ---: |
| 8 | boots | 305 | 1 |

`RemoteArmorEquip` preserves the immutable before, cursor-taken, and equipped
window snapshots. `RemoteArmorPiece` omits count because Packet5 does not
transmit it. The peer tracker records armor only for entity IDs already bound
to player usernames.

## Evidence

The smoke freezes the exact 23-byte sequence for two Packet102 messages, then
exercises the same encoder against the official server. One independent
Packet5 observation proves that the server accepted boots destination slot 1.
After save and reconnect, Packet104 restores the local armor stack and the
observer receives the Packet5 bootstrap value 305.

## Non-claims

The boundary is chain-boots-only. It does not expose arbitrary equipment
writes, right/shift click, occupied-slot replacement, unequip, damaged armor,
durability, combat, health, damage reduction, other chain pieces, or reject
recovery.
