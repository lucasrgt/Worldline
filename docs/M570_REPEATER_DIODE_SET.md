# M570-REPEATER-DIODE-SET repeater diode set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M570 opens the official Beta 1.7.3 repeater diode. A west-facing repeater conducts a lever pulse from its input dust to its output dust while the reverse lever on the output path leaves the repeater unpowered and does not power the input side. This is distinct from M170 place/power and M341 delay-tune.

## Qualification cycle

DataDrivenCycle executes RepeaterDiodeSetSmoke twice on fresh official server JVMs. Each run places a west-facing repeater between input and output dust, proves reverse-lever isolation, then proves forward conduction, and reloads the powered diode after save plus fresh login. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,repeater=4:72:4:93:3->94:3,facing=3,delay=1,reverse=rpt=93:3+in=55:0+out=55:15,forward=rpt=94:3+in=55:15+out=55:15,isolated=true,persisted=94:3,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `82681454d27796440d2c56cc8d0a67ef9f43084871bd0b155424f9b01f827c90`.
