# M297 basic crafts

M297 is the compound personal 2x2 crafting SET. One official session crafts
oak planks from log item `17`, sticks item `280` from a vertical plank pair,
and torches item `50` from coal item `263` above a stick. Both required
result ids `280` and `50` appear in the frozen signal.

This milestone clones M319 stick-craft and M320 torch-craft oracles in one
window-0 grid. Placing two planks and one stick uses M220 workbench-style
right-click Packet102 button `1` so a stack can feed the 2x2 matrix one item
at a time. It never sends Packet15 and does not open a 3x3 workbench.

## Contract

The actor starts with log `17x1:0` in slot 36 and coal `263x1:0` in slot 37.

| Actions | Recipe | Grid | Result |
| ---: | --- | --- | --- |
| 1-4 | log to planks | slot 1 | `5x4:0` |
| 5-10 | two planks vertical | slots 1+3 | `280x4:0` |
| 11-17 | coal above stick | slots 1+3 | `50x4:0` |

Each predicted transition commits only after the correlated Packet106
acceptance. Acceptance of sticks `280x4:0` and torches `50x4:0` on slot 0
are the official recipe oracles.

## Evidence

After save and reconnect, Packet104 restores planks `5x2:0`, torches
`50x4:0`, and remaining sticks `280x3:0`. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`f62ec64a6ea2c9990cdbf656cdedabe239862a866983d92adfb792d4f81d82a3`.
