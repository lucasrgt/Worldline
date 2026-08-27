# M665-LEAF-SUPPORT-DISTANCE leaf support distance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M665 freezes the official Beta 1.7.3 leaf-decay support boundary. One oak log supports an isolated player-placed leaf at axis distance four, while a second isolated leaf at axis distance five decays during the same bounded random-tick window. The log remains present, and the near leaf plus far-leaf air state survive a clean save and fresh login. This refines the all-support-removed decay family in M385 without claiming leaf-chain propagation, species differences, shear drops, sapling rates, or a Worldline decay simulator. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone fixture in two fresh official server JVMs. Each run scans the selected chunk for nearby natural foliage, raises an isolated stone pad, places one oak log and two nonadjacent leaves at axis distances four and five, then observes the near leaf remain 18:8 while the far leaf becomes air 0:0 under bounded official random ticks. The log and both support cells are checked before and after a clean save plus fresh login. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,log=17:0,near=18:8@distance4->leaf,far=18:8@distance5->0:0,support-radius=4,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `8dec76efd445287c28769682efe28a4f16e065e688f0dc27a93654ac5022120a`.
