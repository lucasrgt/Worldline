<!-- worldline-map-schema=1 -->
<!-- boundary=m408-squid-ink-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4f3c68e6439036720158970ea6fb62f2db5d9bb980f42850dbb0cfdf53ac0f41 -->

# M408 behavior map

The fixture finds the deterministic dirt-plus-water column in seed
`17320110707` chunk `0,0` and raises a one-column stone dock through that
water so the actor's head stays in air. Surrounding still or flowing water
remains the squid habitat. Packet7 with diamond sword `276` kills Packet24
type `94`. Packet21 must include ink sac `351:0`. A bounded retry covers
vanilla `nextInt(3)` zero-drop outcomes without freezing drop count or
coordinates.

This map does not re-qualify M328 ink as a craft input or M389 cow/chicken
drops. It does not claim cooked drops, XP, breeding, or other water mobs.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=seed-water-column+surface-stone-dock|cause=official-diamond-sword-packet7|wire=packet24-type94+packet38-status3+packet29+packet21-351:0|oracle=squid-in-water-plus-ink-sac-drop|column=9,surface=4:63:4:1:0,habitat=4:55:4:9:0,mob=type94,death=packet7-sword276+packet38-status3+packet29,drop=packet21-351:0,kills<=8,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`4f3c68e6439036720158970ea6fb62f2db5d9bb980f42850dbb0cfdf53ac0f41`.
