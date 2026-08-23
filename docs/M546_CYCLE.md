# M546-PISTON-QC-SET Piston qc set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M546 opens the official BlockPistonBase quasi-connectivity boundary.
A regular piston `33` extends when the block ABOVE the piston is
powered, while the piston cell itself is not directly powered: no
adjacent dust, torch, or lever on the piston.

One headless session builds a west-facing piston `33:4` on the raised
stone column, places stone on top of the piston, and attaches lever
`69` to that above-block. Lever Packet15 QC-extends `33:4 -> 33:12`
with head `34:4`, then unpowers the above-block and retracts. Those
final cells remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.

This is distinct from M367 (lever on the piston support), shipping
M142-M147 1:1 piston cycles, and M427 remaining place-facings. It does
not claim sticky QC, BUD-without-update, two-block chains, push limits,
obsidian rejection, or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

## Qualification cycle

`PistonQcSetCycle` rebuilds the cloned piston-`33` QC arm in two fresh
official server JVMs. Each run powers the block above the piston so
normal piston `33` QC-extends and then retracts after the above-block
is unpowered, and reloads the final arm after save plus fresh login.
The frozen signal includes `qc-extend`, `qc-retract`, and
`direct-power=false`. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.

Run directly with:

```text
java tools/smoke/PistonQcSetCycle.java m546-piston-qc-set
```

Canonical evidence uses two official server JVMs and four client
sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,qc-extend=33:4->12,qc-retract=33:12->4,piston=4:65:4:33:4->12->4,head=3:65:4:0:0->34:4->0:0,above=4:66:4:1:0,lever=5:66:4:69:1->9->1,direct-power=false,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.
