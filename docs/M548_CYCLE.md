# M548-PISTON-BUD-SET Piston bud set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M548 opens the official classic piston-BUD pulse boundary. It clones the
M367 west-facing piston-`33` arm, then proves one neighbor-place extension
pulse with no continuous power.

One headless session builds piston `33:4` and a stone payload. There is no
lever. Packet15 of torch item `76` on that payload is the neighbor update.
The official server emits moving piston `36:4`, then retracts to `33:4`
with the payload left behind and the torch gone. The frozen signal includes
`bud-pulse` and `power=none`. Those final cells remain after a clean save
plus fresh login.

Frozen semantic SHA-256:
`64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.

This is distinct from M367 lever-power and from M546 QC-with-power-above
remaining on. It does not claim sticky BUD, dispenser QC, or a generic
redstone evaluator.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

## Qualification cycle

`PistonBudSetCycle` rebuilds the cloned piston-`33` BUD arm in two fresh
official server JVMs. Each run places a neighbor torch on the payload,
observes one official extension pulse that self-clears to retracted
`33:4`, and reloads that final arm after save plus fresh login. The frozen
signal includes `bud-pulse` and `power=none`. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.

Run directly with:

```text
java tools/smoke/PistonBudSetCycle.java m548-piston-bud-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,bud-pulse=33:4->36:4->33:4,piston=4:65:4:33:4->36:4->33:4,head=3:65:4:1:0->0:0->0:0,pushed=2:65:4:0:0->1:0,torch=3:66:4:0:0,power=none,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.
