<!-- worldline-map-schema=1 -->
<!-- boundary=m271-gold-chestplate -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c494170f3fb2b9f4b9ec515518081834f5fe6ccd52977a7bb7e82fc946101fea -->

# M271 gold chestplate map

## Stable boundary

- The actor takes undamaged gold chestplate `315` from personal window
  slot 36 and left-clicks empty armor slot 6.
- Actions 1 and 2 are accepted. The cursor is empty after the pair.
- An independent named peer observes Packet5 equipment slot 3 as `315`.
- The equipped stack is not leather chestplate `299` and not iron
  chestplate `307`.
- Save plus a fresh login restores window slot 6 and Packet5 slot 3.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotArmor
acceptance for gold chestplate, Packet106 acknowledgements, Packet5
equipment broadcasts, player NBT persistence, and login bootstrap.
Packet5 carries item ID and damage but no count; count one is established
by the actor's window state.

## Frozen trace

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=gold-chestplate315|cause=packet102-window0-slot36-to-6|wire=packet5-slot3:315|oracle=live-window6+peer-packet5+fresh-login|window=6:315,packet5=3:315,distinct-from=299,307,persisted=true,clients=4,disconnect=clean
```

Frozen semantic SHA-256:
`c494170f3fb2b9f4b9ec515518081834f5fe6ccd52977a7bb7e82fc946101fea`.

## Non-claims

M271 does not claim other gold pieces, leather or iron armor, damaged
armor, replacement, removal, durability, combat, or rejected clicks.
