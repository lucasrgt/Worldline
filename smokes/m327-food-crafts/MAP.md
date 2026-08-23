<!-- worldline-map-schema=1 -->
<!-- boundary=m327-food-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=feb202ff5d2172def94a39a6a9e560b5e4ecdba79681b018a8e046bb89703a54 -->

# M327 food crafts map

## Stable boundary

- `B173PlayerSeed.writeInventory` seeds stone, workbench `58`, wheat `296x8`,
  cocoa beans `351:3`, sugar cane `338x2`, egg `344`, brown mushroom `39`, red
  mushroom `40`, bowl `281`, and three milk buckets `335`.
- Nine accepted Packet102 transactions in personal window 0 craft two sugar
  `353` from cane (M297-style 2x2, one cane in slot 1).
- A raised-stone workbench `58` then accepts Packet102 matrix placement
  (M318-style):
  - vertical brown, red, bowl in slots `2,5,8` yield stew `282`
  - three wheat in a row yield bread `297`
  - wheat, cocoa `351:3`, wheat yield cookies `357x8`
  - milk / sugar+egg+sugar / wheat yield cake `354` and leftover buckets `325`
- Taking result slot 0 with each exact prediction is the Packet106 recipe
  oracle. The four foods and three empty buckets survive a clean save plus
  fresh login.

Beta 1.7.3 stew is a 3-tall shaped recipe, so it cannot use the personal 2x2.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotCrafting
acceptance, Packet106 acknowledgements, Packet200 craft statistics,
ingredient consumption, container leftovers, player NBT persistence, and
login Packet104 bootstrap. Protocol 14 does not transmit SlotCrafting
result updates on accepted grid clicks, so each result stack is the
ACK-correlated local model confirmed by the slot-0 take.

This crafts the food family. It does not air-use food (M258-M266).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2-reed338-sugar353+workbench58-stew282-bread297-cookie357-cake354|cause=packet102-window0-cane-to-sugar+packet102-workbench-matrix+result-take|wire=packet106-accepted+packet200-craft-stat|oracle=food-family-282-297-357-354+fresh-login|stew=282,bread=297,cookie=357x8,cake=354,buckets=325x3,column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,grid=2x2+3x3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`feb202ff5d2172def94a39a6a9e560b5e4ecdba79681b018a8e046bb89703a54`.

## Non-claims

M327 does not claim eating, cake placement (M244), heal amounts, shift
clicks, or other recipes.
