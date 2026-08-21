# M65 Peer Armor Equipment

M65 moves beyond container workflows into a bounded renderer-visible
multiplayer state: one complete undamaged leather set equipped by the actor and
observed by a named peer.

## Contract

`ArmorEquipmentSession` adds one exact leather equipment operation and a typed
peer observation. Each operation takes a count-one, damage-zero leather piece
from personal storage, left-clicks its matching empty armor slot, and commits
each predicted transition only after the correlated Packet106 acceptance.

The legacy layouts run in opposite directions:

| Personal window 0 | Piece | Item | Packet5 slot |
| ---: | --- | ---: | ---: |
| 5 | helmet | 298 | 4 |
| 6 | chestplate | 299 | 3 |
| 7 | leggings | 300 | 2 |
| 8 | boots | 301 | 1 |

`RemoteArmorEquip` preserves the immutable before, cursor-taken, and equipped
window snapshots. `RemoteArmorPiece` intentionally omits count because Packet5
does not transmit it. The peer tracker keeps held slot zero behavior unchanged
and records armor only for entity IDs already bound to player usernames.

## Evidence

The smoke freezes the exact 92-byte sequence for eight Packet102 messages,
then exercises the same encoder against the official server. Four independent
Packet5 observations prove that the server accepted the typed destinations.
After save and restart, Packet104 restores all local armor stacks and the new
observer receives all four Packet5 bootstrap values.

## Non-claims

The boundary is leather-only. It does not expose arbitrary equipment writes,
right/shift click, occupied-slot replacement, unequip, damaged armor,
durability, combat, health, damage reduction, or reject recovery.
