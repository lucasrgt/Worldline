<!-- worldline-map-schema=1 -->
<!-- boundary=m270-iron-helmet -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d62f78b5a3cb690f1845fa802de6bfa0cca27bc60ed090c60f93fdc665bf4f07 -->

# M270 iron helmet map

## Stable boundary

- The actor takes undamaged iron helmet `306` from personal window 0 slot
  `36` and left-clicks empty armor slot `5`.
- The production Packet102 encoder is checked byte-for-byte for source
  slot `36`, armor slot `5`, actions `1` and `2`, no shift, the exact take
  stack, and a null place prediction. The pair is 23 bytes, not M65's 92.
- An independent named peer observes Packet5 slot `4` as item `306`, not
  leather `298`.
- A clean save plus fresh login restores Packet104 window slot `5` and the
  peer Packet5 bootstrap value.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotArmor
acceptance for iron helmet `306`, Packet106 acknowledgements, Packet5
equipment broadcasts, player NBT persistence, and fresh-login bootstrap.
Packet5 carries item ID and damage but no count; count one is established
by the actor's window state.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=iron-helmet306|cause=packet102-window0-slot36-to-5|wire=packet5-slot4-item306|oracle=live-equip+peer+fresh-login|window=5:306,packet5=4:306,item!=298,actions=1,2,persisted=true,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`d62f78b5a3cb690f1845fa802de6bfa0cca27bc60ed090c60f93fdc665bf4f07`.

## Non-claims

M270 does not claim other iron pieces, gold, diamond, chain, leather `298`,
damaged armor, replacement, removal, durability, combat, damage reduction,
generic inventory clicks, shift/right clicks, or rejected recovery.
