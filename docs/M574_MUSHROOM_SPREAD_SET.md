# M574-MUSHROOM-SPREAD-SET mushroom spread set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M574 qualifies official mushroom spread from placed brown `39` and red `40` onto adjacent valid dark opaque blocks as one SET. Packet15 only builds a roofed 7x7 stone pad: striped mushroom sources, open dark stone targets, and one glass `20` control. Random ticks then emit Packet53 air-to-39 or air-to-40 on a dark opaque sample. Air above glass stays empty. Converted cells survive a clean save plus fresh login.

This family is distinct from shipping M200/M201/M383 mushroom place. It does not claim huge mushrooms, bone-meal, or a generic spreading-block model. Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Exact wait length and which dark sample converts are not hashed.

## Qualification cycle

DataDrivenCycle rebuilds the roofed 7x7 pad in two fresh official server JVMs. Each run Packet15-places striped brown `39` and red `40` sources beside dark stone air samples and one glass `20` cell, then waits a bounded random-tick window until Packet53 air-to-mushroom appears on a dark opaque sample while the glass cell stays air. One official EOF is retried after a 5 second sleep.

The frozen signal must name `spread=air->39/40` and `glass-stay=true` and must not claim M200/M201/M383 place oracles.

Run with:

```text
java tools/smoke/DataDrivenCycle.java m574-mushroom-spread-set
```

Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,pad=7x7,sources=15,brown=9,red=6,targets=9,glass=4:71:5:20:0,roof=4:73:4:1:0,spread=air->39/40,glass-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `c811235b7974b4ef624d19676213d5795b8b284eb89feb64110d5dc20703b076`.
