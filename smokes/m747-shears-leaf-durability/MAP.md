<!-- worldline-map-schema=1 -->
<!-- boundary=shears-leaf-durability -->
<!-- nonclaims=sheep-shearing,bare-hand-harvest,multi-break-accumulation,login-persistence,sapling-chances,other-leaf-species -->
<!-- frozen-trace=34f781cee4aaae6e7ef7792e8c26bc3c5e525aa5363d0b525bf36089eb25104a -->

# M747 shears leaf durability behavior map

The public boundary is `worldline.testkit.ShearsLeafDurabilityFixture#harvest`. Its fixture begins with pristine shears `359:1:0` held over one placed oak-leaf block `18:8` sitting on oak log `17`. Its action is one Packet14 break of that single leaf cell by the held shears. Its observations are the leaf cell becoming air `0:0`, exactly one Packet21 oak-leaf item stack `18:1:0`, and the held shear stack moving from damage `0` to exactly damage `1` through Packet103.

This map does not claim sheep shearing, bare-hand harvesting, durability accumulation across multiple breaks, persistence across a fresh login, sapling drop chances, or other leaf species.

The proof uses two fresh official Beta 1.7.3 dedicated-server replicas at seed `17320110707`. It does not use mapped source, controlled-runtime substitution, or runtime RNG writes.

Frozen signal: `tool=shears359,leaf=18:8->0:0,drop=packet21-18:1:0,shears=359:0->359:1,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `34f781cee4aaae6e7ef7792e8c26bc3c5e525aa5363d0b525bf36089eb25104a`.
