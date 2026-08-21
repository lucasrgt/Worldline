# M372 placeable item crafts

M372 is the compound placeable-item crafting SET. One official session
crafts eight sticks `280` from oak planks `5` in the personal 2x2 grid,
then crafts painting `321`, sign `323`, and bowls `281x4` on a placed
workbench `58`. All three result ids appear in the frozen signal.

This milestone clones M297 2x2 single-cell takes and M318 workbench matrix
takes. Vanilla b1.7.3 painting, sign, and bowl are 3-wide shaped recipes,
so those results use the workbench rather than the 2x2. It is distinct from
M176/M350 sign placement and text, M177/M351 painting spawn and orientation,
and M327 food crafts.

## Contract

The actor starts with oak planks `5x13`, white wool `35`, one stick `280`,
stone, and a workbench item.

| Grid | Recipe | Result |
| --- | --- | --- |
| personal 2x2 | two vertical planks | `280x4` twice |
| workbench 3x3 | eight sticks around wool | `321x1` |
| workbench 3x3 | six planks over one stick | `323x1` |
| workbench 3x3 | three planks in a V | `281x4` |

Each predicted transition commits only after the correlated Packet106
acceptance. Acceptance of painting, sign, and bowls on slot 0 are the
official recipe oracles.

## Evidence

After save and reconnect, Packet104 restores painting, sign, and bowls.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`80d1b7a10efe73807810ca2609135b07e47ba57880f35e72d1b205e27394a993`.
