# M577-WHEAT-LIGHT-HALT-SET wheat light halt set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M577 qualifies official wheat age halt in darkness as one SET. Wooden hoe Packet15 tills dirt 3 to farmland 60. Seeds item 295 plant wheat 59:0. One crop is covered by stone 1 so its light is 0 and its metadata stays 0. Lit control crops on the same pad increment age under official random ticks. This is distinct from M179 wheat place. Exact wait length and which lit crop ages are not hashed. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone farmland pad in two fresh official server JVMs. Each run Packet15-hoes dirt, plants seeds 295, covers one wheat 59 with stone 1, then waits a bounded random-tick window until Packet53 ages a lit crop while the covered crop stays 59:0. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions.

Expected signal: `column=17,support=4:71:4:1:0,water=5:72:3:9:0,hoe=290,seeds=295,wheat=59,lit=6:73:4+2:73:4+4:73:2,covered=4:73:6:59:0,cover=4:74:6:1:0,lit-age>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `4afa82e0dca6bebe303f9a381e5dbd2b69bd22f7d2f13a32082b6c262cadb425`.
