# M566-GRASS-SPREAD-SET Grass spread set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M566 qualifies official grass spread from block `2` onto adjacent dirt
`3` as one SET. Packet15 only builds the raised-stone pad: an 8-cell
grass ring, lit dirt samples, and one stone-covered dirt cell. Random
ticks then emit Packet53 `3->2` on a lit dirt sample. A dirt cell
covered by stone stays `3`. The frozen signal names `spread=3->2` and
`covered-stay=true`. Converted cells survive a clean save plus fresh
login.

This family is distinct from shipping M238 grass place and M223 dirt
place. It does not claim farmland, hoe conversion, mycelium, or a
generic spreading-block model. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.

Exact wait length and which lit sample converts are not hashed.

Frozen semantic SHA-256:
`b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.

## Qualification cycle

`GrassSpreadSetCycle` rebuilds the raised stone dirt/grass pad in two
fresh official server JVMs. Each run Packet15-places an 8-cell grass
`2` ring beside lit dirt `3` samples and one stone-covered dirt `3`,
then waits a bounded random-tick window until Packet53 `3->2` appears
on a lit sample while the covered cell stays dirt. One official EOF is
retried after a 5 second sleep.

The frozen signal must name `spread=3->2` and `covered-stay=true` and
must not claim M238/M223 place oracles.

Run directly with:

```text
java tools/smoke/GrassSpreadSetCycle.java m566-grass-spread-set
```

The frozen semantic SHA-256 is
`b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,grass-ring=8,source=2:0,lit=4:72:4+6:72:4+2:72:4+4:72:2,covered=4:72:6:3:0,cover=4:73:6:1:0,spread=3->2,covered-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.
