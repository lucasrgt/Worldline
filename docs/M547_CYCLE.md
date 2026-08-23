# M547-STICKY-PISTON-QC-SET Sticky piston qc set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M547 opens the official sticky-piston quasi-connectivity boundary. It
clones the M367 west-facing sticky-`29` arm, then powers the stone ABOVE
the piston instead of the piston cell or the M144/M367 support lever.

One headless session builds the west-facing sticky arm on the raised
stone column, places QC stone on the piston, and Packet15-toggles the
above-block lever. Sticky piston `29` extends through QC, then pulls the
stone payload on retract. Those final cells remain after a clean save
plus fresh login.

Frozen semantic SHA-256:
`21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.

This is distinct from M546 regular piston-`33` QC, from M367 dual-arm
motion, and from shipping M144 1:1 support-lever pull. It does not claim
BUD updates, two-block chains, push limits, obsidian rejection, or a
generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

## Qualification cycle

`StickyPistonQcSetCycle` rebuilds the cloned sticky-`29` QC arm in two
fresh official server JVMs. Each run powers the block ABOVE sticky
piston `29`, extends through quasi-connectivity, pulls the payload on
retract, and reloads the final arm after save plus fresh login. The
frozen signal includes `qc-extend` and `qc-pull`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.

Run directly with:

```text
java tools/smoke/StickyPistonQcSetCycle.java m547-sticky-piston-qc-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,qc-extend=29:4->12,qc-pull=29:12->4,piston=4:65:4:29:4->12->4,head=3:65:4:1:0->34:12->1:0,pushed=2:65:4:0:0->1:0->0:0,qc=4:66:4:1:0,lever=5:66:4:69:1->9->1,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.
