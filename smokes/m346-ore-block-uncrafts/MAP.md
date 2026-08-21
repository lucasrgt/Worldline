# M346 ore-block uncrafts map

## Stable boundary

- `B173PlayerSeed.writeInventory` seeds gold block `41`, iron block `42`,
  diamond block `57`, and lapis block `22` into hotbar slots that become
  window-0 slots 36 through 39.
- Sixteen accepted Packet102 transactions in personal window 0:
  - left-click the gold block into matrix slot 1 and take gold ingots `266x9:0`
  - left-click the iron block into matrix slot 1 and take iron ingots `265x9:0`
  - left-click the diamond block into matrix slot 1 and take diamonds `264x9:0`
  - left-click the lapis block into matrix slot 1 and take lapis dye `351x9:4`
- Taking result slot 0 with each exact prediction is the Packet106 recipe
  oracle. The four nine-item stacks survive a clean save plus fresh login.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotCrafting
acceptance, Packet106 acknowledgements, ingredient consumption, player NBT
persistence, and login Packet104 bootstrap. Protocol 14 does not transmit
SlotCrafting result updates on accepted grid clicks, so each result stack is
the ACK-correlated local model confirmed by the slot-0 take.

This is player-inventory 2x2 uncrafting. It does not open a 3x3 workbench,
send Packet15, or craft storage blocks from nine ingots.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2-gold41+iron42+diamond57+lapis22|window0=gold-to-ingot266+iron-to-ingot265+diamond-to-gem264+lapis-to-dye351:4|cause=packet102-window0-left|wire=packet106-accepted|oracle=result266x9+result265x9+result264x9+result351x9:4+fresh-login|result=266x9:0+265x9:0+264x9:0+351x9:4,taken=true,stored=36:266x9:0+37:265x9:0+38:264x9:0+39:351x9:4,actions=16,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6cb6facb7859e30e6d0834273f32ba84f01bede6c1d8d39ad7dcf6b33818f452`.

## Non-claims

M346 does not claim 9-ingot storage-block crafts, place-only gold/iron/
diamond/lapis blocks (M212-M215), 3x3 workbench recipes, metadata variants
other than lapis dye `351:4`, shift clicks, or generic inventory automation.
