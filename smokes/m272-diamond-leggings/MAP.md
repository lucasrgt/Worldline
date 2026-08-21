# M272 diamond leggings map

## Stable boundary

- The actor takes undamaged diamond leggings `312` from personal slot 36
  and left-clicks empty armor slot 7 through two accepted Packet102
  transactions.
- An independent named peer observes Packet5 equipment slot 2 as item
  `312`, which is distinct from leather leggings `300`.
- A clean save plus fresh login restores local window slot 7 and the
  peer Packet5 bootstrap value.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotArmor
acceptance for diamond leggings, Packet106 acknowledgements, Packet5
equipment broadcasts, player NBT persistence, and login bootstrap.

## Non-claims

M272 does not claim other diamond pieces, other armor materials,
damaged armor, replacement, unequip, durability, combat, or damage
reduction.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=diamond-leggings312|cause=packet102-window0-slot36-to-slot7|wire=packet5-slot2-item312|oracle=live-equip+peer-packet5+fresh-login|window=7:312,packet5=2:312,leather=300,distinct=true,actions=1,2,persisted=true,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`84c5aee2ec930400dffd8c3fb58277fed715027fa6a538ea53d5acc3cd24b9a0`.
