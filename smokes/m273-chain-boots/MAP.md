<!-- worldline-map-schema=1 -->
<!-- boundary=m273-chain-boots -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=509d729ffedcf64fb1478260c71654e80183c1936480c9db878459abe189ec16 -->

# M273 chain boots map

## Stable boundary

- The actor equips undamaged chain boots item `305` through two accepted
  window-0 left-click Packet102 transactions.
- The production Packet102 encoder is checked byte-for-byte for source slot
  36, armor slot 8, actions 1 and 2, no shift, exact take stack 305, and a
  null place prediction.
- An independent named peer observes Packet5 slot 1 containing 305, which is
  distinct from leather boots 301.
- A clean save plus fresh login proves local Packet104 armor slot 8 and the
  peer Packet5 bootstrap value survive disconnect.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotArmor
acceptance, Packet106 acknowledgements, Packet5 equipment broadcasts, player
NBT persistence, and login bootstrap behavior. Packet5 carries item ID and
damage but no count; count one is established by the actor's window state.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=chain-boots305|window0=8|actions=1,2-accepted|cursor=empty-after-pair|packet5=1:305|damage=0|distinct=leather301|restart=window0+packet5-bootstrap|persisted=1|clients=4|disconnect=clean
```

Frozen semantic SHA-256:
`509d729ffedcf64fb1478260c71654e80183c1936480c9db878459abe189ec16`.

## Non-claims

M273 does not claim other chain pieces, leather/iron/gold/diamond boots,
damaged armor, replacement, removal, durability, combat, damage reduction,
generic inventory clicks, shift/right clicks, or rejected armor transaction
recovery.
