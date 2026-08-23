# M606-FIRE-SPREAD-WOOD-SET fire spread wood set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M606 qualifies official fire spread from a burning source onto adjacent flammable planks 5 and wood 17 as one SET. Packet15 of flint-and-steel item 259 ignites netherrack 87 under a stone sky cover. Official random ticks then emit Packet53 fire 51 in an air cell above the wood ring. The source fire remains 51. The frozen signal names spread=air->51, cover=4:76:4:1:0, and source-stay=true. Netherrack fire persists across a clean save plus fresh login. This family is distinct from shipping M268 flint-and-steel place and M515 fire-support extinguish. It does not claim rain extinguishing, leaves, wool, or a generic fire-propagation model. Headless B173WireClient protocol-14 only. No GUI. No Aero. Exact wait length and which wood-adjacent air cell ignites are not hashed.

## Qualification cycle

DataDrivenCycle rebuilds the raised covered netherrack-plus-wood pad in two fresh official server JVMs. Each run Packet15-ignites netherrack 87, then waits a bounded random-tick window until Packet53 fire 51 appears in air adjacent to planks 5 or wood 17. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero. Qualify it with java tools/harness/Gate.java --milestone m606-fire-spread-wood-set.

Expected signal: `column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,source-fire=4:73:4:51,wood-ring=8,cover=4:76:4:1:0,fuels=5+17,spread=air->51,source-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `4b6a960897e496015d385c4ad2f648d15557860809b692c0e544595f6635f9bc`.
