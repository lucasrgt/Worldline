<!-- worldline-map-schema=1 -->
<!-- boundary=m65-peer-armor -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7bf03514d4331779e14ecaf3379ecf89d3bea276115ca77e909e5a9160587fe4 -->

# M65 peer armor map

## Stable boundary

- The actor equips the exact undamaged leather set through eight accepted
  window-0 left-click Packet102 transactions.
- The production Packet102 encoder is checked byte-for-byte for source slots
  36..39, armor slots 5..8, actions 1..8, no shift, exact take stacks, and
  null place predictions.
- An independent named peer observes Packet5 mappings helmet 5->4,
  chestplate 6->3, leggings 7->2, and boots 8->1.
- A fresh server and two fresh clients prove the four local Packet104 armor
  slots and the four peer Packet5 bootstrap values survive restart.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotArmor
acceptance, Packet106 acknowledgements, Packet5 equipment broadcasts, player
NBT persistence, and restart bootstrap behavior. Packet5 carries item ID and
damage but no count; count one is established by the actor's window state.

## Non-claims

M65 does not claim other armor materials, damaged armor, replacement, removal,
durability, combat, damage reduction, generic inventory clicks, shift/right
clicks, or rejected armor transaction recovery.

Frozen expected signature SHA-256: `7bf03514d4331779e14ecaf3379ecf89d3bea276115ca77e909e5a9160587fe4`
