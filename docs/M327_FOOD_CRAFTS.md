# M327 food crafts

M327 is the compound food-crafting SET. One official session crafts two
sugar `353` from sugar cane `338` in the personal 2x2 grid, then crafts
mushroom stew `282`, bread `297`, cookies `357x8`, and cake `354` on a
placed workbench `58`. At least three of those four result ids appear in
the frozen signal.

This milestone clones M297 2x2 single-cell takes and M318 workbench matrix
takes. Vanilla b1.7.3 stew is a 3-tall shaped recipe, so stew uses the
workbench rather than the 2x2. It never air-uses food and is distinct from
M258-M266 eat-only.

## Contract

The actor starts with wheat `296x8`, cocoa `351:3`, sugar cane `338x2`, egg
`344`, mushrooms, a bowl, and three milk buckets `335`, plus stone and a
workbench item.

| Grid | Recipe | Result |
| --- | --- | --- |
| personal 2x2 | cane `338` in slot 1 | `353x1` twice |
| workbench 3x3 | brown / red / bowl | `282x1` |
| workbench 3x3 | three wheat | `297x1` |
| workbench 3x3 | wheat, cocoa `351:3`, wheat | `357x8` |
| workbench 3x3 | milk / sugar+egg+sugar / wheat | `354x1` plus `325x3` |

Each predicted transition commits only after the correlated Packet106
acceptance. Acceptance of stew, bread, cookies, and cake on slot 0 are the
official recipe oracles.

## Evidence

After save and reconnect, Packet104 restores stew, bread, cookies, cake,
and three empty buckets. Headless `B173WireClient` protocol-14 only. No
GUI. No Aero.

The frozen semantic SHA-256 is
`feb202ff5d2172def94a39a6a9e560b5e4ecdba79681b018a8e046bb89703a54`.
