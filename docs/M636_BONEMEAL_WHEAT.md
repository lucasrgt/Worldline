# M636-BONEMEAL-WHEAT bonemeal wheat

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M636 qualifies the Beta 1.7.3 bonemeal-to-wheat boundary as a focused reusable contract. Planted wheat 59:0 on farmland 60 receives dye item 351 damage 15 through Packet15 and becomes mature wheat 59:7. A fresh login observes the same mature state. The claim excludes natural random-tick growth, harvest drops, bone crafting, other crops, and post-Beta probabilistic bonemeal behavior.

## Qualification cycle

DataDrivenCycle executes two fresh official dedicated-server replicas at seed 17320110707. Each replica builds raised farmland, plants age-zero wheat, applies one seeded bonemeal item, observes Packet53 maturity, saves, and reconnects. BonemealWheatFixture rejects the wrong catalyst, intermediate wheat ages, and missing persistence while normalizing fixture coordinates and packet timing.

Expected signal: `wheat=59:0->59:7,farmland=60:0,bonemeal=351:15,packet=15,persisted=true,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `c14f3fee52def6b9e4d6c1f009b9a4bc03b7a8cca09d9cdf3b94ed0a94fd60b1`.
