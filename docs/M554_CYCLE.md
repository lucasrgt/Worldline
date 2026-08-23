# M554-EXTENDED-HEAD-BREAK-SET Extended head break set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M554 opens the official extended-piston leftover boundary. It clones the
M367 west-facing piston-`33` arm and the M300/M375 Packet14 harvest
pattern into one family cycle.

One headless session builds the raised stone column, places piston `33`
facing west with a stone payload and side lever, then Packet15-extends
until head `34:4` is present. Packet14 while holding iron pickaxe `257`
breaks the extended BASE. Official leftover cleanup removes head `34`
and drops Packet21 piston `33`. The base is gone. Those air cells remain
after a clean save plus fresh login.

This is distinct from M367 retract-by-unpower, which keeps piston `33:4`
after lever depower. It does not claim sticky-head leftover, breaking the
head first, quasi-connectivity, or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen semantic SHA-256:
`2cc464442cf4d3f0a5f88c7cb81921c7594834d6c9114630b54798241b4c5cbf`.

## Qualification cycle

`ExtendedHeadBreakSetCycle` rebuilds the cloned piston-`33` west arm in
two fresh official server JVMs. Each run extends piston `33` until head
`34` is present, Packet14-breaks the extended BASE with iron pickaxe
`257`, and reloads the leftover air cells after save plus fresh login.
The frozen signal includes `extend` and `head-break` and must not include
`retract`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`2cc464442cf4d3f0a5f88c7cb81921c7594834d6c9114630b54798241b4c5cbf`.

Run directly with:

```text
java tools/smoke/ExtendedHeadBreakSetCycle.java m554-extended-head-break-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,extend=33:4->12,head-break=33:12->0,piston=4:65:4:33:4->12->0,head=3:65:4:1:0->34:4->0:0,pushed=2:65:4:0:0->1:0->1:0,lever=5:64:4:69:1->9,drops=packet21-33,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `2cc464442cf4d3f0a5f88c7cb81921c7594834d6c9114630b54798241b4c5cbf`.
