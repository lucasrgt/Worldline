# M559-DOUBLE-EXTENDER-SET Double extender set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M559 opens the official two-piston double-extender boundary. It clones the
M142/M367 west-facing piston family and sequences sticky piston `29` then
normal piston `33` so one cobble payload travels two cells.

One headless session builds both arms on the raised stone column. Lever
Packet15 first extends sticky `29` and shifts the regular piston plus cobble
one cell, then extends piston `33` so cobble travels a second cell. Those
final cells remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.

This is distinct from M145 two-block payload on one piston (one-cell
two-material shift) and from M147 twelve-block push capacity. It does not
claim retraction of the extender, quasi-connectivity, 0-tick pulses, slime,
or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

## Qualification cycle

`DoubleExtenderSetCycle` rebuilds the cloned sticky-`29` then piston-`33`
double extender in two fresh official server JVMs. Each run sequences the
rear sticky arm, then the front regular arm, so one cobble payload travels
two cells, and reloads the extended chain after save plus fresh login. The
frozen signal includes `cells=2` and `sequenced=29-then-33`. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.

Run directly with:

```text
java tools/smoke/DoubleExtenderSetCycle.java m559-double-extender-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,cells=2,sequenced=29-then-33,rear=29:4->12,front=33:4->12,rear-cell=4:65:4:29:12,front-from=3:65:4,front-to=2:65:4:33:12,payload=2:65:4->1:65:4->0:65:4:4:0,sticky-head=3:65:4:34:12,piston-head=1:65:4:34:4,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.
