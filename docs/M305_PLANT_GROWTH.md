# M305 plant growth

M305 opens the official growth-family compound boundary. Wooden hoe Packet15
tills dirt into farmland `60`. Seeds item `295` plant wheat `59:0`. Bonemeal
dye `351:15` Packet15 on that crop writes metadata `7` through official
`BlockCrops.fertilize`. The same raised fixture plants cactus item `81` on
isolated sand and sugar cane item `338` on dirt beside still water `9:0`.
Official random ticks then grow at least one cactus and one cane from height
`1` to height `>= 2`. A fresh login rereads wheat `59:7` plus both grown
stacks.

Exact wait length and extra height above 2 are not hashed, because random-tick
clocks are not a frozen delay. The frozen oracle is the bonemeal wheat age
jump plus categorical cactus and cane height `>= 2`.

Frozen semantic SHA-256:
`b755666b909da0bc4583bf752a32ff032894d3959b4dd0a47c56d3e80c066721`.

This milestone does not claim harvest, hoe durability, trampling, rain, sand
cane planting, bone meal on cactus or cane, height 3, exact metadata clocks,
or a Worldline crop simulation.
