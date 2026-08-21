# M372 placeable item crafts map

## Stable boundary

- `B173PlayerSeed.writeInventory` seeds stone, workbench `58`, oak planks
  `5x13`, white wool `35`, and one stick `280`.
- Two accepted Packet102 transactions in personal window 0 craft eight
  sticks `280` from two vertical plank pairs (M297-style 2x2).
- A raised-stone workbench `58` then accepts Packet102 matrix placement:
  - eight sticks around wool yield painting `321`
  - stick under six planks yield sign `323`
  - three planks in the bowl-V yield `281x4`
- Taking result slot 0 with each exact prediction is the Packet106 recipe
  oracle. Painting, sign, and bowls survive a clean save plus fresh login.

Vanilla b1.7.3 painting, sign, and bowl are 3-wide shaped recipes, so those
results use the workbench rather than the 2x2.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotCrafting
acceptance, Packet106 acknowledgements, Packet200 craft statistics,
ingredient consumption, player NBT persistence, and login Packet104
bootstrap. Protocol 14 does not transmit SlotCrafting result updates on
accepted grid clicks, so each result stack is the ACK-correlated local
model confirmed by the slot-0 take.

This crafts the placeable-item family. It does not place signs (M176),
write sign text (M350), spawn or orient paintings (M177/M351), or craft
food (M327).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2-planks5-sticks280+workbench58-painting321-sign323-bowl281|cause=packet102-window0-vertical-planks-to-sticks+packet102-workbench-matrix+result-take|wire=packet106-accepted+packet200-craft-stat|oracle=placeable-family-321-323-281+fresh-login|painting=321,sign=323,bowl=281x4,column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,grid=2x2+3x3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`80d1b7a10efe73807810ca2609135b07e47ba57880f35e72d1b205e27394a993`.

## Non-claims

M372 does not claim Packet15 placement, Packet130 text, Packet25 painting
direction, eating, or other recipes.
