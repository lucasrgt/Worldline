<!-- worldline-map-schema=1 -->
<!-- boundary=redstone-ore-dust-drop -->
<!-- nonclaims=m229-place-persistence,m571-glow-fade,m300-other-ore-breaks,fortune,bare-hand-rejection,total-dust-quantity,dust-entity-count,probability -->
<!-- frozen-trace=473194f1f2bf89eb01ae9058b28e52d6844576ce5122675f531d9f8096bfcf0f -->

# M743 behavior map

The fixture rebuilds the deterministic raised stone column from the
dirt-under-water foundation of seed `17320110707` chunk `0,0` and places one
unlit redstone ore item `73` on the top support face, landing at cell
`4:72:4`. The live view must read `73:0`. The actor selects iron pickaxe
`257`, sends the full Packet14 dig at that cell, and the authoritative view
must flip the cell to air `0:0`.

The harvest must emit at least one Packet21 dropped-item spawn whose stack is
exactly redstone dust `331x1` (id 331, count 1, damage 0). Packet21 describes
an individual dropped-item entity stack, so this exact-stack equality is the
narrow protocol-observable membership claim. It is polled through
`BoundedAttempts.until(40, ...)`; the frozen evidence records the observation
bound only.

Nonclaims: this map does not claim how many dust entities spawn, their total
dust quantity, or any probability or single random draw; the existing public
harness cannot equate those aggregates. It also does not re-qualify M229
unlit placement persistence, M571 click or step glow fade, M300 cobble, coal,
or diamond pick breaks, fortune behavior, bare-hand rejection, or durability
consumption. Headless `B173WireClient` only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-column+redstone-ore73|cause=packet14-ironpick257-full-break|wire=packet53-air+packet21-331x1|oracle=redstone-ore-dust-drop-not-m229-place-not-m571-glow|column=17,support=4:71:4:1:0,ore=4:72:4:73:0->0:0,pick=iron257,dust=packet21-331x1,wait=bounded<=40,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`473194f1f2bf89eb01ae9058b28e52d6844576ce5122675f531d9f8096bfcf0f`.
