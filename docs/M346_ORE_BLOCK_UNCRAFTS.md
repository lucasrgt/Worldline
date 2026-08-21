# M346 ore-block uncrafts

M346 is the compound personal 2x2 ore-block uncraft SET. One official
session uncrafts gold block `41`, iron block `42`, diamond block `57`, and
lapis block `22` back to nine gold ingots `266`, iron ingots `265`, diamonds
`264`, and lapis dye `351:4`. At least three of those uncraft result ids
appear in the frozen signal.

This milestone clones the M297 personal 2x2 single-cell take. It never sends
Packet15 and does not open a 3x3 workbench. It is distinct from 9-ingot
storage-block crafts and from place-only M212-M215.

## Contract

The actor starts with gold block `41x1:0` in slot 36, iron block `42x1:0` in
slot 37, diamond block `57x1:0` in slot 38, and lapis block `22x1:0` in slot
39.

| Actions | Recipe | Grid | Result |
| ---: | --- | --- | --- |
| 1-4 | gold block to ingots | slot 1 | `266x9:0` |
| 5-8 | iron block to ingots | slot 1 | `265x9:0` |
| 9-12 | diamond block to gems | slot 1 | `264x9:0` |
| 13-16 | lapis block to dye | slot 1 | `351x9:4` |

Each predicted transition commits only after the correlated Packet106
acceptance. Acceptance of gold ingots `266x9:0`, iron ingots `265x9:0`,
diamonds `264x9:0`, and lapis `351x9:4` on slot 0 are the official recipe
oracles.

## Evidence

After save and reconnect, Packet104 restores the four nine-item stacks.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`6cb6facb7859e30e6d0834273f32ba84f01bede6c1d8d39ad7dcf6b33818f452`.
