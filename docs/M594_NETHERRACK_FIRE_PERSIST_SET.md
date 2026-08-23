# M594-NETHERRACK-FIRE-PERSIST-SET netherrack fire persist

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Fire on netherrack persists across a long observation window while fire on wood or planks expires. This is distinct from flint-and-steel stone ignition and from fire-support extinguish. Overworld netherrack is sufficient.

## Qualification cycle

DataDrivenCycle boots two official Beta 1.7.3 server JVMs. Each run raises an isolated stone column, places netherrack 87 and planks 5, ignites both with flint and steel 259, then requires the netherrack flame to remain while the plank flame expires. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,nether-fire=4:73:4:51,planks=5:73:4:5,plank-fire=5:74:4:expired,hold=2400,netherrack-persist=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `4ae2baf1dfa018b12ca7517f9660064ac936c01f90c9a76e84a3b25da95e8683`.
