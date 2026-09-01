<!-- worldline-map-schema=1 -->
<!-- boundary=iron-door-hand-rejection -->
<!-- nonclaims=m118-redstone-iron-door,m277-wooden-door-toggle,m629-door-sound-event,powered-open-metadata,lever-or-redstone-activation,door-sounds -->
<!-- frozen-trace=e8f4f08fbbf53ebc5be10da0fb4931c55779f21c37f2c92b7704bb5487294c9c -->

# M751 behavior map

The fixture raises a deterministic stone column above water and places official
iron door item `330` as BlockDoor `71` halves: lower `71:0`, upper `71:8`.
The actor then selects an empty hand slot and sends Packet15 activation at the
lower half, settles the server ticks, and repeats the activation at the upper
half. After each attempt the settled server world still reports exactly
`71:0` and `71:8`: the official server rejects manual toggling and preserves
both closed halves with zero block edits. A clean save plus a fresh-login
reader persists both halves unchanged.

This map does not claim powered iron-door behavior, lever or redstone
activation (M118), wooden-door toggling (M277), door sound events (M629),
trapdoors, or any open-state metadata. Headless `B173WireClient` only.
No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+closed-irondoor71:0/8|cause=packet15-empty-hand-activate-lower-half+upper-half|wire=no-block-change-after-settled-ticks|oracle=iron-door-hand-rejection-preserves-both-closed-halves-not-m118-redstone-not-m277-wooden-toggle|door=71:0/8,lower-hand=rejected,upper-hand=rejected,preserved=71:0/8,persisted=closed,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e8f4f08fbbf53ebc5be10da0fb4931c55779f21c37f2c92b7704bb5487294c9c`.
