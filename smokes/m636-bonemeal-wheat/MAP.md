<!-- worldline-map-schema=1 -->
<!-- boundary=bonemeal-wheat -->
<!-- nonclaims=natural-growth,harvest,bone-crafting,other-crops,modern-bonemeal -->
<!-- frozen-trace=c14f3fee52def6b9e4d6c1f009b9a4bc03b7a8cca09d9cdf3b94ed0a94fd60b1 -->

# M636 bonemeal wheat behavior map

The public boundary is `worldline.testkit.BonemealWheatFixture#observe`. Its fixture begins with wheat `59:0` on farmland `60:0`. Its action is official Packet15 use of dye `351:15`. Its observations are immediate wheat `59:7` and the same state after save and fresh login. Its equatable evidence retains the catalyst identity and before, after, and persisted states while excluding coordinates and packet timing.

The proof uses two fresh official Beta 1.7.3 dedicated-server replicas. It does not use mapped source, controlled-runtime substitution, block-state injection after planting, or runtime RNG writes.

Frozen signal: `wheat=59:0->59:7,farmland=60:0,bonemeal=351:15,packet=15,persisted=true,replicas=2,disconnect=clean`.
