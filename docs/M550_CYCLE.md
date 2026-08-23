# M550-DISPENSER-QC-SET Dispenser qc set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M550 opens the official dispenser quasi-connectivity SET. Packet15 places
dispenser `23:4` on a raised stone column. The Trap window accepts one
cobblestone `4:0` via Packet102. An east tower then places stone `1:0` on
the dispenser and a floor lever on that block above. The dispenser cell
is not side-levered. The rising edge ejects Packet21 cobble and the Trap
window is empty.

This is distinct from M153/M333, which attach a side lever to the support
(`69:1 -> 69:9` at `5:71:4`) and eject by adjacent power. It does not
claim arrows, buckets, TNT, multi-item RNG, piston QC, or hopper
insertion. Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.

## Qualification cycle

`DispenserQcSetCycle` rebuilds the raised west-facing dispenser QC fixture
in two fresh official server JVMs. Each run places dispenser `23`, loads
cobblestone `4` through the Trap window, powers the stone above with a
floor lever, and awaits Packet21. The support-east cell stays stone, not
a side lever. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/DispenserQcSetCycle.java m550-dispenser-qc-set
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero. The frozen semantic SHA-256 is
`fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.

Expected signal: `column=17,disp=4:72:4:23:4,qc=4:73:4:1:0,lever=4:74:4:floor:0->8,load=4x1,drop=packet21-4x1,remain=empty,power=qc-above,adjacent=none,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.
